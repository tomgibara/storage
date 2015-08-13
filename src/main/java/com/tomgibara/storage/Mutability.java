package com.tomgibara.storage;

/**
 * Exposes methods for manipulating mutability.
 * 
 * @author Tom Gibara
 *
 * @param <T> the underlying type
 */

public interface Mutability<T> {

	/**
	 * Whether the object is mutable.
	 * 
	 * @return true if mutable, false otherwise
	 */
	boolean isMutable();
	
	/**
	 * A copy of the object if it is immutable, or the object itself. This
	 * method is useful when receiving an object of uncertain mutability on
	 * which mutation-based computations are necessary.
	 * 
	 * @return the mutable object or a mutable copy
	 */
	T mutable();
	
	/**
	 * A mutable copy of the object. Changes to the state of the returned copy
	 * will not modify this object.
	 * 
	 * @return a mutable copy of this object
	 */
	T mutableCopy();
	
	/**
	 * An immutable copy of the object if it is mutable or the object itself.
	 * This method is useful when returning an object of uncertain mutability
	 * to prevent accidental mutation by the caller.
	 * 
	 * @return the immutable object or an immutable copy.
	 */
	T immutable();
	
	/**
	 * An immutable copy of this object.
	 * 
	 * @return an immutable copy of this object
	 */
	T immutableCopy();
	
	/**
	 * An immutable view of this object. The view will exhibit the same state as
	 * this object, and mutations of the state of this object will be reflected
	 * in the view, but mutation via calls on the returned view will be
	 * prohibited.
	 * 
	 * @return an immutable view of this object
	 */
	T immutableView();
}
