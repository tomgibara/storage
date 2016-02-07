package com.tomgibara.storage;

/**
 * Defines object methods consistent with the specifications documented for {@link Store}.
 * The class is intended to provide a convenient base class for implementors of the interface.
 *
 * @author Tom Gibara
 *
 * @param <V>
 *            the type of the values stored
 */

public abstract class AbstractStore<V> implements Store<V> {

	@Override
	public int hashCode() {
		return asList().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Store)) return false;
		Store<?> that = (Store<?>) obj;
		return this.asList().equals(that.asList());
	}

	@Override
	public String toString() {
		return asList().toString();
	}

}
