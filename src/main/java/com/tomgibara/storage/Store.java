package com.tomgibara.storage;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Consumer;

import com.tomgibara.fundament.Mutability;

/**
 * <p>
 * Provides array-like storage of values. Stores are intended to provide a basis
 * for building more complex collection types. Like arrays they feature
 * fixed-length index-based access, but they also provide mutability control via
 * the {@link Mutability} interface and can report a size which is distinct from
 * their capacity.
 * 
 * <p>
 * Note that stores <i>do not</i> support storing null values in a way that is
 * distinct from removal; assigning a null value to an index at which a
 * previously non-null value was stored is to remove that value.
 * 
 * <p>
 * Due to the provision of default methods, only the methods
 * {@link #valueType()}, {@link #capacity()}, {@link #size()} and
 * {@link #get(int)} need to be implemented to provide an immutable store
 * implementation. If a store is mutable, the methods {@link #set(int, Object)},
 * {@link #clear()} and {@link #isMutable()} must also be implemented.
 * 
 * @author Tom Gibara
 *
 * @param <V>
 *            the type of the values stored
 */
public interface Store<V> extends Mutability<Store<V>> {

	// statics
	
	/**
	 * Creates a mutable store that wraps an existing array.
	 * 
	 * @param values
	 *            the values of the store
	 * @return a store that mediates access to the array
	 */
	static <V> Store<V> newStore(V[] values) {
		if (values == null) throw new IllegalArgumentException("null values");
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
	static <V> Store<V> newStore(V[] values, int size) {
		if (values == null) throw new IllegalArgumentException("null values");
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
	static <V> Store<V> newImmutableStore(V[] values) {
		if (values == null) throw new IllegalArgumentException("null values");
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
	static <V> Store<V> newImmutableStore(V[] values, int size) {
		if (values == null) throw new IllegalArgumentException("null values");
		if (size < 0) throw new IllegalArgumentException("negative size");
		return new ImmutableArrayStore<>(values, size);
	}
	
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
	Class<? extends V> valueType();

	/**
	 * The greatest number of values that the store can contain. Valid indices
	 * range from <code>0 &lt;= i &lt; capacity</code>. Zero capacity stores are
	 * possible.
	 * 
	 * @return the capacity of store
	 */
	int capacity();

	/**
	 * The number of non-null values in the store.
	 * 
	 * @return the size of the store
	 */
	int size();
	
	/**
	 * Retrieves a value held in the store. The method will return null if there
	 * is no value associated with specified index.
	 * 
	 * @param index
	 *            the index from which to retrieve the value
	 * @return the value stored at the specified index
	 */
	V get(int index);
	
	/**
	 * Stores a value in the store. Storing null will result in no value being
	 * associated with the specified index
	 * 
	 * @param index
	 *            the index at which to store the value
	 * @param value
	 *            the value to store, or null to remove any previous value
	 * @return the previously stored value, or null
	 */
	default V set(int index, V value) {
		throw new IllegalStateException("immutable");
	}

	/**
	 * Removes all stored values.
	 */
	default void clear() {
		throw new IllegalStateException("immutable");
	}
	
	/**
	 * Assigns every index the same value. Filling with null has the same effect
	 * as calling {@link #clear()}.
	 *
	 * @param value the value to be assigned to every index
	 */

	default void fill(V value) {
		if (!isMutable()) throw new IllegalStateException("immutable");
		if (value == null) clear();
		int capacity = capacity();
		for (int i = 0; i < capacity; i++) {
			set(i, value);
		}
	}
	
	/**
	 * A mutable detached copy of this store with the specified capacity.
	 * Detached means that changes to the returned store will not affect the
	 * copied store. The new capacity may be smaller, larger or even the same as
	 * the copied store. This is an analogue of the
	 * <code>Arrays.copyOf(original, length)</code>.
	 * 
	 * @param newCapacity
	 *            the capacity required in the new store
	 * @return a copy of this store with the specified capacity
	 * @see #mutableCopy()
	 */
	default Store<V> withCapacity(int newCapacity) {
		return new ArrayStore<>(Stores.toArray(this, newCapacity), size());
	}

	/**
	 * Exposes the store as a list. The size of the list is equal to the
	 * capacity of the store with 'unset' elements exposed as null. The list
	 * supports value mutation via <code>set</code>, but not appending via
	 * <code>add()</code>.
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
				if (!isMutable()) throw new IllegalStateException("immutable");
				return Store.this.set(index, element);
			}

			@Override
			public void clear() {
				if (!isMutable()) throw new IllegalStateException("immutable");
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
				int capacity = capacity();
				for (int i = 0; i < capacity; i++) {
					action.accept(get(i));
				}
			}
		};
	}

	// mutability methods

	default boolean isMutable() {
		return false;
	}
	
	@Override
	default Store<V> mutable() {
		return isMutable() ? this : mutableCopy();
	}
	
	@Override
	default Store<V> mutableCopy() {
		return withCapacity(capacity());
	}

	@Override
	default Store<V> immutable() {
		return isMutable() ? immutableView() : this;
	}
	
	@Override
	default Store<V> immutableCopy() {
		return new ImmutableArrayStore<>(Stores.toArray(this), size());
	}

	@Override
	default Store<V> immutableView() {
		return new ImmutableStore<>(this);
	}

}
