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

	/**
	 * The type of stores created with this storage.
	 *
	 * @return the type assigned to new stores
	 */

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
	 * supplied by {@link StoreType#nullValue()}.
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
	 * null values will be handled as per {@link #type()}.
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
	 * with {@link StoreType#nullValue()} if necessary. The returned store is
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
		if (size == 0) return newStore(0);
		if (!mutable) return mutable().newCopyOf(store).immutableView();
		// workaround possibility that it may not be possible to create a null filled store before filling
		// if store[0] is null, this will fail, but that's okay, copy would fail anyway
		Store<V> copy = type.nullSettable ? newStore(size) : newStore(size, store.get(0));
		copy.setStore(0, store);
		return copy;
	}

}