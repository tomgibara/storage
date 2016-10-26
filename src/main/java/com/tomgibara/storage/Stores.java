/*
 * Copyright 2015 Tom Gibara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tomgibara.storage;

import static com.tomgibara.storage.StoreNullity.settingNullAllowed;
import static com.tomgibara.storage.StoreNullity.settingNullDisallowed;
import static com.tomgibara.storage.StoreNullity.settingNullToValue;

import java.lang.reflect.Array;

/**
 * Collects static methods for creating new stores that wrap existing arrays.
 *
 * @author Tom Gibara
 *
 */
public final class Stores {

	// private statics

	private static final StoreNullity<Byte>      NULL_BYTE    = settingNullToValue((byte)   0);
	private static final StoreNullity<Float>     NULL_FLOAT   = settingNullToValue((float)  0);
	private static final StoreNullity<Character> NULL_CHAR    = settingNullToValue((char)   0);
	private static final StoreNullity<Short>     NULL_SHORT   = settingNullToValue((short)  0);
	private static final StoreNullity<Long>      NULL_LONG    = settingNullToValue((long)   0);
	private static final StoreNullity<Integer>   NULL_INT     = settingNullToValue((int)    0);
	private static final StoreNullity<Double>    NULL_DOUBLE  = settingNullToValue((double) 0);
	private static final StoreNullity<Boolean>   NULL_BOOLEAN = settingNullToValue(     false);

	private static final StoreNullity<String> NULL_STRING  = settingNullToValue("");

	// package statics

	static final int BYTE    =  1;
	static final int FLOAT   =  2;
	static final int CHAR    =  3;
	static final int SHORT   =  4;
	static final int LONG    =  6;
	static final int INT     =  7;
	static final int DOUBLE  = 11;
	static final int BOOLEAN = 12;

	// public scoped methods

	/**
	 * <p>
	 * Returns a 'mutable' store of zero size. Though the store is ostensibly
	 * mutable, its fixed zero size means that the store is immutable for all
	 * practical purposes.
	 *
	 * <p>
	 * Nevertheless, if the immutable status of the store is significant for the
	 * application, the {@link Store#immutable()} method (or one of its
	 * relations) may be invoked to produce an equivalent store that reports
	 * itself as immutable.
	 *
	 * @param type
	 *            the putative type of the store elements
	 * @return a store of zero size
	 */

	public static <V> Store<V> empty(Class<V> type) {
		return new EmptyStore<V>(type, true);
	}

