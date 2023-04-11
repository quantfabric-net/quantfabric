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
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField.AbstractFormatter;

public class MarketValueFormatter extends AbstractFormatter
{
	private static final long serialVersionUID = 3334590594758760446L;

	@Override
	public Object stringToValue(String text) throws ParseException
	{	
		String strValue = text.trim();
		
		if (strValue.endsWith("K") || strValue.endsWith("k"))
			return getNumericPart(strValue) * 1000;
		else
			if (strValue.endsWith("M") || strValue.endsWith("m"))
				return getNumericPart(strValue) * 1000000;
			else
				if (strValue.endsWith("B") || strValue.endsWith("b"))
					return getNumericPart(strValue) * 1000000000;
		
		return Double.parseDouble(strValue);
	}
	
	private static double getNumericPart(String text) throws NumberFormatException
	{
		return Double.parseDouble(text.substring(0, text.length()-1));
	}

	@Override
	public String valueToString(Object value) throws ParseException
	{
		if (value != null)
			return valueToString(((Number)value).doubleValue());
		else
			return null;
	}

	public String valueToString(Double value) throws ParseException
	{
		String valueView = "";
		
		if (value < 1000)
			valueView = doubleToString(value);
		else
			if (value < 1000000)
				valueView = doubleToString(value / 1000) + "K";
			else
				if (value < 1000000000)
					valueView = doubleToString(value / 1000000) + "M";
				else
					valueView = doubleToString(value / 1000000) + "M";
		return valueView;
	}
	
	private String doubleToString(Double value)
	{
		return DecimalFormat.getInstance().format(value);
		//return value.toString();
	}
	
}
