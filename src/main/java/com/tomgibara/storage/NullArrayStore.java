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

import java.lang.reflect.Array;
import java.util.Arrays;

class NullArrayStore<V> extends AbstractStore<V> {

	static <V> Storage<V> mutableStorage(Class<V> type) {
		return new Storage<V>() {

			@Override
			public boolean isStorageMutable() {
				return true;
			}

			@Override
			public Storage<V> mutable() {
				return this;
			}

			@Override
			public Storage<V> immutable() {
				return immutableStorage(type);
			}

			@Override
			public Store<V> newStore(int size) throws IllegalArgumentException {
				return new NullArrayStore<>(type, size);
			}

			@Override
			public Store<V> newStoreOf(@SuppressWarnings("unchecked") V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				return new NullArrayStore<>(Stores.typedArrayCopy(type, values));
			}

		};
	}

	static <V> Storage<V> immutableStorage(Class<V> type) {
		return new Storage<V>() {

			@Override
			public boolean isStorageMutable() {
				return true;
			}

			@Override
			public Storage<V> mutable() {
				return this;
			}

			@Override
			public Storage<V> immutable() {
				return immutableStorage(type);
			}

			@Override
			public Store<V> newStore(int size) throws IllegalArgumentException {
				//TODO need constant store
				return new NullArrayStore<>(type, size).immutableView();
			}

			@Override
			public Store<V> newStoreOf(@SuppressWarnings("unchecked") V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				return new ImmutableArrayStore<>(Stores.typedArrayCopy(type, values), Stores.countNonNulls(values));
			}

		};
	}

	static final Storage<Object> mutableObjectStorage = mutableStorage(Object.class);

	final V[] values;
	int count;

	@SuppressWarnings("unchecked")
	NullArrayStore(Class<V> type, int size) {
		try {
			values = (V[]) Array.newInstance(type, size);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size", e);
		}
		this.count = 0;
	}

	NullArrayStore(V[] values) {
		this.values = values;
		count = Stores.countNonNulls(values);
	}

	NullArrayStore(V[] values, int count) {
		this.values = values;
		this.count = count;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<V> valueType() {
		return (Class<V>) values.getClass().getComponentType();
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public V get(int index) {
		return values[index];
	}

	@Override
	public boolean isNull(int index) {
		return values[index] == null;
	}

	@Override
	public V set(int index, V value) {
		V old = values[index];
		values[index] = value;
		if (old != null) count --;
		if (value != null) count ++;
		return old;
	}

	@Override
	public void clear() {
		Arrays.fill(values, null);
		count = 0;
	}

	@Override
	public void fill(V value) {
		Arrays.fill(values, value);
		count = value == null ? 0 : values.length;
	}

	@Override
	public boolean compact() {
		return Stores.compact(values, count);
	}

	// mutability

	@Override
	public boolean isMutable() { return true; }

	@Override
	public Store<V> mutableCopy() { return new NullArrayStore<>(values.clone(), count); }

	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), count); }

}
