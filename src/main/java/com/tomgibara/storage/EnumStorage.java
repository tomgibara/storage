package com.tomgibara.storage;

import com.tomgibara.storage.SmallValueStore.SmallValueStorage;

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

	@Override
	public Store<E> newStore(int size) throws IllegalArgumentException {
		SmallValueStore store = storage.newStore(size);
		if (nullValue != 0) store.fillInt(nullValue);
		return new EnumStore(store);
	}

	private final class EnumStore implements Store<E> {

		private final SmallValueStore store;

		EnumStore(SmallValueStore store) {
			this.store = store;
		}

		@Override
		public Class<E> valueType() {
			return type;
		}

		@Override
		public E nullValue() {
			return constant( nullValue );
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

