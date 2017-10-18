/*
 * Copyright 2017 Tom Gibara
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

import static com.tomgibara.storage.Stores.immutableException;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.List;
import java.util.function.Consumer;

class StoreList<V> extends AbstractList<V> {

	private final Store<V> store;

	StoreList(Store<V> store) {
		this.store = store;
	}

	@Override
	public V get(int index) {
		return store.get(index);
	}

	@Override
	public V set(int index, V element) {
		if (!store.isMutable()) throw immutableException();
		return store.set(index, element);
	}

	@Override
	public void clear() {
		if (!store.isMutable()) throw immutableException();
		store.clear();
	}

	@Override
	public boolean add(V e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return store.size();
	}

	@Override
	public List<V> subList(int fromIndex, int toIndex) {
		return store.range(fromIndex, toIndex).asList();
	}

	@Override
	public void forEach(Consumer<? super V> action) {
		int size = size();
		for (int i = 0; i < size; i++) {
			action.accept(get(i));
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// gather info
		Class<?> t = a.getClass().getComponentType();
		int s = store.size();

		// ensure array is sufficiently sized
		T[] as = a.length < s ? (T[]) Array.newInstance(t, s) : a;

		// attempt a faster path
		if (store instanceof AbstractStore && store.type().isAssignableTo(t)) {
			// hacky way around Java typing
			AbstractStore<T> abs = (AbstractStore<T>) store;
			if (abs.toArray(0, s, a)) return a;
		}

		store.forEach((i,v) -> as[i] = (T) v);
		return as;
	}

}
