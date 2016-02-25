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
import java.util.Optional;

final class ImmutableArrayStore<V> extends AbstractStore<V> {

	private final V[] values;
	private final int count;
	private final V nullValue;

	ImmutableArrayStore(V[] values, int count) {
		this.values = values;
		this.count = count;
		nullValue = null;
	}

	ImmutableArrayStore(V[] values, V nullValue) {
		this.values = values;
		count = Stores.countNonNulls(values);
		this.nullValue = nullValue;
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<V> valueType() {
		return (Class<V>) values.getClass().getComponentType();
	}

	@Override
	public int count() { return count; }

	@Override
	public V get(int index) { return values[index]; }

	@Override
	public boolean isNull(int index) { return values[index] == null; }

	@Override
	public Store<V> resizedCopy(int newSize) {
		return nullValue == null ?
				new NullArrayStore<>(Arrays.copyOf(values, newSize), count) :
				new ArrayStore<>(Stores.resizedCopyOf(values, newSize, nullValue), nullValue);
	}


	@Override
	public Optional<V> nullValue() {
		return Optional.ofNullable(nullValue);
	}

	// mutability

	@Override
	public Store<V> mutableCopy() {
		return nullValue == null ?
				new NullArrayStore<>(values.clone(), count) :
				new ArrayStore<>(values.clone(), nullValue);
		}

	@Override
	public Store<V> immutableCopy() {
		return nullValue == null ?
				new ImmutableArrayStore<>(values.clone(), count) :
				new ImmutableArrayStore<>(values.clone(), nullValue);
		}

	@Override
	public Store<V> immutableView() {
		return nullValue == null ?
				new ImmutableArrayStore<>(values, count):
				new ImmutableArrayStore<>(values, nullValue);
	}

}