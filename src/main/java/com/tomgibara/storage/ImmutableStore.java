/*
 * Copyright 2015 Tom Gibara
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

import com.tomgibara.bits.BitStore;

final class ImmutableStore<V> extends AbstractStore<V> {

	private Store<V> store;

	ImmutableStore(Store<V> store) {
		this.store = store;
	}

	@Override
	public StoreType<V> type() {
		return store.type();
	}

	@Override
	public int size() {
		return store.size();
	}

	@Override
	public int count() {
		return store.count();
	}

	@Override
	public V get(int index) {
		return store.get(index);
	}

	@Override
	public boolean isNull(int index) {
		return store.isNull(index);
	}

	@Override
	public Store<V> resizedCopy(int newSize) {
		return store.resizedCopy(newSize);
	}

	@Override
	public Store<V> immutableView() {
		return new ImmutableStore<>(store);
	}

	@Override
	public BitStore population() {
		return store.population();
	}

}