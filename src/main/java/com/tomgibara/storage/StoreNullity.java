package com.tomgibara.storage;

import java.util.Arrays;

import com.tomgibara.fundament.Bijection;

public final class StoreNullity<V> {

	// private statics

	private final static StoreNullity<Object> nullAllowed = new StoreNullity<Object>();
	private final static StoreNullity<Object> nullDisallowed = new StoreNullity<Object>();

	private static final StoreNullity<Byte>      BYTE    = new StoreNullity<>((byte)   0);
	private static final StoreNullity<Float>     FLOAT   = new StoreNullity<>((float)  0);
	private static final StoreNullity<Character> CHAR    = new StoreNullity<>((char)   0);
	private static final StoreNullity<Short>     SHORT   = new StoreNullity<>((short)  0);
	private static final StoreNullity<Long>      LONG    = new StoreNullity<>((long)   0);
	private static final StoreNullity<Integer>   INT     = new StoreNullity<>((int)    0);
	private static final StoreNullity<Double>    DOUBLE  = new StoreNullity<>((double) 0);
	private static final StoreNullity<Boolean>   BOOLEAN = new StoreNullity<>(     false);

	private static final StoreNullity<String> STRING  = new StoreNullity<>("");

	// package statics

	static void failNull() {
		throw new IllegalArgumentException("null value");
	}

	// public statics

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

	/**
	 * <p>
	 * Suggests a suitable null value for a specified type. This method may be
	 * used to identify an appropriate null value for stores of common types.
	 *
	 * <p>
	 * It returns optionals wrapping the following values for the types given.
	 *
	 * <dl>
	 * <dt>primitive numeric types
	 * <dd><code>0</code>
	 * <dt><code>boolean</code>
	 * <dd><code>false</code>
	 * <dt><code>char</code>
	 * <dd><code>'\0'</code>
	 * <dt>primitive wrapper types
	 * <dd><em>as per primitive types</dd>
	 * <dt>enumerations
	 * <dd>the enum constant with ordinal 0
	 * <dt><code>java.lang.String</code>
	 * <dd>the empty string <code>""</code>
	 * </dl>
	 *
	 * <p>
	 * For all other types, the method returns an empty optional. Future
	 * implementations may return non-empty optionals for a greater number of
	 * types.
	 *
	 * @param type
	 *            a type of stored value
	 * @return an optional null value
	 */
	@SuppressWarnings("unchecked")
	public static <V> StoreNullity<V> defaultForType(Class<V> type) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (type.isEnum()) {
			V[] constants = type.getEnumConstants();
			return constants.length == 0 ? settingNullAllowed() : settingNullToValue((V) constants[0]);
		}
		if (type.isPrimitive()) {
			switch((type.getName().hashCode() >> 8) & 0xf) {
			case Stores.BYTE:    return (StoreNullity<V>) BYTE;
			case Stores.FLOAT:   return (StoreNullity<V>) FLOAT;
			case Stores.CHAR:    return (StoreNullity<V>) CHAR;
			case Stores.SHORT:   return (StoreNullity<V>) SHORT;
			case Stores.LONG:    return (StoreNullity<V>) LONG;
			case Stores.INT:     return (StoreNullity<V>) INT;
			case Stores.DOUBLE:  return (StoreNullity<V>) DOUBLE;
			case Stores.BOOLEAN: return (StoreNullity<V>) BOOLEAN;
			}
		}
		switch (type.getName()) {
		case "java.lang.String"    : return (StoreNullity<V>) STRING;
		case "java.lang.Boolean"   : return (StoreNullity<V>) BOOLEAN;
		case "java.lang.Character" : return (StoreNullity<V>) CHAR;
		case "java.lang.Float"     : return (StoreNullity<V>) FLOAT;
		case "java.lang.Double"    : return (StoreNullity<V>) DOUBLE;
		case "java.lang.Byte"      : return (StoreNullity<V>) BYTE;
		case "java.lang.Short"     : return (StoreNullity<V>) SHORT;
		case "java.lang.Integer"   : return (StoreNullity<V>) INT;
		case "java.lang.Long"      : return (StoreNullity<V>) LONG;
		}
		return settingNullAllowed();
	}

	// fields

	// value only null for fixed static instances
	private final V value;

	// constructors

	private StoreNullity() {
		value = null;
	}

	private StoreNullity(V value) {
		this.value = value;
	}

	// access methods

	public boolean nullSettable() {
		return this != nullDisallowed;
	}

	public boolean nullGettable() {
		return this == nullAllowed;
	}

	public V nullValue() {
		return value;
	}

	// object methods

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

	// package utility methods

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
