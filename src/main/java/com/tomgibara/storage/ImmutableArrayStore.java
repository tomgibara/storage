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

final class ImmutableArrayStore<V> extends AbstractStore<V> {

	private final V[] values;
	private final int count;
	private final StoreNullity<V> nullity;

	ImmutableArrayStore(V[] values, int count) {
		this.values = values;
		this.count = count;
		nullity = StoreNullity.settingNullAllowed();
	}

	ImmutableArrayStore(V[] values, StoreNullity<V> nullity) {
		this.values = values;
		count = nullity.countNonNulls(values);
		this.nullity = nullity;
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
		return nullity.nullGettable() ?
				new NullArrayStore<>(Arrays.copyOf(values, newSize), count) :
				new ArrayStore<>(nullity.resizedCopyOf(values, newSize), nullity);
	}

	@Override
	public StoreNullity<V> nullity() {
		return nullity;
	}

	// mutability

	@Override
	public Store<V> mutableCopy() {
		return nullity.nullGettable() ?
				new NullArrayStore<>(values.clone(), count) :
				new ArrayStore<>(values.clone(), nullity);
		}

	@Override
	public Store<V> immutableCopy() {
		return nullity.nullGettable() ?
				new ImmutableArrayStore<>(values.clone(), count) :
					//TODO seems inefficient: count already known
				new ImmutableArrayStore<>(values.clone(), nullity);
		}

	@Override
	public Store<V> immutableView() {
		return nullity.nullGettable() ?
				new ImmutableArrayStore<>(values, count):
					//TODO seems inefficient: count already known
				new ImmutableArrayStore<>(values, nullity);
	}

}