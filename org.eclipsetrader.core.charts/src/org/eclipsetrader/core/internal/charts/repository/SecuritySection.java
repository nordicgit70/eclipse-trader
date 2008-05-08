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

package org.eclipsetrader.core.internal.charts.repository;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipsetrader.core.charts.repository.IChartVisitor;
import org.eclipsetrader.core.charts.repository.IIndicatorSection;
import org.eclipsetrader.core.charts.repository.ISecuritySection;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.internal.markets.SecurityAdapter;

@XmlRootElement(name = "security")
@SuppressWarnings("restriction")
public class SecuritySection implements ISecuritySection {
    @XmlAttribute(name = "uri")
	@XmlJavaTypeAdapter(SecurityAdapter.class)
	private ISecurity security;

	@XmlElement(name = "indicator")
	@XmlJavaTypeAdapter(IndicatorSectionAdapter.class)
	private List<IIndicatorSection> indicators;

	protected SecuritySection() {
	}

	protected SecuritySection(ISecuritySection section) {
	    this.security = section.getSecurity();
    	this.indicators = section.getIndicators() != null ? Arrays.asList(section.getIndicators()) : null;
    }

	public SecuritySection(ISecurity security) {
	    this.security = security;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.ISecuritySection#getSecurity()
     */
	@XmlTransient
	public ISecurity getSecurity() {
    	return security;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.ISecuritySection#getIndicators()
     */
	@XmlTransient
	public IIndicatorSection[] getIndicators() {
    	return indicators.toArray(new IndicatorSection[indicators.size()]);
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.ISecuritySection#setIndicators(org.eclipsetrader.core.charts.repository.IIndicatorSection[])
     */
    public void setIndicators(IIndicatorSection[] indicators) {
    	this.indicators = indicators != null ? Arrays.asList(indicators) : null;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.charts.repository.ISecuritySection#accept(org.eclipsetrader.core.charts.repository.IChartVisitor)
     */
	public void accept(IChartVisitor visitor) {
		if (visitor.visit(this)) {
			IIndicatorSection[] d = getIndicators();
			for (int i = 0; i < d.length; i++)
				d[i].accept(visitor);
		}
	}
}
