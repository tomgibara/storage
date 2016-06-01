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

class ImmutableStorage<V> implements Storage<V> {

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
	public Store<V> newStore(int size) throws IllegalArgumentException {
		//TODO return a constant store
		return storage.newStore(size).immutableView();
	}

	@Override
	public Store<V> newStoreOf(@SuppressWarnings("unchecked") V... values) {
		return storage.newStoreOf(values).immutableView();
	}

	@Override
	public Store<V> newCopyOf(Store<V> store) {
		return storage.newCopyOf(store).immutableView();
	}
}
