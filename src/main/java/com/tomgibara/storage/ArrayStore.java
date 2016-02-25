package com.tomgibara.storage;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;

class ArrayStore<V> extends AbstractStore<V> {

	final V[] values;
	final V nullValue;

	@SuppressWarnings("unchecked")
	ArrayStore(Class<V> type, int size, V nullValue) {
		if (nullValue == null) throw new IllegalArgumentException("null initialValue");
		if (type == Object.class) {
			values = (V[]) new Object[size];
		} else try {
			values = (V[]) Array.newInstance(type, size);
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size", e);
		}
		Arrays.fill(values, nullValue);
		this.nullValue = nullValue;
	}

	ArrayStore(V[] values, V nullValue) {
		this.values = values;
		this.nullValue = nullValue;
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
	public boolean isNull(int index) {
		return false;
	}

	@Override
	public V set(int index, V value) {
		if (value == null) value = nullValue;
		V old = values[index];
		values[index] = value;
		return old;
	}

	@Override
	public void fill(V value) {
		if (value == null) value = nullValue;
		Arrays.fill(values, value);
	}

	@Override
	public Optional<V> nullValue() {
		return Optional.of(nullValue);
	}
	
	// mutability

	@Override
	public boolean isMutable() { return true; }

	@Override
	public Store<V> mutableCopy() { return new ArrayStore<V>(values.clone(), nullValue); }

	@Override
	public Store<V> immutableCopy() { return new ImmutableArrayStore<>(values.clone(), values.length); }

}
