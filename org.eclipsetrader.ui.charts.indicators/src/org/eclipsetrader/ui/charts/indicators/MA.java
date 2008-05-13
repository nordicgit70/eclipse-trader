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

package org.eclipsetrader.ui.charts.indicators;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.RGB;
import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.charts.NumericDataSeries;
import org.eclipsetrader.ui.charts.IChartIndicator;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.charts.MAType;
import org.eclipsetrader.ui.charts.OHLCField;
import org.eclipsetrader.ui.charts.RenderStyle;
import org.eclipsetrader.ui.internal.charts.Util;
import org.eclipsetrader.ui.internal.charts.indicators.Activator;
import org.eclipsetrader.ui.internal.charts.indicators.SingleLineElement;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class MA implements IChartIndicator, IExecutableExtension {
    private String id;
    private String name;

	public MA() {
	}

	/* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
    	id = config.getAttribute("id");
    	name = config.getAttribute("name");
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.charts.ui.indicators.IChartIndicator#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
     * @see org.eclipsetrader.ui.charts.model.IChartIndicator#computeElement(org.eclipse.core.runtime.IAdaptable, org.eclipsetrader.ui.charts.model.IChartParameters)
     */
    public IAdaptable computeElement(IAdaptable source, IChartParameters parameters) {
		IDataSeries dataSeries = (IDataSeries) source.getAdapter(IDataSeries.class);
		if (dataSeries != null) {
		    OHLCField field = parameters.hasParameter("field") ? OHLCField.getFromName(parameters.getString("field")) : OHLCField.Close;
		    int period = parameters.getInteger("period");
		    MAType type = MAType.getFromName(parameters.getString("type"));

		    RenderStyle style = parameters.hasParameter("style") ? RenderStyle.getStyleFromName(parameters.getString("style")) : RenderStyle.Line;
		    RGB color = parameters.getColor("color");

			IAdaptable[] values = dataSeries.getValues();
			Core core = Activator.getDefault() != null ? Activator.getDefault().getCore() : new Core();

		    int startIdx = 0;
	        int endIdx = values.length - 1;
			double[] inReal = Util.getValuesForField(values, field);

			MInteger outBegIdx = new MInteger();
	        MInteger outNbElement = new MInteger();
	        double[] outReal = new double[values.length - core.movingAverageLookback(period, MAType.getTALib_MAType(type))];

	        core.movingAverage(startIdx, endIdx, inReal, period, MAType.getTALib_MAType(type), outBegIdx, outNbElement, outReal);

			IDataSeries result = new NumericDataSeries(getName(), outReal, dataSeries);
			return new SingleLineElement(result, style, color);
		}

		return null;
    }
}