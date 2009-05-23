/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.repository.hibernate;

 import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.osgi.util.NLS;
import org.eclipsetrader.core.feed.IFeedIdentifier;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.repository.hibernate.internal.Activator;
import org.eclipsetrader.repository.hibernate.internal.RepositoryDefinition;
import org.eclipsetrader.repository.hibernate.internal.RepositoryValidator;
import org.eclipsetrader.repository.hibernate.internal.stores.HistoryStore;
import org.eclipsetrader.repository.hibernate.internal.stores.IntradayHistoryStore;
import org.eclipsetrader.repository.hibernate.internal.stores.RepositoryStore;
import org.eclipsetrader.repository.hibernate.internal.stores.SecurityStore;
import org.eclipsetrader.repository.hibernate.internal.stores.TradeStore;
import org.eclipsetrader.repository.hibernate.internal.stores.WatchListStore;
import org.eclipsetrader.repository.hibernate.internal.types.DividendType;
import org.eclipsetrader.repository.hibernate.internal.types.HistoryData;
import org.eclipsetrader.repository.hibernate.internal.types.IdentifierPropertyType;
import org.eclipsetrader.repository.hibernate.internal.types.IdentifierType;
import org.eclipsetrader.repository.hibernate.internal.types.SecurityUnknownPropertyType;
import org.eclipsetrader.repository.hibernate.internal.types.SplitData;
import org.eclipsetrader.repository.hibernate.internal.types.WatchListColumn;
import org.eclipsetrader.repository.hibernate.internal.types.WatchListHolding;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

public class HibernateRepository implements IRepository, ISchedulingRule, IExecutableExtension {
	public static final String URI_SECURITY_PART = "securities";
	public static final String URI_SECURITY_HISTORY_PART = "securities/history";
	public static final String URI_SECURITY_INTRADAY_HISTORY_PART = "securities/history/{0}/{1}";
	public static final String URI_WATCHLIST_PART = "watchlists";
	public static final String URI_TRADE_PART = "trades";

	private static final String ERROR_MESSAGE = "Errors occurred updating database";

	private String schema;
	private String name;
	private Properties properties;
	private RepositoryDefinition repositoryDefinition;
	private Session session;

	private Map<String, IdentifierType> identifiersMap;
	private List<WatchListStore> watchlists;
	private Map<URI, IStore> uriMap = new HashMap<URI, IStore>();

	private IJobManager jobManager;
	private final ILock lock;

	public HibernateRepository() {
		this(null, null, new Properties());
	}

	public HibernateRepository(RepositoryDefinition repositoryDefinition) {
		this(repositoryDefinition.getSchema(), repositoryDefinition.getLabel(), repositoryDefinition.getProperties());
		this.repositoryDefinition = repositoryDefinition;
	}

	public HibernateRepository(String schema, String name, Properties properties) {
		this.schema = schema;
		this.name = name;
		this.properties = properties;

		this.identifiersMap = new HashMap<String, IdentifierType>();

		jobManager = Job.getJobManager();
		lock = jobManager.newLock();
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	this.schema = config.getAttribute("scheme");
    	this.name = config.getAttribute("name");

    	IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();

    	String[] propertyNames = new String[] {
    			"file.encoding",
    			"file.separator",
    			"java.home",
    	    	"user.dir",
    	    	"user.home",
    	    	"user.language",
    	    	"user.name",
    	    	"user.timezone",
    		};

    	List<IValueVariable> variables = new ArrayList<IValueVariable>();
    	if (variableManager.getValueVariable("workspace_loc") == null)
    		variables.add(variableManager.newValueVariable("workspace_loc", "Returns the absolute file system path of the workspace root.", true, Platform.getLocation().toOSString()));
    	for (String k : propertyNames) {
    		if (variableManager.getValueVariable(k) == null)
    			variables.add(variableManager.newValueVariable(k, "", true, System.getProperty(k)));
    	}
    	variableManager.addVariables(variables.toArray(new IValueVariable[variables.size()]));

    	for (IConfigurationElement element : config.getChildren()) {
    		String key = element.getAttribute("name");
    		String value = element.getAttribute("value");
    		this.properties.put(key, variableManager.performStringSubstitution(value));
    	}

		try {
	    	startUp(null);
	    	Activator.getDefault().getRepositories().add(this);
		} catch (Exception e) {
			String message = NLS.bind("Error loading repository '{1}' ({0})", new Object[] { schema, name });
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, message, e);
			Activator.log(status);
		}
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.repositories.IRepository#getSchema()
     */
    public String getSchema() {
    	return schema;
    }

