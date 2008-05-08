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

package org.eclipsetrader.tests.core;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipsetrader.core.feed.IFeedConnector;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarket;
import org.eclipsetrader.core.markets.IMarketDay;

public class TestMarket implements IMarket {
	private String name;
	private Set<ISecurity> members = new HashSet<ISecurity>();
	private IFeedConnector liveFeedConnector;

	public TestMarket(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#addMembers(org.eclipsetrader.core.instruments.ISecurity[])
	 */
	public void addMembers(ISecurity[] securities) {
		members.addAll(Arrays.asList(securities));
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#getLiveFeedConnector()
	 */
	public IFeedConnector getLiveFeedConnector() {
		return liveFeedConnector;
	}

	public void setLiveFeedConnector(IFeedConnector liveFeedConnector) {
    	this.liveFeedConnector = liveFeedConnector;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#getMembers()
	 */
	public ISecurity[] getMembers() {
		return members.toArray(new ISecurity[members.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#getNextDay()
	 */
	public IMarketDay getNextDay() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#getToday()
	 */
	public IMarketDay getToday() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#hasMember(org.eclipsetrader.core.instruments.ISecurity)
	 */
	public boolean hasMember(ISecurity security) {
		return members.contains(security);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#isOpen()
	 */
	public boolean isOpen() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#isOpen(java.util.Date)
	 */
	public boolean isOpen(Date time) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.markets.IMarket#removeMembers(org.eclipsetrader.core.instruments.ISecurity[])
	 */
	public void removeMembers(ISecurity[] securities) {
		members.removeAll(Arrays.asList(securities));
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
	    return null;
    }
}
