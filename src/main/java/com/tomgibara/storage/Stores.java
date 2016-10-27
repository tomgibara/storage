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

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Collects static methods for creating new stores that wrap existing arrays.
 *
 * @author Tom Gibara
 *
 */
public final class Stores {

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
	 * @param <V>
	 *            the storage type
	 * @param type
	 *            the putative type of the store elements
	 * @return a store of zero size
	 */

	public static <V> Store<V> empty(Class<V> type) {
		return new EmptyStore<V>(type, true);
	}

	/**
	 * Creates a mutable store that wraps an existing array. The returned store
	 * supports setting and getting null values.
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
	 * Creates a mutable store that wraps an existing array. The returned store
	 * supports null values as per the specified nullity. For reasons of
	 * performance, the supplied values are <em>not</em> checked for consistency
	 * with the specified nullity; supplying an array containing nulls where
	 * they are prohibited is likely to cause malfunction.
	 *
	 * @param nullity
	 *            how null values are to be supported
	 * @param values
	 *            the values of the store
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
	 * The returned store supports null values as per the specified nullity. For
	 * reasons of performance, the supplied values are <em>not</em> checked for
	 * consistency with the specified nullity; supplying an array containing
	 * nulls where they are prohibited is likely to cause malfunction. The
	 * supplied array is not copied and must not be modified.
	 *
	 * @param values
	 *            the values of the store
	 * @param nullity
	 *            how null values are to be supported
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
	 * @param <V>
	 *            the storage type
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
		return new ImmutableArrayStore<>(values, count, settingNullAllowed());
	}

	/**
	 * Returns an immutable store consisting of <em>size</em> copies of
	 * <em>value</em>. The advantage of using this method is that memory does
	 * not need to be allocated for each instance of the value.
	 *
	 * @param <V>
	 *            the storage type
	 * @param value
	 *            the value to be stored, possibly null
	 * @param size
	 *            the number of copies stored, possibly zero
	 * @return a store consisting of multiple copies of a single value
	 * @see #constantStore(Class, Object, int)
	 */
	public static <V> Store<V> constantStore(V value, int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		@SuppressWarnings("unchecked")
		Class<V> clss = (Class<V>)Object.class;
		return value == null ?
				new NullConstantStore<V>(clss, size) :
				new ConstantStore<V>(clss, value, size);
	}

