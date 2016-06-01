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

import java.util.Optional;

import com.tomgibara.storage.SmallValueStore.SmallValueStorage;

//TODO optimize storage methods
class EnumStorage<E extends Enum<E>> implements Storage<E> {

	private final Class<E> type;
	private final E[] constants;
	private final int nullValue;
	private final SmallValueStorage storage;

	EnumStorage(Class<E> type) {
		this.type = type;
		constants = type.getEnumConstants();
		if (constants.length == 0) throw new IllegalArgumentException("no enum values");
		nullValue = 0;
		storage = SmallValueStore.newStorage(constants.length);
	}

	EnumStorage(E nullValue) {
		type = nullValue.getDeclaringClass();
		constants = type.getEnumConstants();
		this.nullValue = nullValue.ordinal();
		storage = SmallValueStore.newStorage(constants.length);
	}

	EnumStorage(Class<E> type, E nullValue) {
		this.type = type;
		this.nullValue = nullValue.ordinal();
		constants = type.getEnumConstants();
		storage = SmallValueStore.newStorage(constants.length);
	}

	@Override
	public Class<E> valueType() {
		return type;
	}

	@Override
	public Store<E> newStore(int size) throws IllegalArgumentException {
		SmallValueStore store = storage.newStore(size);
		if (nullValue != 0) store.fillInt(nullValue);
		return new EnumStore(store);
	}

	private final class EnumStore extends AbstractStore<E> {

		private final SmallValueStore store;

		EnumStore(SmallValueStore store) {
			this.store = store;
		}

		@Override
		public Class<E> valueType() {
			return type;
		}

		@Override
		public Optional<E> nullValue() {
			return Optional.of( constant(nullValue) );
		}

		@Override
		public int size() {
			return store.size;
		}

		@Override
		public E get(int index) {
			return constant( store.getInt(index) );
		}

		@Override
		public boolean isNull(int index) {
			return false;
		}

		@Override
		public E set(int index, E value) {
			return constant( store.setInt(index, value(value)) );
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
			return new EnumStore(store.mutableCopy());
		}

		@Override
		public Store<E> immutableCopy() {
			return new EnumStore(store.immutableCopy());
		}

		@Override
		public Store<E> immutableView() {
			return new EnumStore(store.immutableView());
		}

		// transposable methods

		@Override
		public void transpose(int i, int j) {
			store.transpose(i, j);
		}

		// private utility methods

		private int value(E e) {
			return e == null ? nullValue : e.ordinal();
		}

		private E constant(int i) {
			return constants[i];
		}
	}

}

