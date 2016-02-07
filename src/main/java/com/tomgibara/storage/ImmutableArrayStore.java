package com.tomgibara.storage;

import java.util.Arrays;

final class ImmutableArrayStore<V> extends AbstractStore<V> {

	private final V[] values;
	private final int count;
	private final boolean nullAllowed;

	ImmutableArrayStore(V[] values, int count) {
		this.values = values;
		this.count = count;
		nullAllowed = true;
	}

	ImmutableArrayStore(V[] values, boolean nullAllowed) {
		this.values = values;
		count = Stores.countNonNulls(values);
		this.nullAllowed = nullAllowed;
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<V> valueType() {
		return (Class<V>) values.getClass().getComponentType();
	}

	@Override
	public int count() { return count; }

	@Override
	public V get(int index) { return values[index]; }

	@Override
	public Store<V> resizedCopy(int newSize) {
		return nullAllowed ?
				new NullArrayStore<>(Arrays.copyOf(values, newSize), count) :
				new ArrayStore<>(Arrays.copyOf(values, newSize));
	}

	@Override
	public boolean isNullAllowed() {
		return nullAllowed;
	}

	// mutability

	@Override
	public Store<V> mutableCopy() {
		return nullAllowed ?
				new NullArrayStore<>(values.clone(), count) :
				new ArrayStore<>(values.clone());
		}

	@Override
	public Store<V> immutableCopy() {
		return nullAllowed ?
				new ImmutableArrayStore<>(values.clone(), count) :
				new ImmutableArrayStore<>(values.clone(), false);
		}

	@Override
	public Store<V> immutableView() {
		return nullAllowed ?
				new ImmutableArrayStore<>(values, count):
				new ImmutableArrayStore<>(values, false);
	}

}