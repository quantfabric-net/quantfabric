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
package com.quantfabric.algo.market.gate.access;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.quantfabric.algo.market.gateway.access.product.Product;

public abstract class AbstractMarketDataServiceHost implements MarketDataServiceHost 
{
	private final Map<String, Product> products = new HashMap<String, Product>();
	
	@Override
	public Set<Product> getProductList() 
	{
		return new HashSet<>(products.values());
	}

	public void addProduct(Product p)
	{
		products.put(p.getProductCode(), p);
	}
	
	public Product removeProduct(Product p)
	{
		return removeProduct(p.getProductCode());
	}
	
	public Product removeProduct(String productCode)
	{
		return products.remove(productCode);
	}
	
	public Product getProduct(String productCode)
	{
		return products.get(productCode);
	}
	
	public boolean isProductExist(String productCode)
	{
		return products.containsKey(productCode);
	}
}
