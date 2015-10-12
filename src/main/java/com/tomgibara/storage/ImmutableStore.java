package com.tomgibara.storage;

import com.tomgibara.bits.BitStore;

final class ImmutableStore<V> extends AbstractStore<V> {

	private Store<V> store;
	
	ImmutableStore(Store<V> store) {
		this.store = store;
	}
	
	@Override
	public int size() {
		return store.size();
	}

	@Override
	public int count() {
		return store.count();
	}

	@Override
	public Class<V> valueType() {
		return store.valueType();
	}

	@Override
	public V get(int index) {
		return store.get(index);
	}
	
	@Override
	public Store<V> resizedCopy(int newSize) {
		return store.resizedCopy(newSize);
	}
	
	@Override
	public Store<V> immutableView() {
		return new ImmutableStore<>(store);
	}

	@Override
	public BitStore population() {
		return store.population();
	}
	
	@Override
	public boolean isNullAllowed() {
		return store.isNullAllowed();
	}
}