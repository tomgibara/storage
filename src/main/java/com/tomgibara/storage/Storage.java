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

import com.tomgibara.storage.RefStore.RefStorage;

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

//	/**
//	 * Genericized storage backed by <code>Object</code> arrays. The storage
//	 * returned by this method supports setting and getting null values. This is
//	 * a convenience method that is equivalent to calling
//	 * {@link #generic(StoreNullity)} with
//	 * {@link StoreNullity#settingNullAllowed()}.
//	 *
//	 * @param <V>
//	 *            the type of values to be stored
//	 * @return genericized storage
//	 */
//	static <V> Storage<V> generic() {
//		return generic(settingNullAllowed());
//	}
//
//	/**
//	 * Genericized storage backed by <code>Object</code> arrays. The storage
//	 * returned by this method supports null values as per the supplied nullity.
//	 *
//	 * @param nullity
//	 *            determines how null values are handled by the stores
//	 * @param <V>
//	 *            the type of values to be stored
//	 * @return genericized storage
//	 */
//	@SuppressWarnings("unchecked")
//	static <V> Storage<V> generic(StoreNullity<V> nullity) {
//		if (nullity.nullGettable()) {
//			return (Storage<V>) NullArrayStore.mutableObjectStorage;
//		} else {
//			return ArrayStore.mutableStorage((Class<V>) Object.class, nullity);
//		}
//	}
//
//	/**
//	 * <p>
//	 * Storage backed by typed arrays. The storage returned by this method
//	 * supports setting and getting null values. This is a convenience method
//	 * that is equivalent to calling {@link #typed(Class, StoreNullity)} with
//	 * {@link StoreNullity#settingNullAllowed()}.
//	 *
//	 * <p>
//	 * Specifying a primitive type will result in storage backed by arrays of
//	 * primitives. Such stores provide greater type safety than those created by
//	 * genericized storage. In some contexts this will provide a very
//	 * significant reduction in the memory required to store values.
//	 *
//	 * @param type
//	 *            the type of the values to be stored
//	 * @param <V>
//	 *            the type of values to be stored
//	 * @throws IllegalArgumentException
//	 *             if the supplied type is null
//	 * @return typed storage
//	 * @see #typed(Class, StoreNullity)
//	 */
//	static <V> Storage<V> typed(Class<V> type) throws IllegalArgumentException {
//		return typed(type, settingNullAllowed());
//	}
//
//	/**
//	 * <p>
//	 * Storage backed by typed arrays. The storage
//	 * returned by this method supports null values as per the supplied nullity.
//	 *
//	 * <p>
//	 * Specifying a primitive type will result in storage backed by arrays of
//	 * primitives. Such stores provide greater type safety than those created by
//	 * genericized storage. In some contexts this will provide a very
//	 * significant reduction in the memory required to store values.
//	 *
//	 * @param type
//	 *            the type of the values to be stored
//	 * @param nullity
//	 *            determines how null values are handled by the stores
//	 * @param <V>
//	 *            the type of values to be stored
//	 * @throws IllegalArgumentException
//	 *             if the type or the initial value is null
//	 * @return typed storage
//	 */
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	static <V> Storage<V> typed(Class<V> type, StoreNullity<V> nullity) throws IllegalArgumentException {
//		if (type == null) throw new IllegalArgumentException("null type");
//		if (nullity.nullGettable()) {
//			if (type.isEnum()) return new NullEnumStorage(type);
//			if (type.isPrimitive()) return NullPrimitiveStore.newStorage(type);
//			return NullArrayStore.mutableStorage(type);
//		} else {
//			if (type.isEnum()) return new EnumStorage(type, nullity);
//			if (type.isPrimitive()) return PrimitiveStore.newStorage(type, nullity);
//			return ArrayStore.mutableStorage(type, nullity);
//		}
//	}

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
		return (RefStorage<V>) (size, value) -> new WeakRefStore<>(size, value);
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
		return (RefStorage<V>) (size, value) -> new SoftRefStore<>(size, value);
	}

