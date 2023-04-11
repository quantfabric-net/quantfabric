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
package com.quantfabric.algo.cep.indicators.util.movingwindow;

public class MovingWindow
{
	private final double[] elements;
	private int start, end;
	protected boolean isFull;
	private final int capacity;

	public MovingWindow(int capacity)
	{
		this.capacity = capacity;
		elements = new double[capacity];
	}

	public int getCapacity()
	{
		return capacity;
	}

	public double get(int position)
	{
		int index = (start + position - 1) % capacity;
		if (index < 0)
		{
			index = capacity + index;
		}

		return elements[index];
	}

	public double getFirst()
	{
		return get(0);
	}

	public double getLast()
	{
		return get(capacity - 1);
	}

	public void add(double value)
	{
		elements[end] = value;
		end = (end + 1) % capacity;
		if (end == start)
		{
			start = (start + 1) % capacity;
			isFull = true;
		}
	}

	public boolean isFull()
	{
		return isFull;
	}

	public void clear()
	{
		isFull = false;
		start = end = 0;
		for (int index = 0; index < capacity; index++)
		{
			elements[index] = 0;
		}
	}
}
