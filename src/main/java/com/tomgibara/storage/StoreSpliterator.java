/*
 * Copyright 2016 Tom Gibara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tomgibara.storage;

import java.util.Spliterator;
import java.util.function.Consumer;

// only for non-null stores
class StoreSpliterator<V> implements Spliterator<V> {

	private final Store<V> store;
	private final int chi;
	private int from;
	private int to;

	StoreSpliterator(Store<V> store) {
		this.store = store;
		if (store.nullValue().isPresent()) {
			chi = store.isMutable() ? ORDERED | SIZED | SUBSIZED | NONNULL : ORDERED | SIZED | SUBSIZED | NONNULL | IMMUTABLE;
		} else {
			chi = store.isMutable() ? ORDERED : ORDERED | IMMUTABLE;
		}
		from = 0;
		to = store.size();
	}

	StoreSpliterator(StoreSpliterator<V> splitter) {
		store = splitter.store;
		chi = splitter.chi;
		from = (splitter.from + splitter.to) >> 1;
		to = splitter.to;
		// modifies supplied splitter
		splitter.to = from;
	}

	@Override
	public boolean tryAdvance(Consumer<? super V> action) {
		if ((chi & NONNULL) != 0) {
			if (from == to) return false;
			action.accept(store.get(from++));
			return true;
		}
		while (from < to) {
			V v = store.get(from++);
			if (v != null) {
				action.accept(v);
				return true;
			}
		}
		return false;
	}

	@Override
	public Spliterator<V> trySplit() {
		if (from >= to - 1) return null;
		return new StoreSpliterator<>(this);
	}

	@Override
	public long estimateSize() {
		return to - from;
	}

	@Override
	public int characteristics() {
		return chi;
	}

}
