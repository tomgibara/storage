package com.tomgibara.storage;

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
 * @author Tom Gibara
 *
 * @param <V>
 *            the type of the values stored
 */
public interface Store<V> extends Mutability<Store<V>> {

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
	V set(int index, V value);

	/**
	 * Removes all stored values.
	 */
	void clear();
	
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

	// mutability methods
	
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
