/*
 * Copyright (c) 2004-2006 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.trading.internal.orders;

import java.text.NumberFormat;

import org.eclipse.swt.SWT;

import net.sourceforge.eclipsetrader.core.db.Order;
import net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider;

public class FilledQuantityColumn implements IOrdersLabelProvider
{
    private NumberFormat numberFormat = NumberFormat.getInstance();

    public FilledQuantityColumn()
    {
        numberFormat.setGroupingUsed(true);
        numberFormat.setMinimumIntegerDigits(1);
        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(0);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider#getHeaderText()
     */
    public String getHeaderText()
    {
        return "Filled Q.ty";
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider#getStyle()
     */
    public int getStyle()
    {
        return SWT.RIGHT;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetrader.trading.IOrdersLabelProvider#getText(net.sourceforge.eclipsetrader.core.db.Order)
     */
    public String getText(Order order)
    {
        if (order.getFilledQuantity() == 0)
            return "";
        return numberFormat.format(order.getFilledQuantity());
    }
}