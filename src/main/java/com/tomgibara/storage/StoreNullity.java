package com.tomgibara.storage;

import java.util.Arrays;

import com.tomgibara.fundament.Bijection;

public final class StoreNullity<V> {

	private final static StoreNullity<Object> nullAllowed = new StoreNullity<Object>();
	private final static StoreNullity<Object> nullDisallowed = new StoreNullity<Object>();

	static void failNull() {
		throw new IllegalArgumentException("null value");
	}

	public static <V> StoreNullity<V> settingNullToValue(V value) {
		if (value == null) throw new IllegalArgumentException("null value");
		return new StoreNullity<>(value);
	}

	@SuppressWarnings("unchecked")
	public static <V> StoreNullity<V> settingNullAllowed() {
		return (StoreNullity<V>) nullAllowed;
	}

	@SuppressWarnings("unchecked")
	public static <V> StoreNullity<V> settingNullDisallowed() {
		return (StoreNullity<V>) nullDisallowed;
	}

	// value only null for fixed static instances
	private final V value;

	private StoreNullity() {
		value = null;
	}

	private StoreNullity(V value) {
		this.value = value;
	}

	public boolean nullSettable() {
		return this != nullDisallowed;
	}

	public boolean nullGettable() {
		return this == nullAllowed;
	}

	public V nullValue() {
		return value;
	}

	@Override
	public int hashCode() {
		if (this == nullDisallowed) return 0x7fffffff;
		if (this == nullAllowed) return 0;
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StoreNullity)) return false;
		StoreNullity<?> that = (StoreNullity<?>) obj;
		if (this.value == null) return this == that;
		if (that.value == null) return false;
		return this.value.equals(that.value);
	}

	@Override
	public String toString() {
		if (this == nullAllowed) return "null allowed";
		if (this == nullDisallowed) return "null disallowed";
		return "null set to " + value;
	}

	void checkNull() {
		if (this == nullDisallowed) failNull();
	}

	V checkedValue(V value) {
		if (this == nullAllowed || value != null) return value;
		if (this == nullDisallowed) failNull();
		return this.value;
	}

	void checkValues(V[] values) {
		if (this == nullAllowed) return;
		if (this == nullDisallowed) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] == null) failNull();
			}
			return;
		}
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) values[i] = value;
		}
	}

	int countNonNulls(V[] values) {
		if (this == nullDisallowed) return values.length;
		int count = 0;
		for (V v : values) if (v != null) count++;
		return count;
	}

	V[] resizedCopyOf(V[] values, int newSize) {
		if (this == nullAllowed) {
			return Arrays.copyOf(values, newSize);
		}
		if (this == nullDisallowed) {
			if (newSize > values.length) throw new IllegalArgumentException("null disallowed");
			return Arrays.copyOf(values, newSize);
		}
		int oldSize = values.length;
		values = Arrays.copyOf(values, newSize);
		if (newSize > oldSize) Arrays.fill(values, oldSize, newSize, value);
		return values;
	}

	<W> StoreNullity<W> map(Bijection<V, W> fn) {
		if (this == nullAllowed) return settingNullAllowed();
		if (this == nullDisallowed) return settingNullDisallowed();
		return new StoreNullity<>(fn.apply(value));
	}
}
