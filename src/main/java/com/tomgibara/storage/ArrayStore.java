package com.tomgibara.storage;

import java.lang.reflect.Array;
import java.util.Arrays;

class ArrayStore<V> implements Store<V> {

	final V[] values;
	int size;
	
	@SuppressWarnings("unchecked")
	ArrayStore(Class<V> type, int size) {
		try {
			values = (V[]) Array.newInstance(type, size);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size", e);
		}
		size = 0;
	}
	
	ArrayStore(V[] values) {
		this.values = values;
		size = Stores.countNonNulls(values);
	}

	ArrayStore(V[] values, int size) {
		this.values = values;
		this.size = size;
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
		return size;
	}
	
	@Override
	public V get(int index) {
		return values[index];
	}

	@Override
	public V set(int index, V value) {
		V old = values[index];
		values[index] = value;
		if (old != null) size --;
		if (value != null) size ++;
		return old;
	}

	@Override
	public void clear() {
		Arrays.fill(values, null);
		size = 0;
	}

	@Override
	public void fill(V value) {
		Arrays.fill(values, value);
		size = value == null ? 0 : values.length;
	}

	// mutability

	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public Store<V> mutableCopy() { return new ArrayStore<>(values.clone(), size); }
	
	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), size); }
	
}
