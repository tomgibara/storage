package com.tomgibara.storage;

/**
 * <p>
 * Implementations of this interface are able to create {@link Store} instances
 * with a specified capacity.
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
	 * Genericized storage backed by <code>Object</code> arrays.
	 * 
	 * @return genericized storage
	 */
	@SuppressWarnings("unchecked")
	static <V> Storage<V> generic() {
		return capacity -> (Store<V>) new ArrayStore<>(new Object[capacity]);
	}

	/**
	 * Storage backed by typed arrays. Specifying a primitive type will result
	 * in storage backed by arrays of primitives. Such stores provide greater
	 * type safety than those created by genericized storage. In some contexts
	 * this will provide a very significant reduction in the memory required to
	 * store values.
	 * 
	 * @param type
	 *            the type of the values to be stored
	 * @throws IllegalArgumentException
	 *             if the supplied type is null
	 * @return typed storage
	 */
	static <V> Storage<V> typed(Class<V> type) throws IllegalArgumentException {
		if (type == null) throw new IllegalArgumentException("null type");
		return type.isPrimitive() ?
				(capacity -> PrimitiveStore.newStore(type, capacity)) :
				(capacity -> new ArrayStore<>(type, capacity));
	}

	/**
	 * Genericized storage using weak references. As a consequence of GC
	 * activity, sizes reported by the weak stores may overestimate the number
	 * of values stored.
	 * 
	 * @return weak storage
	 */
	static <V> Storage<V> weak() {
		return capacity -> new WeakRefStore<>(capacity);
	}
	
	/**
	 * Genericized storage using soft references. As a consequence of GC
	 * activity, sizes reported by the soft stores may overestimate the number
	 * of values stored.
	 * 
	 * @return soft storage
	 */
	static <V> Storage<V> soft() {
		return capacity -> new SoftRefStore<>(capacity);
	}
	
	/**
	 * Creates a new store with the requested capacity
	 * 
	 * @param capacity
	 *            the required capacity
	 * @throws IllegalArgumentException
	 *             if the capacity is negative
	 * @return an new store
	 */
	Store<V> newStore(int capacity) throws IllegalArgumentException;
	
}
