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

package org.eclipsetrader.core.feed;

import java.util.Date;

/**
 * Level II Book proposal entry.
 *
 * @since 1.0
 */
public interface IBookEntry {

	public Long getProposals();

	public Long getQuantity();

	public Double getPrice();

	public String getMarketMaker();

	public Date getTime();
}