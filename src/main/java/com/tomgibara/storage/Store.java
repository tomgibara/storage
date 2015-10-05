package com.tomgibara.storage;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.tomgibara.bits.AbstractBitStore;
import com.tomgibara.bits.BitStore;
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
 * {@link #valueType()}, {@link #size()} and
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
		if (!isMutable()) throw new IllegalStateException("immutable");
		if (value == null) clear();
		int size = size();
		for (int i = 0; i < size; i++) {
			set(i, value);
		}
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
		return new ArrayStore<>(Stores.toArray(this, newSize), count());
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
	 * but will change as values are added and removed from the store.
	 * 
	 * @return bits indicating the indices at which values are present
	 */
	default BitStore population() {
		return new AbstractBitStore() {

			@Override
			public int size() {
				return size();
			}

			@Override
			public boolean getBit(int index) {
				return get(index) != null;
			}

		};
	}

	/**
	 * Exposes the store as a list. The size of the list is equal to the
	 * size of the store with 'unset' elements exposed as null. The list
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
				int size = size();
				for (int i = 0; i < size; i++) {
					action.accept(get(i));
				}
			}
		};
	}

	default Store<V> transformedBy(Function<V, V> fn) {
		return transformedBy(valueType(), fn);
	}

	default <W> Store<W> transformedBy(Class<W> type, Function<V, W> fn) {
		return new Store<W>() {

			@Override
			public Class<W> valueType() {
				return type;
			}

			@Override
			public W get(int index) {
				V v = Store.this.get(index);
				if (v == null) return null;
				W w = fn.apply(v);
				if (w == null) throw new RuntimeException("mapping fn returned null");
				return w;
			}

			@Override
			public int size() {
				return Store.this.size();
			}

			@Override
			public int count() {
				return Store.this.count();
			}

			@Override
			public BitStore population() {
				return Store.this.population();
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
		return resizedCopy(size());
	}

	@Override
	default Store<V> immutable() {
		return isMutable() ? immutableView() : this;
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
