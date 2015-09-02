package com.tomgibara.storage;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

class ArrayStore<V> implements Store<V> {

	final V[] values;
	int size;
	
	@SuppressWarnings("unchecked")
	ArrayStore(Class<V> type, int capacity) {
		try {
			values = (V[]) Array.newInstance(type, capacity);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative capacity", e);
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
	public Class<? extends V> valueType() {
		return (Class<? extends V>) values.getClass().getComponentType();
	}
	
	@Override
	public int capacity() {
		return values.length;
	}

	@Override
	public int size() {
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
	public List<V> asList() {
		return Arrays.asList(values);
	}

	// mutability

	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public Store<V> mutableCopy() { return new ArrayStore<>(values.clone(), size); }
	
	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), size); }
	
}
