package com.tomgibara.storage;


public interface Storage<T> {

	@SuppressWarnings("unchecked")
	static <T> Storage<T> generic() {
		return size -> (Store<T>) new ArrayStore<>(new Object[size]);
	}
	
	static <T> Storage<T> typed(Class<T> type) {
		if (type == null) throw new IllegalArgumentException("null type");
		return type.isPrimitive() ?
				(size -> PrimitiveStore.newStore(type, size)) :
				(size -> new ArrayStore<>(type, size));
	}
	
	Store<T> newStore(int capacity);
	
}
