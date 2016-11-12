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

import java.lang.reflect.Array;
import java.util.Arrays;

import com.tomgibara.storage.StoreArrays.StoreArray;

/**
 * <p>
 * Static methods for creating new stores that wrap existing arrays.
 * 
 * <p>
 * The methods on this class allow
 *
 * @author Tom Gibara
 *
 */
public final class Stores {

	// private statics

	// package statics

	static final int BYTE    =  1;
	static final int FLOAT   =  2;
	static final int CHAR    =  3;
	static final int SHORT   =  4;
	static final int LONG    =  6;
	static final int INT     =  7;
	static final int DOUBLE  = 11;
	static final int BOOLEAN = 12;

	static int hash(Class<?> clss) {
		return (clss.getName().hashCode() >> 8) & 0xf;
	}

	static Class<?> primitiveClassFor(Class<?> clss) {
		switch (clss.getName()) {
		case "java.lang.Boolean"    : return boolean.class;
		case "java.lang.Character"  : return char.class;
		case "java.lang.Float"      : return float.class;
		case "java.lang.Double"     : return double.class;
		case "java.lang.Byte"       : return byte.class;
		case "java.lang.Short"      : return short.class;
		case "java.lang.Integer"    : return int.class;
		case "java.lang.Long"       : return long.class;
		default : return null;
		}
	}

	static Class<?> wrapperClassFor(Class<?> clss) {
		switch (clss.getName()) {
		case "boolean" : return Boolean.class;
		case "char"    : return Character.class;
		case "float"   : return Float.class;
		case "double"  : return Double.class;
		case "byte"    : return Byte.class;
		case "short"   : return Short.class;
		case "int"     : return Integer.class;
		case "long"    : return Long.class;
		default : return null;
		}
	}

	// public scoped methods

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
	public static <V> Store<V> objectsWithNullCount(int count, V... values) {
		if (count < 0) throw new IllegalArgumentException("negative count");
		checkValuesNotNull(values);
		return new NullArrayStore<>(values, count);
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
		return new ImmutableArrayStore<>(values, StoreType.of(componentType(values)));
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
	public static <V> Store<V> immutableObjectsWithNullCount(int count, V... values) {
		if (count < 0) throw new IllegalArgumentException("negative size");
		checkValuesNotNull(values);
		return new ImmutableArrayStore<>(values, count, StoreType.of(componentType(values)));
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
		return new PrimitiveStore.ByteStore(values, StoreType.BYTE_NN);
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
		return new PrimitiveStore.ShortStore(values, StoreType.SHORT_NN);
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
		return new PrimitiveStore.IntegerStore(values, StoreType.INT_NN);
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
		return new PrimitiveStore.LongStore(values, StoreType.LONG_NN);
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
		return new PrimitiveStore.BooleanStore(values, StoreType.BOOLEAN_NN);
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
		return new PrimitiveStore.CharacterStore(values, StoreType.CHAR_NN);
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
		return new PrimitiveStore.FloatStore(values, StoreType.FLOAT_NN);
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
		return new PrimitiveStore.DoubleStore(values, StoreType.DOUBLE_NN);
	}

	// package scoped methods

	static IllegalStateException immutableException() {
		return new IllegalStateException("immutable");
	}

	static <V> V[] typedArrayCopy(Class<V> type, V[] vs) {
		// fast path
		if (vs.getClass().getComponentType() == type) return vs.clone();
		// slow path
		@SuppressWarnings("unchecked")
		V[] copy = (V[]) Array.newInstance(type, vs.length);
		System.arraycopy(vs, 0, copy, 0, vs.length);
		return copy;
	}

	static <V> Object toPrimitiveArray(Store<V> store) {
		return toPrimitiveArray(store, store.size(), null);
	}

	@SuppressWarnings("unchecked")
	static<V> Object toPrimitiveArray(Store<V> store, int length, V nullValue) {
		StoreArray<Object, Object> sa = StoreArrays.forType(store.type().valueType);
		Object array = sa.create(length);
		sa.copyIntoArray((Store<Object>) store, array, nullValue);
		return array;
	}

	static <V> V[] toObjectArray(Store<V> store) {
		return toObjectArray(store, store.size(), null);
	}

	static<V> V[] toObjectArray(Store<V> store, int length, V nullValue) {
		Class<?> clss = store.type().valueType;
		if (clss.isPrimitive()) clss = wrapperClassFor(clss);
		@SuppressWarnings("unchecked")
		V[] vs = (V[]) Array.newInstance(clss, length);
		return copyIntoArray(store, vs, nullValue);
	}

	private static<V> V[] copyIntoArray(Store<V> store, V[] vs, V nullValue) {
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

	@SuppressWarnings("unchecked")
	static <V> Class<V> componentType(V[] vs) {
		return (Class<V>) vs.getClass().getComponentType();
	}

	// non-constructor

	private Stores() {}
}
