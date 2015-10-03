package com.tomgibara.storage;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Consumer;

import com.tomgibara.bits.AbstractBitStore;
import com.tomgibara.bits.BitStore;
import com.tomgibara.fundament.Mutability;
import com.tomgibara.fundament.Transposable;

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
 * {@link #valueType()}, {@link #capacity()} and
 * {@link #get(int)} need to be implemented to provide an immutable store
 * implementation. If a store is mutable, the methods {@link #set(int, Object)}
 * and {@link #isMutable()} must also be implemented.
 * 
 * @author Tom Gibara
 *
 * @param <V>
 *            the type of the values stored
 */
public interface Store<V> extends Mutability<Store<V>>, Transposable {

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
		int capacity = capacity();
		for (int i = 0; i < capacity; i++) {
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
	 * The number of non-null values in the store.
	 * 
	 * @return the size of the store
	 */
	default int size() {
		return population().ones().count();
	}
	
	/**
	 * Bits indicating which values are null. A zero at an index indicates that
	 * there is no value stored at that index. The returned bits are immutable
	 * but will change as values are added and removed from the store.
	 * 
	 * @return bits indicating the indices at which values are present
	 */
	default BitStore population() {
		return new AbstractBitStore() {

			@Override
			public int size() {
				return capacity();
			}

			@Override
			public boolean getBit(int index) {
				return get(index) != null;
			}

		};
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
				return Store.this.capacity();
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

	// transposable methods
	
	default public void transpose(int i, int j) {
		if (i == j) return;
		V v = get(i);
		set(i, get(j));
		set(j, v);
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