	@SuppressWarnings("serial")
    protected AnnotationConfiguration buildConfiguration() {
		AnnotationConfiguration cfg = new AnnotationConfiguration();
		cfg.setProperties(properties);

		cfg.addAnnotatedClass(IdentifierPropertyType.class);
		cfg.addAnnotatedClass(IdentifierType.class);
		cfg.addAnnotatedClass(WatchListColumn.class);
		cfg.addAnnotatedClass(WatchListHolding.class);
		cfg.addAnnotatedClass(HistoryData.class);
		cfg.addAnnotatedClass(DividendType.class);
		cfg.addAnnotatedClass(SplitData.class);
		cfg.addAnnotatedClass(SecurityUnknownPropertyType.class);

		cfg.addAnnotatedClass(SecurityStore.class);
		cfg.addAnnotatedClass(WatchListStore.class);
		cfg.addAnnotatedClass(HistoryStore.class);
		cfg.addAnnotatedClass(IntradayHistoryStore.class);
		cfg.addAnnotatedClass(TradeStore.class);

		cfg.getEventListeners().setPostInsertEventListeners(new PostInsertEventListener[] {
				new PostInsertEventListener() {
                    public void onPostInsert(PostInsertEvent event) {
                    	if (event.getEntity() instanceof IdentifierType)
            				identifiersMap.put(((IdentifierType) event.getEntity()).getSymbol(), (IdentifierType) event.getEntity());
                    	if (event.getEntity() instanceof SecurityStore)
            				uriMap.put(((SecurityStore) event.getEntity()).toURI(), (SecurityStore) event.getEntity());
                    	if (event.getEntity() instanceof WatchListStore)
            				uriMap.put(((WatchListStore) event.getEntity()).toURI(), (WatchListStore) event.getEntity());
                    }
				},
		});
		cfg.getEventListeners().setPostDeleteEventListeners(new PostDeleteEventListener[] {
				new PostDeleteEventListener() {
                    public void onPostDelete(PostDeleteEvent event) {
                    	if (event.getEntity() instanceof IdentifierType)
            				identifiersMap.remove(((IdentifierType) event.getEntity()).getSymbol());
                    	if (event.getEntity() instanceof SecurityStore)
            				uriMap.remove(((SecurityStore) event.getEntity()).toURI());
                    	if (event.getEntity() instanceof WatchListStore)
            				uriMap.remove(((WatchListStore) event.getEntity()).toURI());
                    }
				},
		});

		return cfg;
	}

