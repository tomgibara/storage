package com.tomgibara.storage;

import java.util.List;

import com.tomgibara.bits.BitStore;

final class RangeStore<V> extends AbstractStore<V> {

	private final Store<V> store;
	private final int from;
	private final int to;

	RangeStore(Store<V> store, int from, int to) {
		this.store = store;
		this.from = from;
		this.to = to;
	}

	@Override
	public int size() {
		return to - from;
	}

	@Override
	public V get(int index) {
		return store.get(from + index);
	}

	@Override
	public boolean isNull(int index) {
		return store.isNull(from + index);
	}

	@Override
	public StoreType<V> type() {
		return store.type();
	}

	@Override
	public V set(int index, V value) {
		return store.set(from + index, value);
	}

	@Override
	public boolean isSettable(Object value) {
		return store.isSettable(value);
	}

	@Override
	public BitStore population() {
		return store.population().range(from, to);
	}

	@Override
	public Store<V> range(int from, int to) {
		return store.range(this.from + from, this.from + to);
	}

	@Override
	public List<V> asList() {
		return store.asList().subList(from, to);
	}

	@Override
	public <W extends V> void setStore(int position, Store<W> store) {
		store.setStore(from + position, store);
	}

}
