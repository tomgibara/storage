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


final class MutableStorage<V> implements Storage<V> {

	// immutable
	private final Storage<V> storage;

	MutableStorage(Storage<V> storage) {
		this.storage = storage;
	}

	@Override
	public boolean isStorageMutable() { return true; }

	@Override
	public Storage<V> mutable() { return this; }

	@Override
	public Storage<V> immutable() { return storage; }

	@Override
	public Class<V> valueType() {
		return storage.valueType();
	}

	@Override
	public StoreNullity<V> nullity() {
		return storage.nullity();
	}

	@Override
	public Store<V> newStore(int size) throws IllegalArgumentException {
		return storage.newStore(size).mutableCopy();
	}

	@Override
	@SafeVarargs
	// varargs safety assumes safey of delegate
	final public Store<V> newStoreOf(V... values) {
		return storage.newStoreOf(values).mutableCopy();
	}

	@Override
	public Store<V> newCopyOf(Store<V> store) {
		return storage.newCopyOf(store).mutableCopy();
	}
}