    public void startUp(IProgressMonitor monitor) {
    	properties.put("hibernate.query.factory_class", "org.hibernate.hql.classic.ClassicQueryTranslatorFactory");
    	properties.put("hibernate.connection.pool_size", "5");
    	properties.put("hibernate.jdbc.batch_size", "20");
    	properties.put("hibernate.show_sql", "false");

    	// Build suitable defaults for file-based databases (Apache Derby and HSQL)
    	if (!properties.containsKey("hibernate.connection.url") && Activator.getDefault() != null) {
    		if ("org.apache.derby.jdbc.EmbeddedDriver".equals(properties.get("hibernate.connection.driver_class")))
        		properties.put("hibernate.connection.url", "jdbc:derby:" + Activator.getDefault().getStateLocation().toOSString() + "/.derby;create=true");
    		if ("org.hsqldb.jdbcDriver".equals(properties.get("hibernate.connection.driver_class")))
        		properties.put("hibernate.connection.url", "jdbc:hsqldb:file:" + Activator.getDefault().getStateLocation().toOSString() + "/.hsqldb");
    	}

		AnnotationConfiguration cfg = buildConfiguration();
		try {
			initializeDatabase(cfg);
		} catch (Exception e) {
			String message = NLS.bind("Error initializing repository '{1}' ({0})", new Object[] { schema, name });
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, message, e);
			Activator.log(status);

			int userChoice = new RepositoryValidator(name, cfg).validate();

			switch (userChoice) {
	        	case RepositoryValidator.UPDATE_ID:
					SchemaUpdate schemaUpdate = new SchemaUpdate(cfg);
					schemaUpdate.execute(true, true);
					if (schemaUpdate.getExceptions().size() != 0) {
						MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, 0, new IStatus[0], ERROR_MESSAGE, null);
						for (Object o : schemaUpdate.getExceptions())
							multiStatus.add(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, null, (Exception) o));
						Activator.log(multiStatus);
					}
	        		break;
	        	case RepositoryValidator.CREATE_ID:
					SchemaExport schemaExport = new SchemaExport(cfg);
					schemaExport.create(true, true);
					if (schemaExport.getExceptions().size() != 0) {
						MultiStatus multiStatus = new MultiStatus(Activator.PLUGIN_ID, 0, new IStatus[0], ERROR_MESSAGE, null);
						for (Object o : schemaExport.getExceptions())
							multiStatus.add(new Status(Status.ERROR, Activator.PLUGIN_ID, 0, null, (Exception) o));
						Activator.log(multiStatus);
					}
	        		break;
	        }
		}
		initializeDatabase(cfg);
	}

	@SuppressWarnings("unchecked")
	void initializeDatabase(AnnotationConfiguration cfg) {
		SessionFactory sessionFactory = cfg.buildSessionFactory();
		session = sessionFactory.openSession();

		List<IdentifierType> identifiers = session.createCriteria(IdentifierType.class).list();
		for (IdentifierType identifierType : identifiers)
			identifiersMap.put(identifierType.getSymbol(), identifierType);

		List<SecurityStore> securities = session.createCriteria(SecurityStore.class).list();
		for (SecurityStore store : securities) {
			store.setRepository(this);
			uriMap.put(store.toURI(), store);
		}
	}

	public void shutDown(IProgressMonitor monitor) {
		try {
			if (session != null)
				session.close();
		} catch (Exception e) {
			String message = NLS.bind("Error shutting down repository {0}:{1}", new Object[] { schema, name });
			Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, message, e);
			Activator.log(status);
		}
	}

	@SuppressWarnings("unchecked")
	protected synchronized void initializeWatchListsCollections() {
		if (watchlists == null) {
			try {
				watchlists = session.createCriteria(WatchListStore.class).list();
				for (WatchListStore store : watchlists) {
					store.setRepository(this);
					uriMap.put(store.toURI(), store);
				}
			} catch (Exception e) {
				String message = NLS.bind("Error loading repository {0}:{1}", new Object[] { schema, name });
				Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, message, e);
				Activator.log(status);
			}
		}
		if (watchlists == null)
			watchlists = new ArrayList<WatchListStore>();
	}

	public Session getSession() {
    	return session;
    }

	public IdentifierType getIdentifierTypeFromFeedIdentifier(IFeedIdentifier feedIdentifier) {
		IdentifierType type = identifiersMap.get(feedIdentifier.getSymbol());
		if (type == null) {
			type = new IdentifierType(feedIdentifier);
			identifiersMap.put(type.getSymbol(), type);
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IRepository#canDelete()
	 */
	public boolean canDelete() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IRepository#canWrite()
	 */
	public boolean canWrite() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IRepository#createObject()
	 */
	public IStore createObject() {
		return new RepositoryStore(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IRepository#fetchObjects(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStore[] fetchObjects(IProgressMonitor monitor) {
		if (watchlists == null)
			initializeWatchListsCollections();

		List<IStore> list = new ArrayList<IStore>();
		list.addAll(uriMap.values());
		return list.toArray(new IStore[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IRepository#getObject(java.net.URI)
	 */
	public IStore getObject(URI uri) {
		if (URI_WATCHLIST_PART.equals(uri.getSchemeSpecificPart())) {
			if (watchlists == null)
				initializeWatchListsCollections();
		}
		return uriMap.get(uri);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IRepository#runInRepository(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus runInRepository(IRepositoryRunnable runnable, IProgressMonitor monitor) {
    	return runInRepository(runnable, this, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IRepository#runInRepository(org.eclipsetrader.core.repositories.IRepositoryRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus runInRepository(IRepositoryRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
    	IStatus status;

    	jobManager.beginRule(rule, monitor);
		Transaction currentTransaction = null;

		try {
			lock.acquire();
			currentTransaction = session.beginTransaction();
    		try {
    			status = runnable.run(monitor);

    			session.flush();

    			currentTransaction.commit();
    			currentTransaction = null;
    		} catch(Exception e) {
    			status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
    			Activator.log(status);
    		} catch(LinkageError e) {
    			status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
    			Activator.log(status);
    		}
		} catch (Exception e) {
			status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
			Activator.log(status);
		} finally {
			if (currentTransaction != null) {
				try {
					currentTransaction.rollback();
				} catch(Exception e1) {
					Status status1 = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error rolling back transaction", e1); //$NON-NLS-1$
					Activator.log(status1);
				}
			}
			lock.release();
			jobManager.endRule(rule);
		}

		session.clear();

		return status;
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
     */
    public boolean contains(ISchedulingRule rule) {
		if (this == rule)
			return true;
		if (rule instanceof MultiRule) {
			MultiRule multi = (MultiRule) rule;
			ISchedulingRule[] children = multi.getChildren();
			for (int i = 0; i < children.length; i++)
				if (!contains(children[i]))
					return false;
			return true;
		}
	    return false;
    }

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
     */
    public boolean isConflicting(ISchedulingRule rule) {
		if (this == rule)
			return true;
	    return false;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (repositoryDefinition != null && adapter.isAssignableFrom(repositoryDefinition.getClass()))
			return repositoryDefinition;

		if (adapter.isAssignableFrom(getClass()))
			return this;

		return null;
	}

	public RepositoryDefinition getRepositoryDefinition() {
    	return repositoryDefinition;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	    return name;
    }
}
