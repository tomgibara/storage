package com.tomgibara.storage;

import java.lang.reflect.Array;

class Stores {

	static <V> int countNonNulls(V[] vs) {
		int sum = 0;
		for (V v : vs) if (v != null) sum++;
		return sum;
	}
	
	static <V> V[] toArray(Store<V> store) {
		return toArray(store, store.capacity());
	}

	@SuppressWarnings("unchecked")
	static<V> V[] toArray(Store<V> store, int length) {
		V[] vs = (V[]) Array.newInstance(store.valueType(), length);
		return copyIntoArray(store, vs);
	}

	static<V> V[] copyIntoArray(Store<V> store, V[] vs) {
		int limit = Math.min( store.capacity(), vs.length );
		for (int i = 0; i < limit; i++) {
			vs[i] = store.get(i);
		}
		return vs;
	}
}
