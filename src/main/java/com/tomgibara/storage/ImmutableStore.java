package com.tomgibara.storage;

class ImmutableStore<V> implements Store<V> {

	private Store<V> store;
	
	ImmutableStore(Store<V> store) {
		this.store = store;
	}
	
	@Override
	public boolean isMutable() {
		return false;
	}
	
	@Override
	public int capacity() {
		return store.capacity();
	}

	@Override
	public int size() {
		return store.size();
	}

	@Override
	public Class<? extends V> valueType() {
		return store.valueType();
	}

	@Override
	public V get(int index) {
		return store.get(index);
	}
	
	@Override
	public Store<V> withCapacity(int newCapacity) {
		return store.withCapacity(newCapacity);
	}
	
	@Override
	public V set(int index, V value) {
		throw new IllegalStateException("immutable");
	}
	
	@Override
	public void clear() {
		throw new IllegalStateException("immutable");
	}

	@Override
	public Store<V> immutableView() {
		return new ImmutableStore<>(store);
	}
	
	@Override
	public Store<V> mutableView() {
		throw new IllegalStateException("Cannot take mutable view of immutable store");
	}

}