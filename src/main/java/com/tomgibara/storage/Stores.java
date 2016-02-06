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
	 * Creates a mutable store that wraps an existing array. Supplying an array
	 * containing nulls and specifying that nulls are not allowed is likely to
	 * cause malfunction.
	 * 
	 * @param values
	 *            the values of the store
	 * @param nullsAllowed
	 *            whether the returned storage will accept null values
	 * @param <V>
	 *            the type of values to be stored
	 * @return a store that mediates access to the array
	 */
	@SafeVarargs
	public static <V> Store<V> objects(boolean nullsAllowed, V... values) {
		checkValuesNotNull(values);
		return nullsAllowed ? new NullArrayStore<>(values) : new ArrayStore<>(values);
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
	public static <V> Store<V> objects(int count, V... values) {
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
	 * @param nullsAllowed
	 *            whether the returned storage will accept null values
	 * @param <V>
	 *            the type of values to be stored
	 * @return a store that returns values from array
	 */
	@SafeVarargs
	public static <V> Store<V> immutableObjects(boolean nullsAllowed, V... values) {
		checkValuesNotNull(values);
		return new ImmutableArrayStore<>(values, nullsAllowed);
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
	public static <V> Store<V> immutableObjects(int count, V... values) {
		if (count < 0) throw new IllegalArgumentException("negative size");
		checkValuesNotNull(values);
		return new ImmutableArrayStore<>(values, count);
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
		return new PrimitiveStore.ByteStore(values);
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
	public static Store<Byte> bytesAndNull(byte... values) {
		checkValuesNotNull(values);
		return new NullPrimitiveStore.ByteStore(values);
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
		return new PrimitiveStore.ShortStore(values);
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
	public static Store<Short> shortsAndNull(short... values) {
		checkValuesNotNull(values);
		return new NullPrimitiveStore.ShortStore(values);
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
		return new PrimitiveStore.IntegerStore(values);
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
	public static Store<Integer> intsAndNull(int... values) {
		checkValuesNotNull(values);
		return new NullPrimitiveStore.IntegerStore(values);
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
		return new PrimitiveStore.LongStore(values);
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
	public static Store<Long> longAndNull(long... values) {
		checkValuesNotNull(values);
		return new NullPrimitiveStore.LongStore(values);
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
		return new PrimitiveStore.BooleanStore(values);
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
	public static Store<Boolean> booleansAndNull(boolean... values) {
		checkValuesNotNull(values);
		return new NullPrimitiveStore.BooleanStore(values);
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
		return new PrimitiveStore.CharacterStore(values);
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
	public static Store<Character> charsAndNull(char... values) {
		checkValuesNotNull(values);
		return new NullPrimitiveStore.CharacterStore(values);
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
		return new PrimitiveStore.FloatStore(values);
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
	public static Store<Float> floatsAndNull(float... values) {
		checkValuesNotNull(values);
		return new NullPrimitiveStore.FloatStore(values);
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
		return new PrimitiveStore.DoubleStore(values);
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
	public static Store<Double> doublesAndNull(double... values) {
		checkValuesNotNull(values);
		return new NullPrimitiveStore.DoubleStore(values);
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
