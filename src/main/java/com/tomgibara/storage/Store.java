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
import java.util.Spliterator;
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
 * Due to the provision of default methods, only the methods {@link #size()} and
 * {@link #get(int)} need to be implemented to provide an immutable store
 * implementation. If a store is mutable, the methods {@link #set(int, Object)}
 * and {@link #isMutable()} must also be implemented.
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
	 * @see #type()
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
	 * @see #type()
	 */
	default boolean isNull(int index) {
		return get(index) != null;
	}

	/**
	 * Specifies the type of values stored by this store and how null values are
	 * supported.
	 *
	 * @return the store type
	 */
	default StoreType<V> type() {
		return StoreType.generic();
	}

	/**
	 * Stores a value in the store. In cases where the storage of null value is
	 * supported, storing null will result in no value being associated with the
	 * specified index. Some store implementations do not support null values,
	 * in this case the null value may be substituted by an alternative value,
	 * or simply rejected, as determined by the store type.
	 *
	 * @param index
	 *            the index at which to store the value
	 * @param value
	 *            the value to store, or null to remove any previous value
	 * @return the previously stored value, or null
	 * @throws IllegalArgumentException
	 *             if, in general, the value cannot be stored in this store, and
	 *             in particular, if the value is null and the store type
	 *             disallows the setting of null values
	 * @see #type()
	 */
	default V set(int index, V value) {
		throw immutableException();
	}

	/**
	 * <p>
	 * Indicates whether the supplied value can be set as a value on this store.
	 * 
	 * <p>
	 * The default implementation checks that the value satisfies the value-type
	 * and null constraints of the store type. Some stores may have further
	 * restrictions on the values they may contain.
	 * 
	 * @param value
	 *            some value
	 * @return true if and only if the value is valid for the
	 *         {@link #set(int, Object)} method on this store.
	 */
	default boolean isSettable(Object value) {
		return type().checkValue(value);
	}

	/**
	 * Removes all stored values; equivalent to setting each indexed value to
	 * null. This operation is prohibited if the store type disallows null
	 * values.
	 *
	 * @throws IllegalStateException
	 *             if the store does not allow null values to be set.
	 * @see #type()
	 */
	default void clear() throws IllegalStateException {
		try {
			int size = size();
			for (int i = 0; i < size; i++) {
				set(i, null);
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("null not supported", e);
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
		if (!type().nullGettable) return false; // there can be no nulls
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
	 * <p>
	 * A mutable detached copy of this store with the specified size. Detached
	 * means that changes to the returned store will not affect the copied
	 * store. The new size may be smaller, larger or even the same as the copied
	 * store. This is an analogue of the
	 * <code>Arrays.copyOf(original, length)</code>.
	 *
	 * <p>
	 * It is not possible to create enlarged copies of stores on which null
	 * values cannot be set. However, enlarged copies can be created from stores
	 * that substitute non-null values for null; indices beyond the original
	 * store size are filled with the substitute value.
	 *
	 * @param newSize
	 *            the size required in the new store
	 * @return a copy of this store with the specified size
	 * @see #mutableCopy()
	 */
	default Store<V> resizedCopy(int newSize) {
		StoreType<V> type = type();
		if (type.nullGettable) {
			return new NullArrayStore<>(Stores.toArray(this, newSize, null), count());
		}
		if (!type.nullSettable && newSize > size()) {
			throw new IllegalArgumentException("cannot create copy with greater size, no null value");
		}
		return new ArrayStore<>(Stores.toArray(this, newSize, type.nullValue), type);
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
	 * stores for which {@link StoreType#nullGettable()} is false, all bits
	 * are guaranteed to equal one.
	 *
	 * @return bits indicating the indices at which values are present
	 */
	default BitStore population() {
		if (!type().nullGettable) return Bits.oneBits(size());
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
	 * {@link StoreType#nullGettable()} is true. The list supports value
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
	default Store<V> asTransformedBy(UnaryOperator<V> op) {
		return asTransformedBy(Mapping.fromUnaryOperator(type().valueType, op));
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
	 *            the type of values in the returned store
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
	 * on the store in contrast to {@link #asTransformedBy(Mapping)}.
	 *
	 * <p>
	 * Note that the supplied function must preserve null. That is
	 * <code>fn(a) == null</code> if and only if <code>a == null</code>.
	 *
	 * @param <W>
	 *            the type of values in the returned store
	 * @param fn
	 *            a bijective function over the store elements
	 *
	 * @return a view of this store under the specified function
	 * @see #asTransformedBy(Mapping)
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
	 * @param <W>
	 *            the type of values in the transformed iterator
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
	 * @param <W>
	 *            the type of values in the transformed iterator
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
		if (type().nullGettable) {
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
		if (type().nullGettable) {
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

	@Override
	public default Spliterator<V> spliterator() {
		return new StoreSpliterator<>(this);
	}

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
		return new ImmutableArrayStore<>(Stores.toArray(this), count(), type());
	}

	@Override
	default Store<V> immutableView() {
		return new ImmutableStore<>(this);
	}

}
