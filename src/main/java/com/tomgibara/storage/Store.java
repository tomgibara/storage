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

import static com.tomgibara.storage.Stores.immutableException;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.tomgibara.bits.AbstractBitStore;
import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Bijection;
import com.tomgibara.fundament.Mapping;
import com.tomgibara.fundament.Mutability;
import com.tomgibara.fundament.Transposable;

/**
 * <p>
 * Provides array-like storage of values. Stores are intended to provide a basis
 * for building more complex collection types. Like arrays they feature
 * fixed-length index-based access, but they also provide mutability control via
 * the {@link Mutability} interface.
 *
 * <p>
 * Note that stores <i>do not</i> support storing null values in a way that is
 * distinct from removal; assigning a null value to an index at which a
 * previously non-null value was stored is to remove that value.
 *
 * <p>
 * Due to the provision of default methods, only the methods
 * {@link #valueType()}, {@link #size()} and {@link #get(int)} need to be
 * implemented to provide an immutable store implementation. If a store is
 * mutable, the methods {@link #set(int, Object)} and {@link #isMutable()} must
 * also be implemented.
 *
 * <p>
 * Store implementations are expected to implement the Java object methods as
 * follows:
 *
 * <ul>
 * <li>Two stores are equal if their {@link #asList()} representations are
 * equal.
 * <li>The hashcode of a store is the hashcode of its list representation.
 * <li>The string representation of a store is equal to
 * <code>asList().toString()</code>.
 * </ul>
 *
 * <p>
 * {@link AbstractStore} is convenient base class that predefines this
 * behaviour.
 *
 * @author Tom Gibara
 *
 * @param <V>
 *            the type of the values stored
 */
public interface Store<V> extends Iterable<V>, Mutability<Store<V>>, Transposable {

	// store methods

	/**
	 * The type of values stored by this store. Some store implementations may
	 * store their values as primitives and may choose to report primitive
	 * classes. Other implementations may treat all values as object types and
	 * will return <code>Object.class</code> despite ostensibly having a
	 * genericized type.
	 *
	 * @return the value type
	 */
	Class<V> valueType();

	/**
	 * The greatest number of values that the store can contain. Valid indices
	 * range from <code>0 &lt;= i &lt; size</code>. Stores of size zero are
	 * possible.
	 *
	 * @return the size of store
	 */
	int size();

	/**
	 * Retrieves a value held in the store. The method will return null if there
	 * is no value associated with specified index. In some store
	 * implementations it may not be possible to associate no value with an
	 * index and this method will never return null.
	 *
	 * @param index
	 *            the index from which to retrieve the value
	 * @return the value stored at the specified index
	 * @see #nullity()
	 */
	V get(int index);

	/**
	 * Whether the value at the specified index is null. In many implementations
	 * this method may be faster than calling {@link #get(int)} and checking for
	 * null. In some store implementations it may not be possible to store nulls
	 * and this method will never return true.
	 *
	 * @param index
	 *            the index of the value to be compared to null
	 * @return true if and only if the value at the specified index is null
	 * @see #nullity()
	 */
	default boolean isNull(int index) {
		return get(index) != null;
	}

	/**
	 * Determines how null values are supported by this store
	 * 
	 * @return the support provided for null values
	 */
	default StoreNullity<V> nullity() {
		return StoreNullity.settingNullAllowed();
	}

	/**
	 * Stores a value in the store. In cases where the storage of null value is
	 * supported, storing null will result in no value being associated with the
	 * specified index. Some store implementations do not support null values,
	 * in this case the null value may be substituted by an alternative value,
	 * or simply rejected, as determined by the store nullity.
	 *
	 * @param index
	 *            the index at which to store the value
	 * @param value
	 *            the value to store, or null to remove any previous value
	 * @return the previously stored value, or null
	 * @throws IllegalArgumentException
	 *             if, in general, the value cannot be stored in this store, and
	 *             in particular, if the value is null and the nullity disallows
	 *             the setting of null values
	 * @see #nullity()
	 */
	default V set(int index, V value) {
		throw immutableException();
	}

	/**
	 * Removes all stored values. This operation may be prohibited or operate as
	 * per {@link #fill(Object)}, as per the store nullity.
	 * 
	 * @see #nullity()
	 */
	default void clear() {
		int size = size();
		for (int i = 0; i < size; i++) {
			set(i, null);
		}
	}

	/**
	 * Assigns every index the same value. Filling with null has the same effect
	 * as calling {@link #clear()}.
	 *
	 * @param value the value to be assigned to every index
	 */

	default void fill(V value) {
		if (!isMutable()) throw immutableException();
		if (value == null) clear();
		int size = size();
		for (int i = 0; i < size; i++) {
			set(i, value);
		}
	}

