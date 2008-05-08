/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.repository.local;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipsetrader.core.repositories.IRepository;
import org.eclipsetrader.core.repositories.IRepositoryRunnable;
import org.eclipsetrader.core.repositories.IStore;
import org.eclipsetrader.repository.local.internal.Activator;
import org.eclipsetrader.repository.local.internal.IdentifiersCollection;
import org.eclipsetrader.repository.local.internal.SecurityCollection;
import org.eclipsetrader.repository.local.internal.WatchListCollection;
import org.eclipsetrader.repository.local.internal.stores.RepositoryStore;

public class LocalRepository implements IRepository, ISchedulingRule {
	public static final String URI_SCHEMA = "local";
	public static final String URI_SECURITY_PART = "securities";
	public static final String URI_SECURITY_HISTORY_PART = "securities/history";
	public static final String URI_WATCHLIST_PART = "watchlists";

	public static final String IDENTIFIERS_FILE = "identifiers.xml"; //$NON-NLS-1$
	public static final String SECURITIES_FILE = "securities.xml"; //$NON-NLS-1$
	public static final String SECURITIES_HISTORY_FILE = ".history"; //$NON-NLS-1$
	public static final String WATCHLISTS_FILE = "watchlists.xml"; //$NON-NLS-1$

	private IdentifiersCollection identifiers;
	private SecurityCollection securities;
	private WatchListCollection watchlists;

	private IJobManager jobManager;
	private final ILock lock;

	public LocalRepository() {
		jobManager = Job.getJobManager();
		lock = jobManager.newLock();

		identifiers = new IdentifiersCollection();
		securities = new SecurityCollection();
	}

	public void startUp() {
		File file = Activator.getDefault().getStateLocation().append(IDENTIFIERS_FILE).toFile();
		identifiers = (IdentifiersCollection) unmarshal(IdentifiersCollection.class, file);
		if (identifiers == null)
			identifiers = new IdentifiersCollection();

		file = Activator.getDefault().getStateLocation().append(SECURITIES_FILE).toFile();
		securities = (SecurityCollection) unmarshal(SecurityCollection.class, file);
		if (securities == null)
			securities = new SecurityCollection();
	}

	protected synchronized void initializeWatchListsCollections() {
		if (watchlists == null) {
			if (Activator.getDefault() != null) {
				File file = Activator.getDefault().getStateLocation().append(WATCHLISTS_FILE).toFile();
				watchlists = (WatchListCollection) unmarshal(WatchListCollection.class, file);
			}
			if (watchlists == null) {
				watchlists = WatchListCollection.getInstance();
				if (watchlists == null)
					watchlists = new WatchListCollection();
			}
		}
	}

	public void shutDown() {
		if (watchlists != null) {
			File file = Activator.getDefault().getStateLocation().append(WATCHLISTS_FILE).toFile();
			marshal(watchlists, WatchListCollection.class, file);
		}

		File file = Activator.getDefault().getStateLocation().append(SECURITIES_FILE).toFile();
		marshal(securities, SecurityCollection.class, file);

		file = Activator.getDefault().getStateLocation().append(IDENTIFIERS_FILE).toFile();
		marshal(identifiers, IdentifiersCollection.class, file);
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
		return new RepositoryStore();
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IRepository#fetchObjects(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStore[] fetchObjects(IProgressMonitor monitor) {
		List<IStore> list = new ArrayList<IStore>();
		list.addAll(Arrays.asList(securities.getAll()));

		if (watchlists == null)
			initializeWatchListsCollections();
		list.addAll(Arrays.asList(watchlists.getAll()));

		return list.toArray(new IStore[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.repositories.IRepository#getObject(java.net.URI)
	 */
	public IStore getObject(URI uri) {
		if (URI_SECURITY_PART.equals(uri.getSchemeSpecificPart()))
			return securities.get(uri);

		if (URI_WATCHLIST_PART.equals(uri.getSchemeSpecificPart())) {
			if (watchlists == null)
				initializeWatchListsCollections();
			return watchlists.get(uri);
		}

		return null;
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
		try {
			lock.acquire();
    		try {
    			status = runnable.run(monitor);
    		} catch(Exception e) {
    			status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
    			Activator.getDefault().getLog().log(status);
    		} catch(LinkageError e) {
    			status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
    			Activator.getDefault().getLog().log(status);
    		}
		} catch (Exception e) {
			status = new Status(Status.ERROR, Activator.PLUGIN_ID, 0, "Error running repository task", e); //$NON-NLS-1$
			Activator.getDefault().getLog().log(status);
		} finally {
			lock.release();
			jobManager.endRule(rule);
		}
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
		return null;
	}

	@SuppressWarnings("unchecked")
	protected void marshal(Object object, Class clazz, File file) {
		try {
			if (file.exists())
				file.delete();
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setEventHandler(new ValidationEventHandler() {
				public boolean handleEvent(ValidationEvent event) {
					Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
					Activator.getDefault().getLog().log(status);
					return true;
				}
			});
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$
			marshaller.marshal(object, new FileWriter(file));
        } catch (Exception e) {
    		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error saving securities", null); //$NON-NLS-1$
    		Activator.getDefault().getLog().log(status);
        }
	}

	@SuppressWarnings("unchecked")
    protected Object unmarshal(Class clazz, File file) {
		try {
			if (file.exists()) {
	            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
	            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	            unmarshaller.setEventHandler(new ValidationEventHandler() {
	            	public boolean handleEvent(ValidationEvent event) {
	            		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
	            		Activator.getDefault().getLog().log(status);
	            		return true;
	            	}
	            });
	            return unmarshaller.unmarshal(file);
			}
        } catch (Exception e) {
    		Status status = new Status(Status.WARNING, Activator.PLUGIN_ID, 0, "Error loading identifiers", null); //$NON-NLS-1$
    		Activator.getDefault().getLog().log(status);
        }
        return null;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	    return "Local";
    }
}