//	/**
//	 * <p>
//	 * Storage that packs bounded non-negative integer values into minimal bit
//	 * sizes. Such storage may be useful in situations where a very large number
//	 * of small integer values need to be stored without occupying more memory
//	 * than is needed. The range must be less than
//	 * <code>Integer.MAX_VALUE</code>
//	 *
//	 * <p>
//	 * Generally values in a range are packed linearly using the least number of
//	 * bits needed to represent them individually. However, in the present
//	 * implementation, ternary values ([0,1,2] or [null, 0,1]) and quinary
//	 * values ([0,1,2,3,4] or [null, 0, 1, 2, 3]) are treated specially to avoid
//	 * underutilized memory. Ternary storage requires 8 bits for every 5 values
//	 * and quinary storage requires 7 bits for every 3 values. As a result, the
//	 * performance of ternary and quinary storage may degraded in some
//	 * applications. In any such case, it is possible to use a larger range to
//	 * switch to a regular linear bit-packing strategy.
//	 *
//	 * @param range
//	 *            defines the range <code>[0..range)</code> that small values
//	 *            may take in this store
//	 * @param nullity
//	 *            determines how null values are handled by the stores
//	 * @return small value storage
//	 */
//	static Storage<Integer> smallValues(int range, StoreNullity<Integer> nullity) {
//		if (range <= 0) throw new IllegalArgumentException("non positive range");
//		if (range == Integer.MAX_VALUE) throw new IllegalArgumentException("range too large");
//		if (nullity == null) throw new IllegalArgumentException("null nullity");
//
//		if (nullity.nullGettable()) {
//			return SmallValueStore.newNullStorage(range);
//		}
//		if (nullity.nullSettable()) {
//			int nv = nullity.nullValue();
//			if (nv < 0) throw new IllegalArgumentException("negative nullValue");
//			if (nv >= range) throw new IllegalArgumentException("nullValue meets or exceeds range");
//			return SmallValueStore.newStorage(range, nv);
//		}
//		return SmallValueStore.newStorage(range, -1);
//	}

	/**
	 * Whether the new stores created with this storage are mutable
	 *
	 * @return true if newly created stores are mutable, false otherwise
	 */
	default boolean isStorageMutable() { return true; }

	/**
	 * A version of this storage that creates mutable stores, or the storage
	 * itself if {@link #isStorageMutable()} is already {@code true}.
	 *
	 * @return mutable storage
	 * @see #isStorageMutable()
	 */
	default Storage<V> mutable() {
		return isStorageMutable() ? this : new ImmutableStorage<>(this);
	}

	/**
	 * A version of this storage that creates immutable stores, or the storage
	 * itself if {@link #isStorageMutable()} is already {@code false}.
	 *
	 * @return mutable storage
	 * @see #isStorageMutable()
	 */
	default Storage<V> immutable() {
		return isStorageMutable() ? new MutableStorage<>(this) : this;
	}

//	/**
//	 * The constraints that apply to stores created with this storage.
//	 *
//	 * @return the nullability that applies to stores
//	 */
//	default StoreNullity<V> nullity() {
//		return StoreNullity.settingNullAllowed();
//	}
//
//	/**
//	 * The type of values stored. Some store implementations may store their
//	 * values as primitives and may choose to report primitive classes. Other
//	 * implementations may treat all values as object types and will return
//	 * <code>Object.class</code> despite ostensibly having a genericized type.
//	 *
//	 * @return the value type
//	 */
//	Class<V> valueType();

	StoreType<V> type();

	/**
	 * <p>
	 * Creates a new store with the requested size. A convenience method
	 * equivalent to passing a null value to {@link #newStore(int, Object)}.
	 *
	 * <p>
	 * The returned store is mutable precisely when {@link #isStorageMutable()}
	 * returns true.
	 *
	 * @param size
	 *            the required size
	 * @throws IllegalArgumentException
	 *             if the size is negative
	 * @return a new store
	 */
	default Store<V> newStore(int size) throws IllegalArgumentException {
		return newStore(size, null);
	}

	/**
	 * <p>
	 * Creates a new store with the requested size. The supplied value is
	 * assigned to every index of the store, unless the value is null, in which
	 * case every index either remains unassigned or is assigned the value
	 * supplied by {@link StoreNullity#nullValue()}.
	 * 
	 * <p>
	 * The returned store is mutable precisely when {@link #isStorageMutable()}
	 * returns true.
	 *
	 * @param size
	 *            the required size
	 * @param value
	 *            the value to be assigned to every
	 * @throws IllegalArgumentException
	 *             if the size is negative
	 * @return a new store
	 */
	Store<V> newStore(int size, V value) throws IllegalArgumentException;

	/**
	 * <p>
	 * Creates a new store containing values from the supplied array. The size
	 * of the returned store will equal the length of the supplied array and
	 * null values will be handled as per {@link #nullity()}.
	 *
	 * <p>
	 * The returned store is an independent copy of the supplied array and is
	 * mutable precisely when {@link #isStorageMutable()} returns true.
	 *
	 * @param values
	 *            an array of values
	 * @return a mutable store containing the supplied values
	 */
	default Store<V> newStoreOf(@SuppressWarnings("unchecked") V... values) {
		if (values == null) throw new IllegalArgumentException("null values");
		return newCopyOf(new NullArrayStore<>(values));
	}

	/**
	 * <p>
	 * Creates a copy of the supplied store. The size of the returned store
	 * equals the size of the supplied store and null values will be substituted
	 * with {@link StoreNullity#nullValue()} if necessary. The returned store is
	 * an independent copy of the one supplied.
	 *
	 * <p>
	 * The returned store is mutable precisely when {@link #isStorageMutable()}
	 * returns true.
	 *
	 * @param store
	 *            the store to be copied
	 * @return a copy of the supplied store.
	 */
	default Store<V> newCopyOf(Store<V> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		int size = store.size();
		boolean mutable = isStorageMutable();
		StoreType<V> type = type();
		if (size == 0) return new EmptyStore<>(type, mutable);
		if (!mutable) return mutable().newCopyOf(store).immutableView();
		// workaround possibility that it may not be possible to create a null filled store before filling
		Store<V> copy = type.nullSettable ? newStore(size) : newStore(size, store.get(0));
		for (int i = 0; i < store.size(); i++) {
			copy.set(i, store.get(i));
		}
		return copy;
	}

}