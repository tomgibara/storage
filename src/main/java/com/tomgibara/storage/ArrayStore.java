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
import java.util.Optional;

class ArrayStore<V> extends AbstractStore<V> {

	private static abstract class ArrayStorage<V> implements Storage<V> {

		private final Class<V> type;
		private final Optional<V> nullValue;

		ArrayStorage(Class<V> type, V nullValue) {
			this.type = type;
			this.nullValue = Optional.ofNullable(nullValue);
		}

		@Override
		public Optional<V> nullValue() {
			return nullValue;
		}

		@Override
		public Class<V> valueType() {
			return type;
		}

	}

	static <V> Storage<V> mutableStorage(Class<V> type, V nullValue) {
		return new ArrayStorage<V>(type, nullValue) {

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
				return immutableStorage(type, nullValue);
			}

			@Override
			public Store<V> newStore(int size) throws IllegalArgumentException {
				return new ArrayStore<>(type, size, nullValue);
			}

			@Override
			public Store<V> newStoreOf(@SuppressWarnings("unchecked") V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				values = Stores.typedArrayCopy(type, values);
				Stores.replaceNulls(values, nullValue);
				return new ArrayStore<>(values, nullValue);
			}

		};
	}

	static <V> Storage<V> immutableStorage(Class<V> type, V nullValue) {
		return new ArrayStorage<V>(type, nullValue) {

			@Override
			public boolean isStorageMutable() {
				return false;
			}

			@Override
			public Storage<V> mutable() {
				return mutableStorage(type, nullValue);
			}

			@Override
			public Storage<V> immutable() {
				return this;
			}

			@Override
			public Store<V> newStore(int size) throws IllegalArgumentException {
				if (size < 0L) throw new IllegalArgumentException("negative size");
				return new ConstantStore<V>(type, nullValue, size);
			}

			@Override
			public Store<V> newStoreOf(@SuppressWarnings("unchecked") V... values) {
				if (values == null) throw new IllegalArgumentException("null values");
				values = Stores.typedArrayCopy(type, values);
				Stores.replaceNulls(values, nullValue);
				return new ImmutableArrayStore<>(values, nullValue);
			}

		};
	}

	final V[] values;
	final V nullValue;

	@SuppressWarnings("unchecked")
	ArrayStore(Class<V> type, int size, V nullValue) {
		if (nullValue == null) throw new IllegalArgumentException("null initialValue");
		if (type == Object.class) {
			values = (V[]) new Object[size];
		} else try {
			values = (V[]) Array.newInstance(type, size);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size", e);
		}
		Arrays.fill(values, nullValue);
		this.nullValue = nullValue;
	}

	ArrayStore(V[] values, V nullValue) {
		this.values = values;
		this.nullValue = nullValue;
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
		if (value == null) value = nullValue;
		V old = values[index];
		values[index] = value;
		return old;
	}

	@Override
	public void fill(V value) {
		if (value == null) value = nullValue;
		Arrays.fill(values, value);
	}

	@Override
	public Optional<V> nullValue() {
		return Optional.of(nullValue);
	}

	// mutability

	@Override
	public boolean isMutable() { return true; }

	@Override
	public Store<V> mutableCopy() { return new ArrayStore<V>(values.clone(), nullValue); }

	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), values.length); }

}
