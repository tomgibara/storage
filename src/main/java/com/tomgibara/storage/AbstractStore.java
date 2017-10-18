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

	// object methods

	@Override
	public int hashCode() {
		int size = size();
		int hashCode = 1;
		for (int i = 0; i < size; i++) {
			V v = get(i);
			hashCode = 31 * hashCode + (v == null ? 0 : v.hashCode());
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Store)) return false;
		Store<?> that = (Store<?>) obj;
		int size = size();
		if (size != that.size()) return false;
		for (int i = 0; i < size; i++) {
			V v = this.get(i);
			Object w = that.get(i);
			if (v == w) continue;
			if (v == null || w == null) return false;
			if (!v.equals(w)) return false;
		}
		return true;
	}

	@Override
	public String toString() {
		int size = size();
		switch (size) {
		case 0 : return "[]";
		case 1 : return '[' + toString(get(0)) + ']';
		default:
			StringBuilder sb = new StringBuilder();
			sb.append('[').append(toString(get(0)));
			for (int i = 1; i < size; i++) {
				sb.append(',').append(' ').append(toString(get(i)));
			}
			return sb.append(']').toString();
		}
	}

	// package scoped helpers

	int checkSetStore(int position, Store<?> store) {
		if (position < 0) throw new IllegalArgumentException("negative position");
		if (store == null) throw new IllegalArgumentException("null store");
		int size = store.size();
		if (!type().nullSettable && store.count() < store.size()) throw new IllegalAccessError("null not settable");
		if (position + size > size()) throw new IllegalArgumentException("position too large");
		return size;
	}

	<W extends V> void setStoreImpl(int position, Store<W> store, int from, int to) {
		int offset = position - from;
		if (offset == 0) {
			for (int i = from; i < to; i++) {
				set(i, store.get(i));
			}
		} else {
			for (int i = from; i < to; i++) {
				set(offset + i, store.get(i));
			}
		}
	}

	// returns true if fast fill was available
	// implementation assumes that all checks have been performed
	boolean fastFill(int from, int to, V value) {
		return false;
	}

	// returns true if toArray was available
	// guaranteed to be called with an array that has a compatible type and sufficient length
	boolean toArray(int from, int to, V[] vs) {
		return false;
	}

	// private helper methods

	private String toString(Object value) {
		if (value == null) return "null";
		if (value == this) return "(this store)";
		return value.toString();
	}

}
