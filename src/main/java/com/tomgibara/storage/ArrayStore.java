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

		private final Class<V> type;
		private final StoreNullity<V> nullity;

		ArrayStorage(Class<V> type, StoreNullity<V> nullity) {
			this.type = type;
			this.nullity = nullity;
		}

		@Override
		public StoreNullity<V> nullity() {
			return nullity;
		}

		@Override
		public Class<V> valueType() {
			return type;
		}

	}

	static <V> Storage<V> mutableStorage(Class<V> type, StoreNullity<V> nullity) {
		return new ArrayStorage<V>(type, nullity) {

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
				return immutableStorage(type, nullity);
			}

			@Override
			public Store<V> newStore(int size) throws IllegalArgumentException {
				return new ArrayStore<>(type, size, nullity);
			}

			@Override
			@SafeVarargs
			final public Store<V> newStoreOf(V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				values = Stores.typedArrayCopy(type, values);
				nullity.checkValues(values);
				return new ArrayStore<>(values, nullity);
			}

		};
	}

	static <V> Storage<V> immutableStorage(Class<V> type, StoreNullity<V> nullity) {
		return new ArrayStorage<V>(type, nullity) {

			@Override
			public boolean isStorageMutable() {
				return false;
			}

			@Override
			public Storage<V> mutable() {
				return mutableStorage(type, nullity);
			}

			@Override
			public Storage<V> immutable() {
				return this;
			}

			@Override
			public Store<V> newStore(int size) throws IllegalArgumentException {
				if (size < 0) throw new IllegalArgumentException("negative size");
				if (nullity.nullSettable()) return new ConstantStore<V>(type, nullity.nullValue(), size);
				if (size == 0) return new EmptyStore<>(type, false);
				throw new IllegalArgumentException("null disallowed");
			}

			@Override
			@SafeVarargs
			final public Store<V> newStoreOf(V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				values = Stores.typedArrayCopy(type, values);
				nullity.checkValues(values);
				return new ImmutableArrayStore<>(values, nullity);
			}

		};
	}

	final V[] values;
	final StoreNullity<V> nullity;

	@SuppressWarnings("unchecked")
	ArrayStore(Class<V> type, int size, StoreNullity<V> nullity) {
		V nullValue = nullity.nullValue();
		if (size > 0 && nullValue == null) throw new IllegalArgumentException("nullity has no nullValue");
		if (type == Object.class) {
			values = (V[]) new Object[size];
		} else try {
			values = (V[]) Array.newInstance(type, size);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size", e);
		}
		Arrays.fill(values, nullValue);
		this.nullity = nullity;
	}

	ArrayStore(V[] values, StoreNullity<V> nullity) {
		this.values = values;
		this.nullity = nullity;
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
		value = nullity.checkedValue(value);
		V old = values[index];
		values[index] = value;
		return old;
	}

	@Override
	public void fill(V value) {
		value = nullity.checkedValue(value);
		Arrays.fill(values, value);
	}

	@Override
	public StoreNullity<V> nullity() {
		return nullity;
	}

	@Override
	public Spliterator<V> spliterator() {
		return Spliterators.spliterator(values, Spliterator.ORDERED | Spliterator.NONNULL);
	}

	// mutability

	@Override
	public boolean isMutable() { return true; }

	@Override
	public Store<V> mutableCopy() { return new ArrayStore<V>(values.clone(), nullity); }

	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), nullity); }

}
