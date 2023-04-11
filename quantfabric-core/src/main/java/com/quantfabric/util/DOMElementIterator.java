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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Iterator over DOM nodes that positions between elements.
 */
public class DOMElementIterator implements Iterator<Element>
{
    private int index;
    private final NodeList nodeList;

    /**
     * Ctor.
     * @param nodeList is a list of DOM nodes.
     */
    public DOMElementIterator(NodeList nodeList)
    {
        this.nodeList = nodeList;
    }

    public boolean hasNext()
    {
        positionNext();
        return index < nodeList.getLength();
    }

    public Element next()
    {
        if (index >= nodeList.getLength())
        {
            throw new NoSuchElementException();
        }
        Element result = (Element) nodeList.item(index);
        index++;
        return result;
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private void positionNext()
    {
        while (index < nodeList.getLength())
        {
            Node node = nodeList.item(index);
            if (node instanceof Element)
            {
                break;
            }
            index++;
        }
    }
}
