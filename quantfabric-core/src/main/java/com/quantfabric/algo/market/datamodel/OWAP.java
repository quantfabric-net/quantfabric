/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quantfabric.algo.market.datamodel;


public class OWAP extends BaseMDFeedEvent implements Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -715869442352435801L;

	public enum OWAPSides
	{
		BID,
		OFFER,
	}
	
	private int price = 0;
	private int amountOrders = 0;
	private long snapshotId = 0L;
	private OWAPSides side;
	
	public OWAP()
	{
		super();
	}
	
	public OWAP(int price, int amountOrders, long snapshotId, OWAPSides side)
	{
		this();
		this.price = price;
		this.snapshotId = snapshotId;
		this.side = side;
		this.amountOrders = amountOrders;
	}
	
	
	public OWAPSides getSide()
	{
		return side;
	}

	public void setSide(OWAPSides side)
	{
		this.side = side;
	}

	public int getPrice()
	{
		return price;
	}
	public void setPrice(int price)
	{
		this.price = price;
	}
	public long getSnapshotId()
	{
		return snapshotId;
	}
	public void setSnapshotId(long snapshotId)
	{
		this.snapshotId = snapshotId;
	}
	public int getAmountOrders()
	{
		return amountOrders;
	}

	public void setAmountOrders(int amountOrders)
	{
		this.amountOrders = amountOrders;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		OWAP newOWAP = new OWAP(this.price, this.amountOrders, this.snapshotId, this.side);
		newOWAP.pupulate(this);
		return newOWAP;
	}	
}
