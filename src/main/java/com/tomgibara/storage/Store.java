package com.tomgibara.storage;


public interface Store<V> extends Mutability<Store<V>> {

	Class<? extends V> valueType();

	int capacity();
	
	int size();
	
	V get(int index);
	
	V set(int index, V value);
	
	void clear();
	
	// returns a mutable detached copy
	default Store<V> withCapacity(int newCapacity) {
		return new ArrayStore<>(Stores.toArray(this, newCapacity), size());
	}

	@Override
	default Store<V> mutable() {
		return isMutable() ? this : mutableCopy();
	}
	
	@Override
	default Store<V> mutableCopy() {
		return new ArrayStore<>(Stores.toArray(this), size());
	}

	@Override
	default Store<V> immutable() {
		return isMutable() ? immutableView() : this;
	}
	
	@Override
	default Store<V> immutableCopy() {
		return new ImmutableArrayStore<>(Stores.toArray(this), size());
	}

}
