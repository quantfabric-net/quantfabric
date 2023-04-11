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


public class VWAP extends BaseMDFeedEvent implements Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1598530420240202501L;

	public enum VWAPSides
	{
		BID,
		OFFER,
	}
	
	private int price = 0;
	private double size = 0.;
	private long snapshotId = 0L;
	private VWAPSides side;
	private int depth = 0;
	
	public VWAP()
	{
		super();
	}
	
	public VWAP(int price, double size, long snapshotId, VWAPSides side)
	{
		this();
		this.price = price;
		this.snapshotId = snapshotId;
		this.side = side;
		this.size = size;
	}
	
	
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public VWAPSides getSide()
	{
		return side;
	}

	public void setSide(VWAPSides side)
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
	public double getSize()
	{
		return size;
	}
	public void setSize(double size)
	{
		this.size = size;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		VWAP newVWAP = new VWAP(this.price, this.size, this.snapshotId, this.side);
		newVWAP.setDepth(this.depth);
		newVWAP.pupulate(this);
		return newVWAP;
	}	
}
