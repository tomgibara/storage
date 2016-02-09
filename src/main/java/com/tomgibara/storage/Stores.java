package com.tomgibara.storage;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;

/**
 * Collects static methods for creating new stores that wrap existing arrays.
 *
 * @author Tom Gibara
 *
 */
public final class Stores {

	// public scoped methods

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
	public static <V> Store<V> objects(Optional<V> nullValue, V... values) {
		checkNullValueNotNull(values);
		checkValuesNotNull(values);
		return nullValue.isPresent() ? new ArrayStore<>(values, nullValue.get()) : new NullArrayStore<>(values);
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
	public static <V> Store<V> objectsAndNull(V... values) {
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
	public static <V> Store<V> immutableObjects(Optional<V> nullValue, V... values) {
		checkNullValueNotNull(values);
		checkValuesNotNull(values);
		return new ImmutableArrayStore<>(values, nullValue.orElse(null));
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
	public static <V> Store<V> immutableObjectsAndNull(V... values) {
		checkValuesNotNull(values);
		return new ImmutableArrayStore<>(values, null);
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

	/**
	 * Creates a mutable store that wraps an existing byte array.
	 *
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	public static Store<Byte> bytes(byte... values) {
		checkValuesNotNull(values);
		return new PrimitiveStore.ByteStore(values, (byte) 0);
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
		return new PrimitiveStore.ShortStore(values, (short) 0);
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
		return new PrimitiveStore.IntegerStore(values, (int) 0);
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
		return new PrimitiveStore.LongStore(values, (long) 0);
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
		return new PrimitiveStore.BooleanStore(values, false);
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
		return new PrimitiveStore.CharacterStore(values, (char) 0);
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
		return new PrimitiveStore.FloatStore(values, (float) 0);
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
		return new PrimitiveStore.DoubleStore(values, (double) 0);
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

	static void checkNullValueNotNull(Object nullValue) {
		if (nullValue == null) throw new IllegalArgumentException("null nullValue");
	}

	static <V> V[] resizedCopyOf(V[] vs, int newSize, V v) {
		if (v == null) return Arrays.copyOf(vs, newSize);
		int oldSize = vs.length;
		vs = Arrays.copyOf(vs, newSize);
		if (newSize > oldSize) Arrays.fill(vs, oldSize, newSize, v);
		return vs;
	}
	
	// non-constructor

	private Stores() {}
}
