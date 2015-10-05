package com.tomgibara.storage;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

final class WeakRefStore<V> extends RefStore<V> {

	WeakRefStore(int size) {
		super(size);
	}

	@Override
	public WeakRefStore<V> resizedCopy(int newSize) {
		WeakRefStore<V> that = new WeakRefStore<V>(newSize);
		populate(that);
		return that;
	}
	
	@Override
	Reference<V> newReference(V referent, ReferenceQueue<V> queue, int index) {
		return new WeakRef<V>(referent, queue, index);
	}
	
	@Override
	int indexOf(Reference<?> ref) {
		return ((WeakRef<?>) ref).index;
	}
	
	private static class WeakRef<T> extends WeakReference<T> {

		final int index;

		public WeakRef(T referent, ReferenceQueue<? super T> q, int index) {
			super(referent, q);
			this.index = index;
		}
	}
	
}
