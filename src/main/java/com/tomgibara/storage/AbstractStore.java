package com.tomgibara.storage;

public abstract class AbstractStore<V> implements Store<V> {

	@Override
	public String toString() {
		return asList().toString();
	}

}
