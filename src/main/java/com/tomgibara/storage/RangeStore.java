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

import java.util.List;

import com.tomgibara.bits.BitStore;

final class RangeStore<V> extends AbstractStore<V> {

	final Store<V> store;
	final int from;
	final int to;

	RangeStore(Store<V> store, int from, int to) {
		this.store = store;
		this.from = from;
		this.to = to;
	}

	@Override
	public int size() {
		return to - from;
	}

	@Override
	public V get(int index) {
		return store.get(from + index);
	}

	@Override
	public boolean isNull(int index) {
		return store.isNull(from + index);
	}

	@Override
	public StoreType<V> type() {
		return store.type();
	}

	@Override
	public V set(int index, V value) {
		return store.set(from + index, value);
	}

	@Override
	public boolean isSettable(Object value) {
		return store.isSettable(value);
	}

	@Override
	public BitStore population() {
		return store.population().range(from, to);
	}

	@Override
	public Store<V> range(int from, int to) {
		return store.range(this.from + from, this.from + to);
	}

	@Override
	public List<V> asList() {
		return store.asList().subList(from, to);
	}

	@Override
	public <W extends V> void setStore(int position, Store<W> store) {
		this.store.setStore(from + position, store);
	}

	// mutable methods

	@Override
	public Store<V> immutableView() {
		return isMutable() ? store.immutableView().range(from, to) : this;
	}

}
