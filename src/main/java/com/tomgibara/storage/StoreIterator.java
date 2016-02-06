package com.tomgibara.storage;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.tomgibara.bits.BitStore.Positions;

class StoreIterator<V> implements Iterator<V> {

	private final Store<V> store;
	private final Positions positions;

	StoreIterator(Store<V> store) {
		this.store = store;
		this.positions = store.population().ones().positions();
	}
	
	@Override
	public boolean hasNext() {
		return positions.hasNext();
	}

	@Override
	public V next() {
		if (!positions.hasNext()) throw new NoSuchElementException();
		return get(positions.nextPosition());
	}

	@Override
	public void remove() {
		int previous = positions.previousPosition();
		if (previous == -1) throw new NoSuchElementException();
		V value = get(previous);
		if (value == null) throw new IllegalStateException();
		set(previous, null);
		previous = -1;
	}

	@Override
	public void forEachRemaining(Consumer<? super V> action) {
		while (positions.hasNext()) {
			action.accept(get(positions.nextPosition()));
		}
	}
	
	V get(int index) {
		return store.get(index);
	}

	void set(int index, V v) {
		store.set(index, v);
	}
}
