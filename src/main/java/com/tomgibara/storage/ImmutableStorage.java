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


final class ImmutableStorage<V> implements Storage<V> {

	// mutable
	private final Storage<V> storage;

	ImmutableStorage(Storage<V> storage) {
		this.storage = storage;
	}

	@Override
	public boolean isStorageMutable() { return false; }

	@Override
	public Storage<V> mutable() { return storage; }

	@Override
	public Storage<V> immutable() { return this; }

	@Override
	public StoreType<V> type() { return storage.type(); }

	@Override
	public Store<V> newStore(int size, V value) throws IllegalArgumentException {
		StoreType<V> type = storage.type();
		if (size == 0) return new EmptyStore<>(type, false);
		if (!type.nullSettable) StoreType.failNull();
		return type.nullGettable ?
				new NullConstantStore<>(type, size) :
				new ConstantStore<>(type, value, size);
	}

	@Override
	@SafeVarargs
	// varargs safety assumes safey of delegate
	final public Store<V> newStoreOf(V... values) {
		return storage.newStoreOf(values).immutableView();
	}

	@Override
	public Store<V> newCopyOf(Store<V> store) {
		return storage.newCopyOf(store).immutableView();
	}
}
