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

/**
 * <p>
 * Implementations of this interface are able to create {@link Store} instances
 * with a specified size.
 *
 * <p>
 * Static methods are provided for creating common and useful types of storage.
 * None of the stores originating from these methods are suitable for use by
 * multiple concurrent threads. Access in those circumstances must be externally
 * synchronized.
 *
 * @author Tom Gibara
 *
 * @param <V>
 *            the type of values to be stored
 */
public interface Storage<V> {

	/**
	 * Genericized storage backed by <code>Object</code> arrays. The storage
	 * returned by this method <em>will</em> support null values. This is a
	 * convenience method that is equivalent to calling
	 * {@link #generic(Class, Object)} with an empty null value.
	 *
	 * @param <V>
	 *            the type of values to be stored
	 * @return genericized storage
	 */
	static <V> Storage<V> generic() {
		return generic(null);
	}

	/**
	 * Genericized storage backed by <code>Object</code> arrays. The storage
	 * returned by this method <em>will</em> support null values <em>if</em> the
	 * supplied <code>nullValue</code> is null, otherwise it <em>will not</em>
	 * allow null values and the initial value at every index will be the
	 * specified <code>nullValue</code>.
	 *
	 * @param nullValue
	 *            the value that stands-in for absent values in the store, never
	 *            itself null
	 * @param <V>
	 *            the type of values to be stored
	 * @return genericized storage
	 */
	@SuppressWarnings("unchecked")
	static <V> Storage<V> generic(V nullValue) {
		if (nullValue == null) {
			return size -> (Store<V>) new NullArrayStore<>(new Object[size], 0);
		} else {
			return size -> new ArrayStore<V>((Class<V>)Object.class, size, nullValue);
		}
	}

	/**
	 * <p>
	 * Storage backed by typed arrays. The storage returned by this method
	 * <em>will</em> support null values. This is a convenience method that is
	 * equivalent to calling {@link #typed(Class, Object)} with a null value.
	 * 
	 * <p>
	 * Specifying a primitive type will result in storage backed by arrays of
	 * primitives. Such stores provide greater type safety than those created by
	 * genericized storage. In some contexts this will provide a very
	 * significant reduction in the memory required to store values.
	 *
	 * @param type
	 *            the type of the values to be stored
	 * @param <V>
	 *            the type of values to be stored
	 * @throws IllegalArgumentException
	 *             if the supplied type is null
	 * @return typed storage
	 * @see #typed(Class, Optional)
	 */
	static <V> Storage<V> typed(Class<V> type) throws IllegalArgumentException {
		return typed(type, null);
	}

	/**
	 * <p>
	 * Storage backed by typed arrays. The storage returned by this method
	 * <em>will</em> support null values <em>if</em> the supplied
	 * <code>nullValue</code> is null, otherwise it <em>will not</em> allow
	 * null values and the initial value at every index will be the specified
	 * <code>nullValue</code>.
	 * 
	 * <p>
	 * Specifying a primitive type will result in storage backed by arrays of
	 * primitives. Such stores provide greater type safety than those created by
	 * genericized storage. In some contexts this will provide a very
	 * significant reduction in the memory required to store values.
	 *
	 * @param type
	 *            the type of the values to be stored
	 * @param nullValue
	 *            the value that stands-in for absent values in the store, or
	 *            null
	 * @param <V>
	 *            the type of values to be stored
	 * @throws IllegalArgumentException
	 *             if the type or the initial value is null
	 * @return typed storage
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <V> Storage<V> typed(Class<V> type, V nullValue) throws IllegalArgumentException {
		if (type == null) throw new IllegalArgumentException("null type");
		if (nullValue == null) {
			if (type.isEnum()) return new NullEnumStorage(type);
			if (type.isPrimitive()) return size -> NullPrimitiveStore.newStore(type, size);
			return size -> new NullArrayStore<>(type, size);
		} else {
			if (type.isEnum()) return new EnumStorage(type, (Enum) nullValue);
			if (type.isPrimitive()) return size -> PrimitiveStore.newStore(type, size, nullValue);
			return size -> new ArrayStore<>(type, size, nullValue);
		}
	}

	/**
	 * <p>
	 * Genericized storage using weak references. The storage returned by this
	 * method <em>will</em> allow null values.
	 * 
	 * <p>
	 * As a consequence of GC activity, sizes reported by the weak stores may
	 * overestimate the number of values stored.
	 *
	 * @param <V>
	 *            the type of values to be stored
	 * @return weak storage
	 */
	static <V> Storage<V> weak() {
		return size -> new WeakRefStore<>(size);
	}

	/**
	 * <p>
	 * Genericized storage using soft references. The storage returned by this
	 * method <em>will</em> allow null values.
	 * 
	 * <p>
	 * As a consequence of GC activity, sizes reported by the soft stores may
	 * overestimate the number of values stored.
	 *
	 * @param <V>
	 *            the type of values to be stored
	 * @return soft storage
	 */
	static <V> Storage<V> soft() {
		return size -> new SoftRefStore<>(size);
	}

	/**
	 * <p>
	 * Storage that packs bounded non-negative integer values into minimal bit
	 * sizes. Such storage may be useful in situations where a very large number
	 * of small integer values need to be stored without occupying more memory
	 * than is needed. The range must be less than
	 * <code>Integer.MAX_VALUE</code>
	 *
	 * <p>
	 * Generally values in a range are packed linearly using the least number of
	 * bits needed to represent them individually. However, in the present
	 * implementation, ternary values ([0,1,2] or [null, 0,1]) and quinary
	 * values ([0,1,2,3,4] or [null, 0, 1, 2, 3]) are treated specially to avoid
	 * underutilized memory. Ternary storage requires 8 bits for every 5 values
	 * and quinary storage requires 7 bits for every 3 values. As a result, the
	 * performance of ternary and quinary storage may degraded in some
	 * applications. In any such case, it is possible to use a larger range to
	 * switch to a regular linear bit-packing strategy.
	 *
	 * @param range
	 *            defines the range <code>[0..range)</code> that small values
	 *            may take in this store
	 * @param nullsAllowed
	 *            whether the returned storage will accept null values
	 * @return small value storage
	 */
	static Storage<Integer> smallValues(int range, boolean nullsAllowed) {
		if (range < 0) throw new IllegalArgumentException("negative range");
		if (range == Integer.MAX_VALUE) throw new IllegalArgumentException("range too large");
		if (nullsAllowed) {
			return SmallValueStore.newNullStorage(range);
		} else {
			return SmallValueStore.newStorage(range);
		}
	}

	/**
	 * Creates a new store with the requested size
	 *
	 * @param size
	 *            the required size
	 * @throws IllegalArgumentException
	 *             if the size is negative
	 * @return an new store
	 */
	Store<V> newStore(int size) throws IllegalArgumentException;

}
