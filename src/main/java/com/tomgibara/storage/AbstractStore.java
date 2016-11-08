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

	// package scoped helpers

	int checkSetStore(int position, Store<?> store) {
		if (position < 0) throw new IllegalArgumentException("negative position");
		if (store == null) throw new IllegalArgumentException("null store");
		int size = store.size();
		if (!type().nullSettable && store.count() < store.size()) throw new IllegalAccessError("null not settable");
		if (position + size > size()) throw new IllegalArgumentException("position too large");
		return size;
	}

	<W extends V> void setStoreImpl(int position, Store<W> store, int size) {
		if (position == 0) {
			for (int i = 0; i < size; i++) {
				set(i, store.get(i));
			}
		} else {
			for (int i = 0; i < size; i++) {
				set(position + i, store.get(i));
			}
		}
	}
}
