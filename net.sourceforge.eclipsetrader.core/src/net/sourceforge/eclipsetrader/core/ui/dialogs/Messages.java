/*
 * Copyright (c) 2004-2007 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package net.sourceforge.eclipsetrader.core.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "net.sourceforge.eclipsetrader.core.ui.dialogs.messages"; //$NON-NLS-1$

    private Messages()
    {
    }

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }
    public static String CurrencyConversionDialog_Title;
    public static String CurrencyConversionDialog_Convert;
    public static String CurrencyConversionDialog_To;
    public static String CurrencyConversionDialog_Equal;
    public static String EventDetailsDialog_Title;
    public static String EventDetailsDialog_Date;
    public static String EventDetailsDialog_Security;
    public static String EventDetailsDialog_Message;
    public static String EventDetailsDialog_Details;
    public static String ExchangeRateDialog_Date;
    public static String ExchangeRateDialog_DefaultFrom;
    public static String ExchangeRateDialog_DefaultTo;
    public static String ExchangeRateDialog_Equals;
    public static String ExchangeRateDialog_Title;
    public static String ExchangeRateDialog_To;
	public static String FeedSelectionDialog_Description;
	public static String FeedSelectionDialog_DialogTitle;
	public static String FeedSelectionDialog_ShellTitle;
	public static String IntradayChartsDialog_Description;
	public static String IntradayChartsDialog_DialogTitle;
	public static String IntradayChartsDialog_ShellTitle;
	public static String TradingOptionsDialog_Description;
	public static String TradingOptionsDialog_DialogTitle;
	public static String TradingOptionsDialog_ShellTitle;
}
