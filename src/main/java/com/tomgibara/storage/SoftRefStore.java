package com.tomgibara.storage;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

final class SoftRefStore<V> extends RefStore<V> {

	SoftRefStore(int size) {
		super(size);
	}

	@Override
	public SoftRefStore<V> resizedCopy(int newSize) {
		SoftRefStore<V> that = new SoftRefStore<V>(newSize);
		populate(that);
		return that;
	}

	@Override
	Reference<V> newReference(V referent, ReferenceQueue<V> queue, int index) {
		return new SoftRef<V>(referent, queue, index);
	}

	@Override
	int indexOf(Reference<?> ref) {
		return ((SoftRef<?>) ref).index;
	}

	private static class SoftRef<T> extends SoftReference<T> {

		final int index;

		public SoftRef(T referent, ReferenceQueue<? super T> q, int index) {
			super(referent, q);
			this.index = index;
		}
	}

}
