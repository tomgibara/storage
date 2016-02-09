package com.tomgibara.storage;

import java.lang.reflect.Array;
import java.util.Arrays;

class NullArrayStore<V> extends AbstractStore<V> {

	final V[] values;
	int count;

	@SuppressWarnings("unchecked")
	NullArrayStore(Class<V> type, int size) {
		try {
			values = (V[]) Array.newInstance(type, size);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size", e);
		}
		this.count = 0;
	}

	NullArrayStore(V[] values) {
		this.values = values;
		count = Stores.countNonNulls(values);
	}

	NullArrayStore(V[] values, int count) {
		this.values = values;
		this.count = count;
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
		return count;
	}

	@Override
	public V get(int index) {
		return values[index];
	}

	@Override
	public V set(int index, V value) {
		V old = values[index];
		values[index] = value;
		if (old != null) count --;
		if (value != null) count ++;
		return old;
	}

	@Override
	public void clear() {
		Arrays.fill(values, null);
		count = 0;
	}

	@Override
	public void fill(V value) {
		Arrays.fill(values, value);
		count = value == null ? 0 : values.length;
	}
	
	// mutability

	@Override
	public boolean isMutable() { return true; }

	@Override
	public Store<V> mutableCopy() { return new NullArrayStore<>(values.clone(), count); }

	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), count); }

}
