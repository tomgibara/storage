package com.tomgibara.storage;

import java.lang.reflect.Array;

/**
 * Collects static methods for creating new stores that wrap existing arrays.
 * 
 * @author Tom Gibara
 *
 */
public final class Stores {

	// public scoped methods

	/**
	 * Creates a mutable store that wraps an existing array.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static <V> Store<V> newStore(V[] values) {
		checkValuesNotNull(values);
		return new ArrayStore<>(values);
	}

	/**
	 * Creates a mutable store that wraps an existing array. This method may be
	 * used if the size (the number of non-null values) is already known.
	 * Supplying an incorrect size is likely to cause malfunction.
	 * 
	 * @param values
	 *            the values of the store
	 * @param size
	 *            the number of non-null values in the array
	 * @return a store that mediates access to the array
	 */
	public static <V> Store<V> newStore(V[] values, int size) {
		checkValuesNotNull(values);
		if (size < 0) throw new IllegalArgumentException("negative size");
		return new ArrayStore<>(values, size);
	}
	
	/**
	 * Creates an immutable store that returns values from an existing array.
	 * The supplied array is not copied and must not be modified.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that returns values from array
	 */
	public static <V> Store<V> newImmutableStore(V[] values) {
		checkValuesNotNull(values);
		return new ImmutableArrayStore<>(values);
	}
	
	/**
	 * Creates an immutable store that returns values from existing array. The
	 * supplied array is not copied and must not be modified. This method may be
	 * used if the size (the number of non-null values) is already known.
	 * Supplying an incorrect size is likely to cause malfunction.
	 * 
	 * @param values
	 *            the values of the store
	 * @param size
	 *            the number of non-null values in the array
	 * @return a store that mediates access to the array
	 */
	public static <V> Store<V> newImmutableStore(V[] values, int size) {
		checkValuesNotNull(values);
		if (size < 0) throw new IllegalArgumentException("negative size");
		return new ImmutableArrayStore<>(values, size);
	}
	
	/**
	 * Creates a mutable store that wraps an existing byte array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link #population()}.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Byte> newStore(byte... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.ByteStore(values);
	}

	/**
	 * Creates a mutable store that wraps an existing short array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link #population()}.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Short> newStore(short... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.ShortStore(values);
	}

	/**
	 * Creates a mutable store that wraps an existing integer array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link #population()}.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Integer> newStore(int... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.IntegerStore(values);
	}

	/**
	 * Creates a mutable store that wraps an existing long array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link #population()}.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Long> newStore(long... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.LongStore(values);
	}

	/**
	 * Creates a mutable store that wraps an existing boolean array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link #population()}.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Boolean> newStore(boolean... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.BooleanStore(values);
	}

	/**
	 * Creates a mutable store that wraps an existing char array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link #population()}.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Character> newStore(char... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.CharacterStore(values);
	}

	/**
	 * Creates a mutable store that wraps an existing float array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link #population()}.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Float> newStore(float... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.FloatStore(values);
	}

	/**
	 * Creates a mutable store that wraps an existing double array. Values may
	 * subsequently be removed from the store by setting an index value to null.
	 * Such operations will not modify the wrapped array; the null status of an
	 * index may be obtained from the {@link #population()}.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Double> newStore(double... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.DoubleStore(values);
	}

	// package scoped methods
	
	static <V> int countNonNulls(V[] vs) {
		int sum = 0;
		for (V v : vs) if (v != null) sum++;
		return sum;
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

	// non-constructor
	
	private Stores() {}
}
