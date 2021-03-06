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
import java.util.Spliterator;
import java.util.Spliterators;

class ArrayStore<V> extends AbstractStore<V> {

	private static abstract class ArrayStorage<V> implements Storage<V> {

		private final StoreType<V> type;

		ArrayStorage(StoreType<V> type) {
			this.type = type;
		}

		@Override
		public StoreType<V> type() {
			return type;
		}

	}

	static <V> Storage<V> mutableStorage(StoreType<V> type) {
		return new ArrayStorage<V>(type) {

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
			public Store<V> newStore(int size, V initialValue) throws IllegalArgumentException {
				return new ArrayStore<>(type, size, initialValue);
			}

			@Override
			@SafeVarargs
			final public Store<V> newStoreOf(V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				values = Stores.typedArrayCopy(type.valueType, values);
				type.checkValues(values);
				return new ArrayStore<>(values, type);
			}

		};
	}

	static <V> Storage<V> immutableStorage(StoreType<V> type) {
		return new ArrayStorage<V>(type) {

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
			public Store<V> newStore(int size, V initialValue) throws IllegalArgumentException {
				if (size < 0) throw new IllegalArgumentException("negative size");
				if (size == 0) return new EmptyStore<>(type, false);
				initialValue = type.checkedValue(initialValue);
				return new ConstantStore<V>(type, initialValue, size);
			}

			@Override
			@SafeVarargs
			final public Store<V> newStoreOf(V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				values = Stores.typedArrayCopy(type.valueType, values);
				type.checkValues(values);
				return new ImmutableArrayStore<>(values, type);
			}

		};
	}

	final V[] values;
	final StoreType<V> type;

	@SuppressWarnings("unchecked")
	ArrayStore(StoreType<V> type, int size, V initialValue) {
		if (initialValue == null) initialValue = type.nullValue;
		if (size > 0 && initialValue == null) StoreType.failNull();
		if (type.valueType == Object.class) {
			values = (V[]) new Object[size];
		} else try {
			values = (V[]) Array.newInstance(type.valueType, size);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size", e);
		}
		Arrays.fill(values, initialValue);
		this.type = type;
	}

	ArrayStore(V[] values, StoreType<V> type) {
		this.values = values;
		this.type = type;
	}

	@Override
	public StoreType<V> type() {
		return type;
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public int count() {
		return values.length;
	}

	@Override
	public V get(int index) {
		return values[index];
	}

	@Override
	public boolean isNull(int index) {
		return false;
	}

	@Override
	public V set(int index, V value) {
		value = type.checkedValue(value);
		V old = values[index];
		values[index] = value;
		return old;
	}

	@Override
	public void fill(V value) {
		value = type.checkedValue(value);
		Arrays.fill(values, value);
	}

	@Override
	public <W extends V> void setStore(int index, Store<W> store) {
		int thatSize = checkSetStore(index, store);
		if (thatSize == 0) {
			/* no op */
		} else if (store instanceof ConstantStore<?>) {
			Arrays.fill(this.values, index, index + thatSize, store.get(0));
		} else if (store instanceof NullConstantStore<?>) {
			Arrays.fill(this.values, index, index + thatSize, type.nullValue);
		} else if (store instanceof ArrayStore<?>) {
			ArrayStore<W> that = (ArrayStore<W>) store;
			System.arraycopy(that.values, 0, this.values, index, thatSize);
		} else if (store instanceof NullArrayStore<?>) {
			NullArrayStore<W> that = (NullArrayStore<W>) store;
			W[] thatValues = that.values;
			V[] thisValues = this.values;
			System.arraycopy(thatValues, 0, thisValues, index, thatSize);
			if (that.count < thatSize) {
				V nullValue = type.nullValue;
				for (int i = 0; i < thatSize; i++) {
					if (thatValues[i] == null) {
						thisValues[i + index] = nullValue;
					}
				}
			}
		} else {
			if (store.count() < thatSize) {
				V nullValue = type.nullValue;
				for (int i = 0; i < thatSize; i++) {
					W w = store.get(i);
					values[index + i] = w == null ? nullValue : w;
				}
			} else {
				for (int i = 0; i < thatSize; i++) {
					values[index + i] = store.get(i);
				}
			}
		}
	}

	// iterable methods

	@Override
	public Spliterator<V> spliterator() {
		return Spliterators.spliterator(values, Spliterator.ORDERED | Spliterator.NONNULL);
	}

	// mutability

	@Override
	public boolean isMutable() { return true; }

	@Override
	public Store<V> mutableCopy() { return new ArrayStore<V>(values.clone(), type); }

	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), type); }

	// abstract store methods

	@Override
	boolean fastFill(int from, int to, V value) {
		Arrays.fill(values, from, to, value);
		return true;
	}

	@Override
	boolean toArray(int from, int to, V[] vs) {
		System.arraycopy(values, from, vs, 0, to - from);
		return true;
	}
}
