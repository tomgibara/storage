package com.tomgibara.storage;

import com.tomgibara.storage.SmallValueStore.SmallValueStorage;

class EnumStorage<E extends Enum<E>> implements Storage<E> {

	private final Class<E> type;
	private final SmallValueStorage storage;
	private final E[] constants;

	EnumStorage(Class<E> type) {
		this.type = type;
		constants = type.getEnumConstants();
		storage = SmallValueStore.newStorage(constants.length);
	}

	@Override
	public Store<E> newStore(int size) throws IllegalArgumentException {
		return new EnumStore(storage.newStore(size));
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
		public boolean isNullAllowed() {
			return false;
		}

		@Override
		public int size() {
			return store.size;
		}

		@Override
		public E get(int index) {
			return constants[ store.getInt(index) ];
		}

		@Override
		public E set(int index, E value) {
			if (value == null) throw new IllegalArgumentException("null value");
			return constants[ store.setInt(index, value.ordinal()) ];
		}

		@Override
		public void fill(E value) {
			if (value == null) throw new IllegalArgumentException("null value");
			store.fillInt( value.ordinal() );
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

	}

}

