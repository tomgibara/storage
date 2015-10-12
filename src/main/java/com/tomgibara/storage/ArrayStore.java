package com.tomgibara.storage;

import java.lang.reflect.Array;
import java.util.Arrays;

class ArrayStore<V> extends AbstractStore<V> {

	final V[] values;
	
	@SuppressWarnings("unchecked")
	ArrayStore(Class<V> type, int size) {
		try {
			values = (V[]) Array.newInstance(type, size);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size", e);
		}
	}
	
	ArrayStore(V[] values) {
		this.values = values;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<V> valueType() {
		return (Class<V>) values.getClass().getComponentType();
	}
	
	@Override
	public int size() {
		return values.length;
	}

	@Override
	public int count() {
		return values.length;
	}
	
	@Override
	public V get(int index) {
		return values[index];
	}

	@Override
	public V set(int index, V value) {
		V old = values[index];
		values[index] = value;
		return old;
	}

	@Override
	public void fill(V value) {
		if (value == null) throw new IllegalArgumentException("null not allowed");
		Arrays.fill(values, value);
	}
	
	@Override
	public boolean isNullAllowed() {
		return true;
	}

	// mutability

	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public Store<V> mutableCopy() { return new ArrayStore<>(values.clone()); }
	
	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), values.length); }
	
}
