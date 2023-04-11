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
package com.quantfabric.util;

import java.util.ArrayList;
import java.util.List;

import com.quantfabric.util.LatencyMeasurer.Measure.MeasureTypes;

public class LatencyMeasurer
{	
	public static class Measure
	{
		public enum MeasureTypes
		{
			START_POINT,
			INTERIM_POINT,			
			END_POINT,
			SEPARATE_POINT
		}
		
		public enum TimestampUnits
		{
			NANOSECONDS,
			MILLISECONDS
		}
		
		private final MeasureTypes measureType;
		private long timestamp;
		private final TimestampUnits timestampUnit;
		private final String description;
		
		public Measure(MeasureTypes measureType, String description, TimestampUnits timestampUnit)
		{
			super();
			this.measureType = measureType;
			this.timestampUnit = timestampUnit;
			this.description = description; 
			this.snapshot();
		}
		
		public Measure(MeasureTypes measureType, String description)
		{
			this(measureType, description, TimestampUnits.NANOSECONDS);
		}
		
		public Measure(MeasureTypes measureType, String description,
				TimestampUnits timestampUnit, long timestamp)
		{
			super();
			this.measureType = measureType;
			this.timestampUnit = timestampUnit;
			this.description = description; 
			this.timestamp = timestamp;
		}

		public MeasureTypes getMeasureType()
		{
			return measureType;
		}
		public long getTimestamp()
		{
			return timestamp;
		}
		public String getDescription()
		{
			return description;
		}

		public long getTimestamp(TimestampUnits timestampUnit)
		{
			if (getTimestampUnit() != timestampUnit)
			{
				switch (getTimestampUnit())
				{
					case MILLISECONDS:
						return getTimestamp() * 1000000;
					case NANOSECONDS:
						return getTimestamp() / 1000000;
					default: 
						return 0;
				}
			}
			return timestamp;
		}		
		public TimestampUnits getTimestampUnit()
		{
			return timestampUnit;
		}
		public void snapshot()
		{
			if (getTimestampUnit() == TimestampUnits.NANOSECONDS)
				timestamp = System.nanoTime();
			else
				if (getTimestampUnit() == TimestampUnits.MILLISECONDS)
					timestamp = System.currentTimeMillis();
		}
	}
	
	private List<Measure> measurements;
	
	public LatencyMeasurer()
	{
		super();
		resetMeasurements();
	}

	public void resetMeasurements()
	{
		measurements = new ArrayList<Measure>();
	}
	
	public void createPoint(Measure measure)
	{
		measurements.add(measure);
	}
	
	public void createStartPoint(String description)
	{
		measurements.add(new Measure(MeasureTypes.START_POINT, description));
	}
	
	public void createInterimePoint(String description)
	{
		measurements.add(new Measure(MeasureTypes.INTERIM_POINT, description));
	}
	
	public void createEndPoint(String description)
	{
		measurements.add(new Measure(MeasureTypes.END_POINT, description));
	}
	
	public List<Measure> getMeasurements()
	{
		return measurements;
	}	
}
