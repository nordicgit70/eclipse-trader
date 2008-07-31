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

package org.eclipsetrader.core.trading;

import java.util.Date;

import org.eclipsetrader.core.instruments.ISecurity;

/**
 * Default implementation of the <code>IOrder</code> interface.
 *
 * <p>Clients that needs to be notified of property changes can
 * request an adapter to <code>PropertyChangeSupport</code> class.</p>
 *
 * @since 1.0
 */
public class Order implements IOrder {
	private Date date;
	private IOrderRoute route;
	private IAccount account;

	private ISecurity security;
	private Long quantity;
	private Double price;
	private Double stopPrice;

	private OrderType type;
	private OrderSide side;
	private OrderValidity validity;
	private Date expireDate;

	protected Order() {
	}

	public Order(IAccount account, OrderType type, OrderSide side, ISecurity security, Long quantity, Double price) {
	    this.account = account;
	    this.type = type;
	    this.side = side;
	    this.security = security;
	    this.quantity = quantity;
	    this.price = price;
	    this.date = new Date();
    }

	public Order(IAccount account, OrderType type, OrderSide side, ISecurity security, Long quantity, Double price, IOrderRoute route) {
	    this.account = account;
	    this.type = type;
	    this.side = side;
	    this.security = security;
	    this.quantity = quantity;
	    this.price = price;
	    this.route = route;
	    this.date = new Date();
    }

	public Order(IAccount account, OrderSide side, ISecurity security, Long quantity) {
	    this.account = account;
	    this.type = OrderType.Market;
	    this.side = side;
	    this.security = security;
	    this.quantity = quantity;
	    this.date = new Date();
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getDate()
	 */
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
    	this.date = date;
    }

	/* (non-Javadoc)
     * @see org.eclipsetrader.core.trading.IOrder#getRoute()
     */
    public IOrderRoute getRoute() {
	    return route;
    }

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getAccount()
	 */
	public IAccount getAccount() {
		return account;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getSecurity()
	 */
	public ISecurity getSecurity() {
		return security;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getQuantity()
	 */
	public Long getQuantity() {
		return quantity;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getPrice()
	 */
	public Double getPrice() {
		return price;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getSide()
	 */
	public OrderSide getSide() {
		return side;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getType()
	 */
	public OrderType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getStopPrice()
	 */
	public Double getStopPrice() {
		return stopPrice;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getValidity()
	 */
	public OrderValidity getValidity() {
		return validity;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.core.trading.IOrder#getExpire()
	 */
	public Date getExpire() {
		return expireDate;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	return "Order: date=" + getDate()
			+ ", instrument=" + getSecurity().getName()
    	    + ", type=" + getType()
			+ ", side=" + getSide()
			+ ", quantity=" + getQuantity()
			+ ", price=" + getPrice()
			+ ", stopPrice=" + getStopPrice()
			+ ", timeInForce=" + getValidity()
			+ ", expiration=" + getExpire();
    }
}