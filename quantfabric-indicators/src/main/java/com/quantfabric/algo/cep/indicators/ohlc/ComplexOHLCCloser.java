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
package com.quantfabric.algo.cep.indicators.ohlc;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.quantfabric.algo.market.datamodel.ComplexAccumulatedOHLC;
import com.quantfabric.algo.market.datamodel.OHLCUpdate;

public class ComplexOHLCCloser extends OHLCCalculator {

	private ComplexAccumulatedOHLC complexOHLC = null;
	
	public ComplexOHLCCloser(String period) {
		
		super(period);
	}
	
	public ComplexOHLCCloser(String period, int timeOffSet) {
		
		super(period, timeOffSet);
	}
	
	public synchronized void update(Date timestamp, OHLCUpdate ohlcUpdate) throws Exception {

		if (complexOHLC == null) {
			init(timestamp, ohlcUpdate.getSnapshotId());
		}

		if (timestamp.compareTo(this.currentTimeframeInterval.getEnd()) >= 0) {

			complexOHLC.close(false);
			postCurrentState();

			init(timestamp, ohlcUpdate.getSnapshotId());

			try {
				setupTimeout(calculateDelay(currentTimeframeInterval.getEnd(), timestamp));
			}
			catch (Exception e) {
				System.out.printf("ERROR (TF %s). Inconsistency in time intervals [%s, %s]. Exception message: %s%n", timeframe,
						currentTimeframeInterval.getEnd(), timestamp, e.getMessage());
			}
		}

		complexOHLC.update(ohlcUpdate);

		postCurrentState();
	}
	
	@Override
	protected void setupTimeout(long delay)
	{
		if (timer != null)
			timer.cancel();

		timer = new Timer(true);
		
		timer.schedule(
				new TimerTask() 
				{
					@Override
					public void run()
					{
						complexOHLC.close(true);
						postCurrentState();
						complexOHLC = null;
					}
				}, delay);
	}
	
	protected void init(Date timestamp, long snapshotId) {
		
		super.init(timestamp);
		complexOHLC = new ComplexAccumulatedOHLC(snapshotId);
	}
	
	@Override
	protected void postCurrentState()
	{		
		if (listener != null)
		{
			synchronized (this)
			{
				listener.update(complexOHLC.clone());
			}
		}
	}
}
