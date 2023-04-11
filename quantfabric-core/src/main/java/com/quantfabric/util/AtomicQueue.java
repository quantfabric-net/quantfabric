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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A queue that allows one thread to call {@link #put(Object)} and another thread to call {@link #poll()}. Multiple threads must
 * not call these methods.
 */
public class AtomicQueue<T> {
        private final AtomicInteger writeIndex = new AtomicInteger();
        private final AtomicInteger readIndex = new AtomicInteger();
        private final AtomicReferenceArray<T> queue;

        public AtomicQueue (int capacity) {
                queue = new AtomicReferenceArray<T>(capacity);
        }

        private int next (int idx) {
                return idx + 1 & queue.length() - 1;
        }

        public boolean put (T value) {
                int write = writeIndex.get();
                int read = readIndex.get();
                int next = next(write);
                if (next == read) return false;
                queue.set(write, value);
                writeIndex.set(next);
                return true;
        }

        public T poll () {
                int read = readIndex.get();
                int write = writeIndex.get();
                if (read == write) return null;
                T value = queue.get(read);
                readIndex.set(next(read));
                return value;
        }
}