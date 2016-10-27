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

final class EnumStorage<E extends Enum<E>> implements Storage<E> {

	private final Class<E> type;
	private final E[] constants;
	private final int nullValue;
	private final StoreNullity<E> nullity;
	private final SmallValueStorage storage;

	EnumStorage(Class<E> type, StoreNullity<E> nullity) {
		this.type = type;
		this.nullity = nullity;
		this.nullValue = nullity.nullSettable() ? nullity.nullValue().ordinal() : 0;
		constants = type.getEnumConstants();
		storage = SmallValueStore.newStorage(constants.length);
	}

	@Override
	public Class<E> valueType() {
		return type;
	}

	@Override
	public Store<E> newStore(int size) throws IllegalArgumentException {
		if (size < 0) throw new IllegalArgumentException("negative size");
		if (size > 0 && !nullity.nullSettable()) throw new IllegalArgumentException("no null value with which to populate store");
		SmallValueStore store = storage.newStore(size);
		if (nullValue != 0) store.fillInt(nullValue);
		return new EnumStore(store);
	}

	@Override
	public Store<E> newStoreOf(@SuppressWarnings("unchecked") E... values) {
		if (values == null) throw new IllegalArgumentException("null values");
		int size = values.length;
		SmallValueStore store = storage.newStore(size);
		for (int i = 0; i < size; i++) {
			store.set(i, nullity.checkedValue(values[i]).ordinal());
		}
		return new EnumStore(store);
	}

	@Override
	public Store<E> newCopyOf(Store<E> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		int size = store.size();
		SmallValueStore s = storage.newStore(size);
		if (store.nullity().nullGettable()) {
			for (int i = 0; i < size; i++) {
				s.set(i, nullity.checkedValue(store.get(i)).ordinal());
			}
		} else {
			for (int i = 0; i < size; i++) {
				s.set(i, store.get(i).ordinal());
			}
		}
		return new EnumStore(s);
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
		public StoreNullity<E> nullity() {
			return nullity;
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
			if (e == null) {
				nullity.checkNull();
				return nullValue;
			}
			return e.ordinal();
		}

		private E constant(int i) {
			return constants[i];
		}
	}

}