	/**
	 * Returns an immutable store consisting of <em>size</em> copies of
	 * <em>value</em>. The advantage of using this method is that memory does
	 * not need to be allocated for each instance of the value.
	 *
	 * @param <V>
	 *            the storage type
	 * @param type
	 *            the declared storage type
	 * @param value
	 *            the value to be stored, possibly null
	 * @param size
	 *            the number of copies stored, possibly zero
	 * @return a store consisting of multiple copies of a single value
	 * @see #constantStore(Object, int)
	 */
	public static <V> Store<V> constantStore(Class<V> type, V value, int size) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (size < 0) throw new IllegalArgumentException("negative size");
		return value == null ?
				new NullConstantStore<V>(type, size) :
				new ConstantStore<V>(type, value, size);
	}

	/**
	 * Creates a mutable store that wraps an existing byte array. Null values
	 * are not supported by the returned store.
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
	 * Creates a mutable store that wraps an existing byte array. Where the
	 * permitted by the specified nullity, values may subsequently be removed
	 * from the store by setting an index value to null. Such operations will
	 * not modify the wrapped array; the null status of an index may be obtained
	 * from the {@link Store#population()}.
	 *
	 * @param nullity
	 *            how null values are to be supported
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Byte> bytesWithNullity(StoreNullity<Byte> nullity, byte... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.ByteStore(values) : new PrimitiveStore.ByteStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing short array. Null values
	 * are not supported by the returned store.
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
	 * Creates a mutable store that wraps an existing short array. Where the
	 * permitted by the specified nullity, values may subsequently be removed
	 * from the store by setting an index value to null. Such operations will
	 * not modify the wrapped array; the null status of an index may be obtained
	 * from the {@link Store#population()}.
	 *
	 * @param nullity
	 *            how null values are to be supported
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Short> shortsWithNullity(StoreNullity<Short> nullity, short... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.ShortStore(values) : new PrimitiveStore.ShortStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing integer array. Null values
	 * are not supported by the returned store.
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
	 * Creates a mutable store that wraps an existing integer array. Where the
	 * permitted by the specified nullity, values may subsequently be removed
	 * from the store by setting an index value to null. Such operations will
	 * not modify the wrapped array; the null status of an index may be obtained
	 * from the {@link Store#population()}.
	 *
	 * @param nullity
	 *            how null values are to be supported
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Integer> intsWithNullity(StoreNullity<Integer> nullity, int... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.IntegerStore(values) : new PrimitiveStore.IntegerStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing long array. Null values
	 * are not supported by the returned store.
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
	 * Creates a mutable store that wraps an existing long array. Where the
	 * permitted by the specified nullity, values may subsequently be removed
	 * from the store by setting an index value to null. Such operations will
	 * not modify the wrapped array; the null status of an index may be obtained
	 * from the {@link Store#population()}.
	 *
	 * @param nullity
	 *            how null values are to be supported
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Long> longsWithNullity(StoreNullity<Long> nullity, long... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.LongStore(values) : new PrimitiveStore.LongStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing boolean array. Null values
	 * are not supported by the returned store.
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
	 * Creates a mutable store that wraps an existing boolean array. Where the
	 * permitted by the specified nullity, values may subsequently be removed
	 * from the store by setting an index value to null. Such operations will
	 * not modify the wrapped array; the null status of an index may be obtained
	 * from the {@link Store#population()}.
	 *
	 * @param nullity
	 *            how null values are to be supported
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Boolean> booleansWithNullity(StoreNullity<Boolean> nullity, boolean... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.BooleanStore(values) : new PrimitiveStore.BooleanStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing char array. Null values
	 * are not supported by the returned store.
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
	 * Creates a mutable store that wraps an existing char array. Where the
	 * permitted by the specified nullity, values may subsequently be removed
	 * from the store by setting an index value to null. Such operations will
	 * not modify the wrapped array; the null status of an index may be obtained
	 * from the {@link Store#population()}.
	 *
	 * @param nullity
	 *            how null values are to be supported
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Character> charsWithNullity(StoreNullity<Character> nullity, char... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.CharacterStore(values) : new PrimitiveStore.CharacterStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing float array. Null values
	 * are not supported by the returned store.
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
	 * Creates a mutable store that wraps an existing float array. Where the
	 * permitted by the specified nullity, values may subsequently be removed
	 * from the store by setting an index value to null. Such operations will
	 * not modify the wrapped array; the null status of an index may be obtained
	 * from the {@link Store#population()}.
	 *
	 * @param nullity
	 *            how null values are to be supported
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Float> floatsWithNullity(StoreNullity<Float> nullity, float... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.FloatStore(values) : new PrimitiveStore.FloatStore(values, nullity);
	}

	/**
	 * Creates a mutable store that wraps an existing double array. Null values
	 * are not supported by the returned store.
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
	 * Creates a mutable store that wraps an existing double array. Where the
	 * permitted by the specified nullity, values may subsequently be removed
	 * from the store by setting an index value to null. Such operations will
	 * not modify the wrapped array; the null status of an index may be obtained
	 * from the {@link Store#population()}.
	 *
	 * @param nullity
	 *            how null values are to be supported
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Double> doublesWithNullity(StoreNullity<Double> nullity, double... values) {
		checkValuesNotNull(values);
		return nullity.nullGettable() ? new NullPrimitiveStore.DoubleStore(values) : new PrimitiveStore.DoubleStore(values, nullity);
	}

	// package scoped methods

	static IllegalStateException immutableException() {
		return new IllegalStateException("immutable");
	}

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
		return toArray(store, store.size(), null);
	}

	static<V> V[] toArray(Store<V> store, int length, V nullValue) {
		@SuppressWarnings("unchecked")
		V[] vs = (V[]) Array.newInstance(store.valueType(), length);
		return copyIntoArray(store, vs, nullValue);
	}

	static<V> V[] copyIntoArray(Store<V> store, V[] vs, V nullValue) {
		int length = vs.length;
		int limit = Math.min( store.size(), length);
		for (int i = 0; i < limit; i++) {
			vs[i] = store.get(i);
		}
		if (limit < length && nullValue != null) {
			Arrays.fill(vs, limit, length, nullValue);
		}
		return vs;
	}

	static void checkValuesNotNull(Object values) {
		if (values == null) throw new IllegalArgumentException("null values");
	}

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
