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

package org.eclipsetrader.core.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Interface for WatchList type views.
 *
 * @since 1.0
 */
public interface IWatchList extends IAdaptable {
	public static final String NAME = "name";
	public static final String COLUMNS = "columns";
	public static final String HOLDINGS = "holdings";

	/**
	 * Returns the name of the watchlist.
	 *
	 * @return the name.
	 */
	public String getName();

	/**
	 * Sets the name of the watchlist.
	 *
	 * @param name the new name to set.
	 */
	public void setName(String name);

	/**
	 * Adds the columns to the watchlist.
	 *
	 * @param columns the columns to add.
	 */
	public void setColumns(IWatchListColumn[] columns);

	/**
	 * Returns the number of columns contained in the watchlist.
	 *
	 * @return the number of columns.
	 */
	public int getColumnCount();

	/**
	 * Returns an array of <code>IWatchListColumn</code>s which are the
	 * columns in the watchlist. Columns are returned in the order
	 * that they were created.
	 *
	 * @return the columns in the watchlist.
	 */
	public IWatchListColumn[] getColumns();

	/**
	 * Adds the item to the watchlist.
	 *
	 * @param item the item to add.
	 */
	public void addItem(IWatchListElement item);

	/**
	 * Adds the items to the watchlist.
	 *
	 * @param items the items to add.
	 */
	public void addItems(IWatchListElement[] items);

	/**
	 * Adds the security to the watchlist.
	 *
	 * @param security the security to add.
	 */
	public IWatchListElement addSecurity(ISecurity security);

	/**
	 * Adds the securities to the watchlist.
	 *
	 * @param securities the securities to add.
	 */
	public IWatchListElement[] addSecurities(ISecurity[] securities);

	/**
	 * Returns the number of items contained in the watchlist.
	 *
	 * @return the number of items.
	 */
	public int getItemCount();

	/**
	 * Returns a (possibly empty) array of <code>IWatchListElement</code>s which
	 * are the items in the watchlist.
	 *
	 * @return the items in the watchlist.
	 */
	public IWatchListElement[] getItems();

	/**
	 * Returns the item at the given, zero-relative index in the
	 * watchlist. Throws an exception if the index is out of range.
	 *
	 * @param index the index of the item to return.
	 * @return the item at the given index.
	 *
	 * @exception IllegalArgumentException if the index is not between 0 and the number of elements in the list minus 1 (inclusive)
	 * </ul>
	 */
	public IWatchListElement getItem(int index);

	/**
	 * Returns a (possibly empty) array of <code>IWatchListElement</code>s which
	 * are the items in the watchlist associated with the given <code>ISecurity</code>
	 * object.
	 *
	 * @param security the security to search.
	 * @return the items in the watchlist associated with the security.
	 */
	public IWatchListElement[] getItem(ISecurity security);

	/**
	 * Removes the item from the watchlist.
	 *
	 * @param item the item to remove.
	 */
	public void removeItem(IWatchListElement item);

	/**
	 * Removes the items from the watchlist.
	 *
	 * @param items the items to remove.
	 */
	public void removeItems(IWatchListElement[] items);

	/**
	 * Returns an editable representation of this watchlist.
	 *
	 * @return the editable view.
	 */
	public IView getView();

	/**
	 * Accepts the given visitor.
	 * The visitor's <code>visit</code> method is called with this
	 * watchlist. If the visitor returns <code>true</code>, this method
	 * visits this watchlist's members.
	 *
	 * @param visitor the visitor.
	 */
	public void accept(IWatchListVisitor visitor);
}
