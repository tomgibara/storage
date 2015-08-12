package com.tomgibara.storage;

import java.util.Arrays;

final class ImmutableArrayStore<V> implements Store<V> {

	private final V[] values;
	private final int size;

	ImmutableArrayStore(V[] values, int size) {
		this.values = values;
		this.size = size;
	}

	@Override
	public int capacity() {
		return values.length;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends V> valueType() {
		return (Class<? extends V>) values.getClass().getComponentType();
	}
	
	@Override
	public int size() { return size; }

	@Override
	public V get(int index) { return values[index]; }

	@Override
	public Store<V> withCapacity(int newCapacity) {
		return new ArrayStore<>(Arrays.copyOf(values, newCapacity), size);
	}
	
	@Override
	public V set(int index, V value) {
		throw new IllegalStateException("immutable");
	}
	
	@Override
	public void clear() {
		throw new IllegalStateException("immutable");
	}
	
	// mutability
	
	@Override
	public boolean isMutable() { return false; }
	
	@Override
	public Store<V> mutableCopy() { return new ArrayStore<>(values.clone(), size); }
	
	@Override
	public Store<V> mutableView() { throw new IllegalStateException("Cannot take mutable view of immutable store"); }

	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), size); }
	
	@Override
	public Store<V> immutableView() { return new ImmutableArrayStore<>(values, size); }

}