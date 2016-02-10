package com.tomgibara.storage;

import java.util.Spliterator;
import java.util.function.Consumer;

// only for non-null stores
class StoreSpliterator<V> implements Spliterator<V> {

	private final Store<V> store;
	private final int chi;
	private int from;
	private int to;

	StoreSpliterator(Store<V> store) {
		this.store = store;
		if (store.nullValue() == null) {
			chi = store.isMutable() ? ORDERED : ORDERED | IMMUTABLE;
		} else {
			chi = store.isMutable() ? ORDERED | SIZED | SUBSIZED | NONNULL : ORDERED | SIZED | SUBSIZED | NONNULL | IMMUTABLE;
		}
		from = 0;
		to = store.size();
	}

	StoreSpliterator(StoreSpliterator<V> splitter) {
		store = splitter.store;
		chi = splitter.chi;
		from = (splitter.from + splitter.to) >> 1;
		to = splitter.to;
		// modifies supplied splitter
		splitter.to = from;
	}

	@Override
	public boolean tryAdvance(Consumer<? super V> action) {
		if ((chi & NONNULL) != 0) {
			if (from == to) return false;
			action.accept(store.get(from++));
			return true;
		}
		while (from < to) {
			V v = store.get(from++);
			if (v != null) {
				action.accept(v);
				return true;
			}
		}
		return false;
	}

	@Override
	public Spliterator<V> trySplit() {
		if (from >= to - 1) return null;
		return new StoreSpliterator<>(this);
	}

	@Override
	public long estimateSize() {
		return to - from;
	}

	@Override
	public int characteristics() {
		return chi;
	}

}
