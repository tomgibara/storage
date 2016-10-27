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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Arrays;

abstract class RefStore<V> extends AbstractStore<V> {

	interface RefStorage<V> extends Storage<V> {
		//TODO this is pretty smelly
		@SuppressWarnings("unchecked")
		@Override
		public default Class<V> valueType() { return (Class<V>) Object.class; }
	}

	private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
	private final Reference<V>[] refs;
	// counts number of references (cleared or not)
	private int count;


	@SuppressWarnings("unchecked")
	RefStore(int size) {
		try {
			refs = new Reference[size];
		} catch (NegativeArraySizeException e) {
			throw new IllegalArgumentException("negative size");
		}
		count = 0;
	}

	@Override
	//TODO this is pretty smelly
	@SuppressWarnings("unchecked")
	public Class<V> valueType() {
		return (Class<V>) Object.class;
	}

	@Override
	public int size() {
		return refs.length;
	}

	@Override
	public int count() {
		flushQueue();
		return count;
	}

	@Override
	public V get(int index) {
		flushQueue();
		Reference<V> ref = refs[index];
		return ref == null ? null : ref.get();
	}

	@Override
	public boolean isNull(int index) {
		flushQueue();
		Reference<V> ref = refs[index];
		return ref == null || ref.get() == null;
	}

	@Override
	public V set(int index, V value) {
		flushQueue();
		Reference<V> ref = refs[index];

		// removal case
		if (value == null) {
			if (ref == null) return null;
			refs[index] = null;
			count--;
			return ref.get();
		}

		// addition case
		refs[index] = newReference(value, queue, index);
		if (ref == null) {
			count++;
			return null;
		} else {
			return ref.get();
		}
	}

	@Override
	public void clear() {
		flushQueue();
		Arrays.fill(refs, null);
		count = 0;
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Store<V> immutableCopy() {
		flushQueue();
		int size = refs.length;
		@SuppressWarnings("unchecked")
		V[] vs = (V[]) new Object[size];
		int count = 0;
		for (int i = 0; i < size; i++) {
			Reference<V> ref = refs[i];
			if (ref == null) continue;
			V value = ref.get();
			if (value == null) continue;
			vs[i] = value;
			count++;
		}
		return new ImmutableArrayStore<>(vs, count, nullity());
	}

	abstract Reference<V> newReference(V referent, ReferenceQueue<V> queue, int index);

	// should be overridden for efficiency
	int indexOf(Reference<?> ref) {
		for (int i = 0; i < refs.length; i++) {
			if (refs[i] == ref) return i;
		}
		return -1;
	}

	// that must be clear
	RefStore<V> populate(RefStore<V> that) {
		flushQueue();
		Reference<V>[] thatRefs = that.refs;
		ReferenceQueue<V> thatQueue = that.queue;
		int limit = Math.min(refs.length, thatRefs.length);
		int size = 0;
		for (int i = 0; i < limit; i++) {
			Reference<V> ref = refs[i];
			if (ref == null) continue;
			V v = ref.get();
			if (v == null) continue;
			thatRefs[i] = that.newReference(v, thatQueue, i);
			size ++;
		}
		that.count = size;
		return that;
	}

	private void flushQueue() {
		while (true) {
			Reference<?> ref = queue.poll();
			if (ref == null) break; // queue exhausted
			int index = indexOf(ref);
			if (refs[index] == ref) {
				refs[index] = null;
				count --;
			}
		}
	}

}