	/**
	 * Moves every non-null values to the least available index.
	 *
	 * @return whether store was mutated as a consequence of calling this method
	 */
	default boolean compact() {
		if (!isMutable()) throw immutableException();
		if (!nullity().nullGettable()) return false; // there can be no nulls
		int count = count();
		if (count == size()) return false; // this should be a cheap test

		int i = 0; // index to read from
		int j = 0; // index to write to
		while (j < count) {
			V value = get(i);
			if (value != null) {
				if (j < i) set(j, value);
				j++;
			}
			i++; // skip forwards
		}

		if (i == j) return false;
		while (j < i) {
			set(j++, null);
		}
		return true;
	}

	/**
	 * A mutable detached copy of this store with the specified size.
	 * Detached means that changes to the returned store will not affect the
	 * copied store. The new size may be smaller, larger or even the same as
	 * the copied store. This is an analogue of the
	 * <code>Arrays.copyOf(original, length)</code>.
	 *
	 * @param newSize
	 *            the size required in the new store
	 * @return a copy of this store with the specified size
	 * @see #mutableCopy()
	 */
	default Store<V> resizedCopy(int newSize) {
		StoreNullity<V> nullity = nullity();
		return nullity.nullGettable() ?
				new NullArrayStore<>(Stores.toArray(this, newSize), count()) :
				new ArrayStore<>(Stores.toArray(this, newSize), nullity);
	}

	/**
	 * The number of non-null values in the store.
	 *
	 * @return the number of non-null values in the store
	 */
	default int count() {
		return population().ones().count();
	}

	/**
	 * Bits indicating which values are null. A zero at an index indicates that
	 * there is no value stored at that index. The returned bits are immutable
	 * but will change as values are added and removed from the store. In
	 * stores for which {@link StoreNullity#nullGettable()} is false, all bits
	 * are guaranteed to equal one.
	 *
	 * @return bits indicating the indices at which values are present
	 */
	default BitStore population() {
		if (!nullity().nullGettable()) return Bits.oneBits(size());
		return new AbstractBitStore() {

			@Override
			public int size() {
				return Store.this.size();
			}

			@Override
			public boolean getBit(int index) {
				return !isNull(index);
			}

		};
	}

