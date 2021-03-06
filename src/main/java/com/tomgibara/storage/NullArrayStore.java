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

// must be null allowed
class NullArrayStore<V> extends AbstractStore<V> {

	static <V> Storage<V> mutableStorage(StoreType<V> type) {
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
			public StoreType<V> type() {
				return type;
			}

			@Override
			public Store<V> newStore(int size, V value) throws IllegalArgumentException {
				return new NullArrayStore<>(type, size, value);
			}

			@Override
			@SafeVarargs
			final public Store<V> newStoreOf(V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				return new NullArrayStore<>(Stores.typedArrayCopy(type.valueType, values));
			}

		};
	}

	static <V> Storage<V> immutableStorage(StoreType<V> type) {
		return new Storage<V>() {

			@Override
			public boolean isStorageMutable() {
				return false;
			}

			@Override
			public Storage<V> mutable() {
				return mutableStorage(type);
			}

			@Override
			public Storage<V> immutable() {
				return this;
			}

			@Override
			public StoreType<V> type() {
				return type;
			}

			@Override
			public Store<V> newStore(int size, V value) throws IllegalArgumentException {
				if (size < 0L) throw new IllegalArgumentException("negative size");
				if (size == 0) return new EmptyStore<V>(type, false);
				return value == null ?
					new NullConstantStore<V>(type, size) :
					new ConstantStore<V>(type, value, size);
			}

			@Override
			@SafeVarargs
			final public Store<V> newStoreOf(V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				return new ImmutableArrayStore<>(Stores.typedArrayCopy(type.valueType, values), type);
			}

		};
	}

	static final Storage<Object> mutableObjectStorage = mutableStorage(StoreType.generic());

	final V[] values;
	int count;

	@SuppressWarnings("unchecked")
	NullArrayStore(StoreType<V> type, int size, V initialValue) {
		try {
			values = (V[]) Array.newInstance(type.valueType, size);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size", e);
		}
		if (initialValue == null) {
			count = 0;
		} else {
			Arrays.fill(values, initialValue);
			count = size;
		}
	}

	NullArrayStore(V[] values) {
		this.values = values;
		count = type().countNonNulls(values);
	}

	NullArrayStore(V[] values, int count) {
		this.values = values;
		this.count = count;
	}

	@Override
	public StoreType<V> type() {
		return StoreType.of(Stores.componentType(values));
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

	@Override
	public <W extends V> void setStore(int index, Store<W> store) {
		int thatSize = checkSetStore(index, store);
		if (thatSize == 0) {
			/* no op */
		} else if (store instanceof ConstantStore<?>) {
			count -= count(index, thatSize);
			Arrays.fill(this.values, index, index + thatSize, store.get(0));
			count += thatSize;
		} else if (store instanceof NullConstantStore<?>) {
			count -= count(index, thatSize);
			Arrays.fill(this.values, index, index + thatSize, null);
		} else if (store instanceof ArrayStore<?>) {
			count -= count(index, thatSize);
			ArrayStore<W> that = (ArrayStore<W>) store;
			System.arraycopy(that.values, 0, this.values, index, thatSize);
			count += thatSize;
		} else if (store instanceof NullArrayStore<?>) {
			count -= count(index, thatSize);
			NullArrayStore<W> that = (NullArrayStore<W>) store;
			System.arraycopy(that.values, 0, this.values, index, thatSize);
			count += that.count();
		} else {
			for (int i = 0; i < thatSize; i++) {
				W w = store.get(i);
				V v = values[index + i];
				if (v == w) continue;
				if (w == null) {
					count--;
				} else if (v == null) {
					count++;
				}
				values[index + i] = w;
			}
		}
	}

	// mutability

	@Override
	public boolean isMutable() { return true; }

	@Override
	public Store<V> mutableCopy() { return new NullArrayStore<>(values.clone(), count); }

	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), count, type()); }

	// abstract store methods

	@Override
	boolean fastFill(int from, int to, V value) {
		if (from == 0 && to == values.length) {
			Arrays.fill(values, value);
			count = value == null ? 0 : values.length;
		} else {
			int length = to - from;
			count -= count(from, length);
			Arrays.fill(values, from, to, value);
			if (value != null) count += length;
		}
		return true;
	}

	@Override
	boolean toArray(int from, int to, V[] vs) {
		System.arraycopy(values, from, vs, 0, to - from);
		return true;
	}

	// private helper methods

	private int count(int index, int length) {
		int limit = index + length;
		int count = 0;
		for (int i = index; i < limit; i++) {
			if (values[i] != null) count++;
		}
		return count;
	}
}