	/**
	 * Creates a mutable store that wraps an existing array. The returned store
	 * supports null values if the <code>nullValue</code> optional parameter is
	 * empty and in this case, supplying an array containing nulls is likely to
	 * cause malfunction.
	 *
	 * @param values
	 *            the values of the store
	 * @param nullValue
	 *            the contains the value substituted for null on mutation, or is
	 *            empty if the store should support null values
	 * @param <V>
	 *            the type of values to be stored
	 * @return a store that mediates access to the array
	 */
	@SafeVarargs
	public static <V> Store<V> objectsWithNullity(StoreNullity<V> nullity, V... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullArrayStore<>(values) : new ArrayStore<>(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing array. The returned store
	 * supports null values.
	 *
	 * @param values
	 *            the values of the store
	 * @param <V>
	 *            the type of values to be stored
	 * @return a store that mediates access to the array
	 */
	@SafeVarargs
	public static <V> Store<V> objects(V... values) {
		checkValuesNotNull(values);
		return new NullArrayStore<>(values);
	}

	/**
	 * Creates a mutable store that wraps an existing array. This method may be
	 * used if the size (the number of non-null values) is already known.
	 * Supplying an incorrect size is likely to cause malfunction.
	 *
	 * @param values
	 *            the values of the store
	 * @param count
	 *            the number of non-null values in the array
	 * @param <V>
	 *            the type of values to be stored
	 * @return a store that mediates access to the array
	 */
	@SafeVarargs
	public static <V> Store<V> objectsAndNullCount(int count, V... values) {
		if (count < 0) throw new IllegalArgumentException("negative count");
		checkValuesNotNull(values);
		return new NullArrayStore<>(values, count);
	}

	/**
	 * Creates an immutable store that returns values from an existing array.
	 * The returned store supports null values if the <code>nullValue</code>
	 * optional parameter is empty and in this case, supplying an array
	 * containing nulls is likely to cause malfunction. The supplied array is
	 * not copied and must not be modified.
	 *
	 * @param values
	 *            the values of the store
	 * @param nullValue
	 *            the contains the value substituted for null in any mutable
	 *            copies, or is empty if the store should support null values
	 * @param <V>
	 *            the type of values to be stored
	 * @return a store that returns values from array
	 */
	@SafeVarargs
	public static <V> Store<V> immutableObjectsWithNullity(StoreNullity<V> nullity, V... values) {
		checkValuesNotNull(values);
		return new ImmutableArrayStore<>(values, nullity);
	}

	/**
	 * Creates an immutable store that returns values from an existing array.
	 * The supplied array is not copied and must not be modified.
	 *
	 * @param values
	 *            the values of the store
	 * @param <V>
	 *            the type of values to be stored
	 * @return a store that returns values from array
	 */
	@SafeVarargs
	public static <V> Store<V> immutableObjects(V... values) {
		checkValuesNotNull(values);
		return new ImmutableArrayStore<>(values, settingNullAllowed());
	}

	/**
	 * Creates an immutable store that returns values from existing array. The
	 * supplied array is not copied and must not be modified. This method may be
	 * used if the size (the number of non-null values) is already known.
	 * Supplying an incorrect size is likely to cause malfunction.
	 *
	 * @param values
	 *            the values of the store
	 * @param count
	 *            the number of non-null values in the array
	 * @return a store that mediates access to the array
	 */
	@SafeVarargs
	public static <V> Store<V> immutableObjectsAndNullCount(int count, V... values) {
		if (count < 0) throw new IllegalArgumentException("negative size");
		checkValuesNotNull(values);
		return new ImmutableArrayStore<>(values, count);
	}

	//TODO this is unpleasant
	@SuppressWarnings("unchecked")
	public static <V> Store<V> constantStore(V value, int size) {
		if (value == null) throw new IllegalArgumentException("null value");
		if (size < 0L) throw new IllegalArgumentException("negative size");
		return new ConstantStore<V>((Class<V>)Object.class, value, size);
	}

	public static <V> Store<V> constantStore(Class<V> type, V value, int size) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (value == null) throw new IllegalArgumentException("null value");
		if (size < 0L) throw new IllegalArgumentException("negative size");
		return new ConstantStore<V>(type, value, size);
	}

	@SuppressWarnings("unchecked")
	public static <V> Store<V> constantNullStore(int size) {
		if (size < 0L) throw new IllegalArgumentException("negative size");
		return new NullConstantStore<V>((Class<V>)Object.class, size);
	}

	public static <V> Store<V> constantNullStore(Class<V> type, int size) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (size < 0L) throw new IllegalArgumentException("negative size");
		return new NullConstantStore<V>(type, size);
	}