	/**
	 * Exposes the store as a list. The size of the list is equal to the size of
	 * the store. The list may contain null values if
	 * {@link StoreNullity#nullGettable()} is true. The list supports value
	 * mutation via <code>set</code>, but not appending via <code>add()</code>.
	 *
	 * @return a list backed by the values in the store.
	 */
	default List<V> asList() {
		return new AbstractList<V>() {

			@Override
			public V get(int index) {
				return Store.this.get(index);
			}

			@Override
			public V set(int index, V element) {
				if (!isMutable()) throw immutableException();
				return Store.this.set(index, element);
			}

			@Override
			public void clear() {
				if (!isMutable()) throw immutableException();
				Store.this.clear();
			}

			@Override
			public boolean add(V e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int size() {
				return Store.this.size();
			}

			@Override
			public void forEach(Consumer<? super V> action) {
				int size = size();
				for (int i = 0; i < size; i++) {
					action.accept(get(i));
				}
			}
		};
	}

	/**
	 * <p>
	 * Derives a store by applying an operator over the store values. It
	 * provides a live view of the original store. The mutability of the
	 * returned store matches the mutability of this store, but only null values
	 * may be set.
	 *
	 * <p>
	 * Note that the supplied operator must preserve null. That is
	 * <code>op(a) == null</code> if and only if <code>a == null</code>.
	 *
	 * @param op
	 *            an operator over the store elements
	 *
	 * @return a view of this store under the specified operator
	 * @see #asTransformedBy(Mapping)
	 */
	default Store<V> asTransformedBy(UnaryOperator<V> fn) {
		return asTransformedBy(Mapping.fromUnaryOperator(valueType(), fn));
	}

	/**
	 * <p>
	 * Derives a store by applying a mapping over the store values. It provides
	 * a live view of the original store. The mutability of the returned store
	 * matches the mutability of this store, but only null values may be set.
	 *
	 * <p>
	 * Note that the supplied mapping must preserve null. That is
	 * <code>fn(a) == null</code> if and only if <code>a == null</code>.
	 *
	 * @param fn
	 *            a mapping over the store elements
	 * @param <W>
	 *            the type of value in the returned store
	 *
	 * @return a view of this store under the specified mapping
	 * @see #asTransformedBy(UnaryOperator)
	 * @see #asTransformedBy(Bijection)
	 */
	default <W> Store<W> asTransformedBy(Mapping<V, W> fn) {
		if (fn == null) throw new IllegalArgumentException("null fn");
		return new TransformedStore<V,W>(this, fn);
	}

	/**
	 * <p>
	 * Derives a store by applying a bijection over the store values. It
	 * provides a live view of the original store. The mutability of the
	 * returned store matches the mutability of this store. Bijectivity of the
	 * supplied transforming function has the consequence that values may be set
	 * on the store in contrast to {@link #asTransformedBy(Function)}.
	 *
	 * <p>
	 * Note that the supplied function must preserve null. That is
	 * <code>fn(a) == null</code> if and only if <code>a == null</code>.
	 *
	 * @param fn
	 *            a bijective function over the store elements
	 *
	 * @return a view of this store under the specified function
	 * @see #asTransformedBy(Function)
	 */
	default <W> Store<W> asTransformedBy(Bijection<V, W> fn) {
		if (fn == null) throw new IllegalArgumentException("null fn");
		return new TransformedStore<V,W>(this, fn);
	}

	/**
	 * Creates an iterator over the non-null values of the store under the image
	 * of the supplied function. On mutable stores the returned iterator
	 * supports element removal.
	 *
	 *
	 * @param fn
	 *            a transforming function
	 * @return an iterator over transformed non-null values
	 */
	default <W> Iterator<W> transformedIterator(Function<V, W> fn) {
		if (fn == null) throw new IllegalArgumentException("null fn");
		return new StoreIterator.Transformed<>(this, fn);
	}

	/**
	 * Creates an iterator over the non-null values of the store under the image
	 * of the supplied binary function. The first argument to the function is
	 * the index of the value in the store and the second argument is the value
	 * itself. On mutable stores the returned iterator supports element removal.
	 *
	 * @param fn
	 *            a transforming function over index and value
	 * @return an iterator over transformed non-null values
	 */
	default <W> Iterator<W> transformedIterator(BiFunction<Integer, V, W> fn) {
		if (fn == null) throw new IllegalArgumentException("null fn");
		return new StoreIterator.BiTransformed<>(this, fn);
	}

	/**
	 * Copies the elements of this store into a new store created by the
	 * supplied storage. If the storage of every value in this store is not
	 * supported by the supplied storage, then an
	 * <code>IllegalArgumentException</code> is thrown.
	 * 
	 * @param storage
	 *            the storage used to create a copy of this store
	 * @return a copy
	 * @throws IllegalArgumentException
	 *             if the supplied storage cannot accommodate all of the values
	 *             in this store
	 * @see Storage#newCopyOf(Store)
	 */
	default Store<V> copiedBy(Storage<V> storage) throws IllegalArgumentException {
		if (storage == null) throw new IllegalArgumentException("null storage");
		return storage.newCopyOf(this);
	}

	// iterable methods

	/**
	 * Provides an iterator over the non-null values in the store.
	 *
	 * @return an iterator over the non-null values
	 */
	@Override
	default Iterator<V> iterator() {
		return new StoreIterator.Regular<>(this);
	}

	/**
	 * Performs the given action over all non-null values in the store.
	 *
	 * @param action
	 *            the action to be performed for each non-null value
	 */
	@Override
	public default void forEach(Consumer<? super V> action) {
		int size = size();
		if (nullity().nullGettable()) {
			for (int i = 0; i < size; i++) {
				V v = get(i);
				if (v != null) action.accept(v);
			}
		} else {
			for (int i = 0; i < size; i++) {
				action.accept(get(size));
			}
		}
	}

	/**
	 * Performs the given action over all non-null position values in the store.
	 *
	 * @param action
	 *            the action to be performed for each non-null value
	 */
	public default void forEach(BiConsumer<Integer, ? super V> action) {
		int size = size();
		if (nullity().nullGettable()) {
			for (int i = 0; i < size; i++) {
				V v = get(i);
				if (v != null) action.accept(i, v);
			}
		} else {
			for (int i = 0; i < size; i++) {
				action.accept(i, get(size));
			}
		}
	}

	//TODO implement spliterator

	// transposable methods

	@Override
	default public void transpose(int i, int j) {
		if (i == j) return;
		V v = get(i);
		set(i, get(j));
		set(j, v);
	}

	// mutability methods

	@Override
	default boolean isMutable() {
		return false;
	}

	@Override
	default Store<V> mutableCopy() {
		return resizedCopy(size());
	}

	@Override
	default Store<V> immutableCopy() {
		return new ImmutableArrayStore<>(Stores.toArray(this), count());
	}

	@Override
	default Store<V> immutableView() {
		return new ImmutableStore<>(this);
	}

}
