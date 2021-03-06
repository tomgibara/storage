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

import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;

final class ImmutableArrayStore<V> extends AbstractStore<V> {

	private final V[] values;
	private final int count;
	private final StoreType<V> type;

	ImmutableArrayStore(V[] values, int count, StoreType<V> type) {
		this.values = values;
		this.count = count;
		this.type = type;
	}

	ImmutableArrayStore(V[] values, StoreType<V> type) {
		this.values = values;
		count = type.countNonNulls(values);
		this.type = type;
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public StoreType<V> type() {
		return type;
	}

	@Override
	public int count() { return count; }

	@Override
	public V get(int index) { return values[index]; }

	@Override
	public boolean isNull(int index) { return values[index] == null; }

	@Override
	public Store<V> resizedCopy(int newSize) {
		return type.nullGettable ?
				new NullArrayStore<>(Arrays.copyOf(values, newSize), count) :
				new ArrayStore<>(type.resizedCopyOf(values, newSize), type);
	}

	@Override
	public Spliterator<V> spliterator() {
		return type.nullGettable ?
				new StoreSpliterator<>(this) :
				Spliterators.spliterator(values, Spliterator.ORDERED | Spliterator.NONNULL);
	}

	// mutability

	@Override
	public Store<V> mutableCopy() {
		return type.nullGettable ?
				new NullArrayStore<>(copiedValues(), count) :
				new ArrayStore<>(copiedValues(), type);
		}

	@Override
	public Store<V> immutableCopy() {
		return new ImmutableArrayStore<>(copiedValues(), count, type);
	}

	@Override
	public Store<V> immutableView() {
		return new ImmutableArrayStore<>(copiedValues(), count, type);
	}

	// package scoped methods

	@Override
	boolean toArray(int from, int to, V[] vs) {
		System.arraycopy(values, from, vs, 0, to - from);
		return true;
	}

	// private utility methods

	// we are cautious here because this store may wrap a more specifically typed array
	private V[] copiedValues() {
		return Stores.typedArrayCopy(type.valueType, values);
	}

}