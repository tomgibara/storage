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

import com.tomgibara.storage.SmallValueStore.SmallValueStorage;

class NullEnumStorage<E extends Enum<E>> implements Storage<E> {

	private final Class<E> type;
	private final SmallValueStorage storage;
	private final E[] constants;

	NullEnumStorage(Class<E> type) {
		this.type = type;
		constants = type.getEnumConstants();
		storage = SmallValueStore.newStorage(constants.length + 1, 0);
	}

	@Override
	public Class<E> valueType() {
		return type;
	}

	@Override
	public Store<E> newStore(int size, E value) throws IllegalArgumentException {
		return new NullEnumStore(storage.newStore(size, value == null ? 0 : value.ordinal() + 1));
	}

	private final class NullEnumStore extends AbstractStore<E> {

		private final SmallValueStore store;

		NullEnumStore(SmallValueStore store) {
			this.store = store;
		}

		@Override
		public Class<E> valueType() {
			return type;
		}

		@Override
		public int size() {
			return store.size;
		}

		@Override
		public E get(int index) {
			return constant(store.getInt(index));
		}

		@Override
		public boolean isNull(int index) {
			return store.getInt(index) == 0;
		}

		@Override
		public E set(int index, E value) {
			return constant(store.setInt(index, value(value)));
		}

		@Override
		public void clear() {
			store.fillInt(0);
		}

		@Override
		public void fill(E value) {
			store.fillInt( value(value) );
		}

		// mutability methods

		@Override
		public boolean isMutable() {
			return store.isMutable();
		}

		@Override
		public Store<E> mutableCopy() {
			return new NullEnumStore(store.mutableCopy());
		}

		@Override
		public Store<E> immutableCopy() {
			return new NullEnumStore(store.immutableCopy());
		}

		@Override
		public Store<E> immutableView() {
			return new NullEnumStore(store.immutableView());
		}

		@Override
		public Store<E> resizedCopy(int newSize) {
			return new NullEnumStore(store.resizedCopy(newSize));
		}

		// transposable methods

		@Override
		public void transpose(int i, int j) {
			store.transpose(i, j);
		}

		// private utility methods

		private E constant(int i) {
			return i == 0 ? null : constants[i - 1];
		}

		private int value(E e) {
			return e == null ? 0 : e.ordinal() + 1;
		}
	}

}

