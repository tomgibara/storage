package com.tomgibara.storage;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

class ConstantSpliterator<V> implements Spliterator<V> {

	private final V value;
	private int size;

	ConstantSpliterator(V value, int size) {
		this.value = value;
		this.size = size;
	}

	@Override
	public boolean tryAdvance(Consumer<? super V> action) {
		if (size == 0) return false;
		action.accept(value);
		size --;
		return true;
	}

	@Override
	public void forEachRemaining(Consumer<? super V> action) {
		for (; size > 0; size--) {
			action.accept(value);
		}
	}

	@Override
	public Spliterator<V> trySplit() {
		if (size < 2) return null;
		int s = size / 2;
		size -= s;
		return new ConstantSpliterator<V>(value, s);
	}

	@Override
	public long estimateSize() {
		return size;
	}

	@Override
	public long getExactSizeIfKnown() {
		return size;
	}

	@Override
	public int characteristics() {
		return SORTED | ORDERED | SIZED | SUBSIZED | NONNULL | IMMUTABLE;
	}

	@Override
	public Comparator<? super V> getComparator() {
		return new Comparator<V>() {
			@Override
			public int compare(V v1, V v2) {
				if (v1 != value || v2 != value) throw new IllegalArgumentException();
				return 0;
			}
		};
	}
}