	/**
	 * Creates a mutable store that wraps an existing byte array.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Byte> bytes(byte... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.ByteStore(values, settingNullDisallowed());
	}

	/**
	 * Creates a mutable store that wraps an existing byte array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link Store#population()}.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Byte> bytesWithNullity(StoreNullity<Byte> nullity, byte... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.ByteStore(values) : new PrimitiveStore.ByteStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing short array.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Short> shorts(short... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.ShortStore(values, settingNullDisallowed());
	}

	/**
	 * Creates a mutable store that wraps an existing short array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link Store#population()}.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Short> shortsWithNullity(StoreNullity<Short> nullity, short... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.ShortStore(values) : new PrimitiveStore.ShortStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing integer array.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Integer> ints(int... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.IntegerStore(values, settingNullDisallowed());
	}

	/**
	 * Creates a mutable store that wraps an existing integer array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link Store#population()}.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Integer> intsWithNullity(StoreNullity<Integer> nullity, int... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.IntegerStore(values) : new PrimitiveStore.IntegerStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing long array.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Long> longs(long... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.LongStore(values, settingNullDisallowed());
	}

	/**
	 * Creates a mutable store that wraps an existing long array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link Store#population()}.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Long> longsWithNullity(StoreNullity<Long> nullity, long... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.LongStore(values) : new PrimitiveStore.LongStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing boolean array
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Boolean> booleans(boolean... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.BooleanStore(values, settingNullDisallowed());
	}

	/**
	 * Creates a mutable store that wraps an existing boolean array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link Store#population()}.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Boolean> booleansWithNullity(StoreNullity<Boolean> nullity, boolean... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.BooleanStore(values) : new PrimitiveStore.BooleanStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing char array.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Character> chars(char... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.CharacterStore(values, settingNullDisallowed());
	}

	/**
	 * Creates a mutable store that wraps an existing char array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link Store#population()}.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Character> charsWithNullity(StoreNullity<Character> nullity, char... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.CharacterStore(values) : new PrimitiveStore.CharacterStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing float array.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Float> floats(float... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.FloatStore(values, settingNullDisallowed());
	}

	/**
	 * Creates a mutable store that wraps an existing float array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link Store#population()}.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Float> floatsWithNullity(StoreNullity<Float> nullity, float... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.FloatStore(values) : new PrimitiveStore.FloatStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing double array.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Double> doubles(double... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.DoubleStore(values, settingNullDisallowed());
	}

	/**
	 * Creates a mutable store that wraps an existing double array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link Store#population()}.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Double> doublesWithNullity(StoreNullity<Double> nullity, double... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.DoubleStore(values) : new PrimitiveStore.DoubleStore(values, nullity);
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
	public static <V> StoreNullity<V> defaultNullity(Class<V> type) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (type.isEnum()) return settingNullToValue((V) type.getEnumConstants()[0]);
		if (type.isPrimitive()) {
			switch((type.getName().hashCode() >> 8) & 0xf) {
			case Stores.BYTE:    return (StoreNullity<V>) NULL_BYTE;
			case Stores.FLOAT:   return (StoreNullity<V>) NULL_FLOAT;
			case Stores.CHAR:    return (StoreNullity<V>) NULL_CHAR;
			case Stores.SHORT:   return (StoreNullity<V>) NULL_SHORT;
			case Stores.LONG:    return (StoreNullity<V>) NULL_LONG;
			case Stores.INT:     return (StoreNullity<V>) NULL_INT;
			case Stores.DOUBLE:  return (StoreNullity<V>) NULL_DOUBLE;
			case Stores.BOOLEAN: return (StoreNullity<V>) NULL_BOOLEAN;
			}
		}
		switch (type.getName()) {
		case "java.lang.String"    : return (StoreNullity<V>) NULL_STRING;
		case "java.lang.Boolean"   : return (StoreNullity<V>) NULL_BOOLEAN;
		case "java.lang.Character" : return (StoreNullity<V>) NULL_CHAR;
		case "java.lang.Float"     : return (StoreNullity<V>) NULL_FLOAT;
		case "java.lang.Double"    : return (StoreNullity<V>) NULL_DOUBLE;
		case "java.lang.Byte"      : return (StoreNullity<V>) NULL_BYTE;
		case "java.lang.Short"     : return (StoreNullity<V>) NULL_SHORT;
		case "java.lang.Integer"   : return (StoreNullity<V>) NULL_INT;
		case "java.lang.Long"      : return (StoreNullity<V>) NULL_LONG;
		}
		return StoreNullity.settingNullAllowed();
	}

	// package scoped methods

	static IllegalStateException immutableException() {
		return new IllegalStateException("immutable");
	}

//	static <V> int countNonNulls(V[] vs) {
//		int sum = 0;
//		for (V v : vs) if (v != null) sum++;
//		return sum;
//	}

//	static <V> void replaceNulls(V[] vs, V nullValue) {
//		for (int i = 0; i < vs.length; i++) {
//			if (vs[i] == null) vs[i] = nullValue;
//		}
//	}

	static <V> V[] typedArrayCopy(Class<V> type, V[] vs) {
		if (vs.getClass().getComponentType() == type) {
			return vs.clone();
		} else {
			@SuppressWarnings("unchecked")
			V[] copy = (V[]) Array.newInstance(type, vs.length);
			System.arraycopy(vs, 0, copy, 0, vs.length);
			return copy;
		}

	}

	static <V> V[] toArray(Store<V> store) {
		return toArray(store, store.size());
	}

	static<V> V[] toArray(Store<V> store, int length) {
		@SuppressWarnings("unchecked")
		V[] vs = (V[]) Array.newInstance(store.valueType(), length);
		return copyIntoArray(store, vs);
	}

	static<V> V[] copyIntoArray(Store<V> store, V[] vs) {
		int limit = Math.min( store.size(), vs.length );
		for (int i = 0; i < limit; i++) {
			vs[i] = store.get(i);
		}
		return vs;
	}

	static void checkValuesNotNull(Object values) {
		if (values == null) throw new IllegalArgumentException("null values");
	}

//	static <V> V[] resizedCopyOf(V[] vs, int newSize, V v) {
//		if (v == null) return Arrays.copyOf(vs, newSize);
//		int oldSize = vs.length;
//		vs = Arrays.copyOf(vs, newSize);
//		if (newSize > oldSize) Arrays.fill(vs, oldSize, newSize, v);
//		return vs;
//	}

	static <V> boolean compact(V[] vs, int count) {
		if (count == vs.length) return false;
		int i = 0; // index to read from
		int j = 0; // index to write to
		while (j < count) {
			V value = vs[i];
			if (value != null) {
				if (j < i) vs[j] = value;
				j++;
			}
			i++; // skip forwards
		}
		if (i == j) return false;
		while (j < i) {
			vs[j++] = null;
		}
		return true;
	}

	// non-constructor

	private Stores() {}
}
