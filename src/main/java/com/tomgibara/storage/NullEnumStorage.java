package com.tomgibara.storage;

import com.tomgibara.storage.SmallValueStore.SmallValueStorage;

class NullEnumStorage<E extends Enum<E>> implements Storage<E> {

	private final Class<E> type;
	private final SmallValueStorage storage;
	private final E[] constants;

	NullEnumStorage(Class<E> type) {
		this.type = type;
		constants = type.getEnumConstants();
		storage = SmallValueStore.newStorage(constants.length + 1);
	}

	@Override
	public Store<E> newStore(int size) throws IllegalArgumentException {
		return new NullEnumStore(storage.newStore(size));
	}

	private final class NullEnumStore implements Store<E> {

		private final SmallValueStore store;

		NullEnumStore(SmallValueStore store) {
			this.store = store;
		}

		@Override
		public Class<E> valueType() {
			return type;
		}

		@Override
		public boolean isNullAllowed() {
			return true;
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

