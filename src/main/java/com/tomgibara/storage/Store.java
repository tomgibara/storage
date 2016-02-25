package com.tomgibara.storage;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.tomgibara.bits.AbstractBitStore;
import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Bijection;
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
	 * is no value associated with specified index.
	 *
	 * @param index
	 *            the index from which to retrieve the value
	 * @return the value stored at the specified index
	 */
	V get(int index);

	/**
	 * Whether the value at the specified index is null. In many implementations
	 * this method may be faster than calling {@link #get(int)} and checking for
	 * null.
	 *
	 * @param index
	 *            the index of the value to be compared to null
	 * @return true if and only if the value at the specified index is null
	 */
	default boolean isNull(int index) {
		return get(index) != null;
	}

	/**
	 * <p>
	 * The value that substitutes for null in this store. For stores that
	 * support the storage of null values this method will always return
	 * <em>empty</em>. Some stores do not support null values (for example,
	 * those backed by primitive arrays) in this instance the returned value may
	 * never be <em>empty</em>.
	 * 
	 * <p>
	 * Note that the value returned by this method will by substituted for null
	 * in calls to {@link #set(int, Object)} and {@link #clear()} but
	 * occurrences of this value in the store will not be reported as null.
	 * 
	 * @return the value that substitutes for null wrapped in an optional, or
	 *         empty
	 */
	default Optional<V> nullValue() {
		return Optional.empty();
	}
	
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
		Optional<V> nv = nullValue();
		return nv.isPresent() ?
				new ArrayStore<>(Stores.toArray(this, newSize), nv.get()) :
				new NullArrayStore<>(Stores.toArray(this, newSize), count());
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
		if (nullValue().isPresent()) return Bits.oneBits(size());
		return new AbstractBitStore() {

			@Override
			public int size() {
				return Store.this.size();
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

	/**
	 * <p>
	 * Derives a store by applying a function over the store values. It provides
	 * a live view of the original store. The mutability of the returned store
	 * matches the mutability of this store, but only null values may be set.
	 *
	 * <p>
	 * Note that the supplied function must preserve null. That is
	 * <code>fn(a) == null</code> if and only if <code>a == null</code>.
	 *
	 * @param fn
	 *            a function over the store elements
	 *
	 * @return a view of this store under the specified function
	 * @see #asTransformedBy(Class, Function)
	 */
	default Store<V> asTransformedBy(UnaryOperator<V> fn) {
		return asTransformedBy(valueType(), fn);
	}

	/**
	 * <p>
	 * Derives a store by applying a function over the store values. It provides
	 * a live view of the original store. The mutability of the returned store
	 * matches the mutability of this store. Bijectivity of the supplied
	 * transforming function has the consequence that values may be set on the
	 * store in contrast to {@link #asTransformedBy(UnaryOperator)}.
	 *
	 * <p>
	 * Note that the supplied function must preserve null. That is
	 * <code>fn(a) == null</code> if and only if <code>a == null</code>.
	 *
	 * @param fn
	 *            a bijective function over the store elements
	 *
	 * @return a view of this store under the specified function
	 * @see #asTransformedBy(UnaryOperator)
	 */
	default Store<V> asTransformedBy(Bijection<V,V> fn) {
		return asTransformedBy(valueType(), fn);
	}

	/**
	 * <p>
	 * Derives a store by applying a function over the store values. It provides
	 * a live view of the original store. The mutability of the returned store
	 * matches the mutability of this store, but only null values may be set.
	 *
	 * <p>
	 * Note that the supplied function must preserve null. That is
	 * <code>fn(a) == null</code> if and only if <code>a == null</code>.
	 *
	 * @param type
	 *            the type of values returned by the function
	 * @param fn
	 *            a function over the store elements
	 * @param <W>
	 *            the type of value in the returned store
	 *
	 * @return a view of this store under the specified function
	 * @see #asTransformedBy(Function)
	 * @see #asTransformedBy(Class, Bijection)
	 */
	default <W> Store<W> asTransformedBy(Class<W> type, Function<V, W> fn) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (fn == null) throw new IllegalArgumentException("null fn");
		return new TransformedStore<V,W>(this, type, fn);
	}

	/**
	 * <p>
	 * Derives a store by applying a function over the store values. It provides
	 * a live view of the original store. The mutability of the returned store
	 * matches the mutability of this store. Bijectivity of the supplied
	 * transforming function has the consequence that values may be set on the
	 * store in contrast to {@link #asTransformedBy(Class, Function)}.
	 *
	 * <p>
	 * Note that the supplied function must preserve null. That is
	 * <code>fn(a) == null</code> if and only if <code>a == null</code>.
	 *
	 * @param type
	 *            the type of values returned by the function
	 * @param fn
	 *            a bijective function over the store elements
	 *
	 * @return a view of this store under the specified function
	 * @see #asTransformedBy(Class, Function)
	 */
	default <W> Store<W> asTransformedBy(Class<W> type, Bijection<V, W> fn) {
		if (type == null) throw new IllegalArgumentException("null type");
		if (fn == null) throw new IllegalArgumentException("null fn");
		return new TransformedStore<V,W>(this, type, fn);
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
		if (nullValue().isPresent()) {
			for (int i = 0; i < size; i++) {
				action.accept(get(size));
			}
		} else {
			for (int i = 0; i < size; i++) {
				V v = get(size);
				if (v != null) action.accept(v);
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
		return new ImmutableArrayStore<>(Stores.toArray(this), count());
	}

	@Override
	default Store<V> immutableView() {
		return new ImmutableStore<>(this);
	}

}
