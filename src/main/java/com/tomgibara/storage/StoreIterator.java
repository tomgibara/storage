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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.tomgibara.bits.BitStore.Positions;

abstract class StoreIterator<V,W> implements Iterator<W> {

	final Store<V> store;
	private final Positions positions;
	private int previous = -1;

	private StoreIterator(Store<V> store) {
		this.store = store;
		this.positions = store.population().ones().positions();
	}

	@Override
	public boolean hasNext() {
		return positions.hasNext();
	}

	@Override
	public W next() {
		if (!positions.hasNext()) throw new NoSuchElementException();
		previous = positions.nextPosition();
		return get(previous);
	}

	@Override
	public void remove() {
		if (previous == -1) throw new NoSuchElementException();
		W value = get(previous);
		if (value == null) throw new IllegalStateException();
		set(previous, null);
	}

	@Override
	public void forEachRemaining(Consumer<? super W> action) {
		while (positions.hasNext()) {
			action.accept(get(positions.nextPosition()));
		}
	}

	abstract W get(int index);

	abstract void set(int index, W v);

	static final class Regular<V> extends StoreIterator<V,V> {

		Regular(Store<V> store) { super(store); }
		@Override V get(int index) { return store.get(index); }
		@Override void set(int index, V v) { store.set(index, v); }

	}

	static final class Transformed<V,W> extends StoreIterator<V,W> {

		private final Function<V,W> fn;

		Transformed(Store<V> store, Function<V,W> fn) {
			super(store);
			this.fn = fn;
		}

		@Override
		W get(int index) {
			return fn.apply(store.get(index));
		}

		@Override
		void set(int index, W v) {
			if (v != null) throw new IllegalArgumentException("non-null v");
			store.set(index, null);
		}

	}

	static final class BiTransformed<V,W> extends StoreIterator<V,W> {

		private final BiFunction<Integer, V,W> fn;

		BiTransformed(Store<V> store, BiFunction<Integer, V,W> fn) {
			super(store);
			this.fn = fn;
		}

		@Override
		W get(int index) {
			return fn.apply(index, store.get(index));
		}

		@Override
		void set(int index, W v) {
			if (v != null) throw new IllegalArgumentException("non-null v");
			store.set(index, null);
		}

	}
}
