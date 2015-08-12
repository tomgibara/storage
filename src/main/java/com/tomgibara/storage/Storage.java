package com.tomgibara.storage;


public interface Storage<T> {

	@SuppressWarnings("unchecked")
	static <T> Storage<T> generic() {
		return capacity -> (Store<T>) new ArrayStore<>(new Object[capacity]);
	}
	
	static <T> Storage<T> typed(Class<T> type) {
		if (type == null) throw new IllegalArgumentException("null type");
		return type.isPrimitive() ?
				(capacity -> PrimitiveStore.newStore(type, capacity)) :
				(capacity -> new ArrayStore<>(type, capacity));
	}
	
	static <T> Storage<T> weak() {
		return capacity -> new WeakRefStore<>(capacity);
	}
	
	static <T> Storage<T> soft() {
		return capacity -> new SoftRefStore<>(capacity);
	}
	
	Store<T> newStore(int capacity);
	
}
