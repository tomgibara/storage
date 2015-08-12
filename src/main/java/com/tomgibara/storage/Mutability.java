package com.tomgibara.storage;

public interface Mutability<T> {

	// whether mutable
	boolean isMutable();
	
	// creates a mutable copy if not mutable
	T mutable();
	
	// creates a mutable copy
	T mutableCopy();
	
	// creates a mutable view only if mutable
	T mutableView();
	
	// creates a mutable view if not mutable
	T immutable();
	
	// creates an immutable copy
	T immutableCopy();
	
	// creates an immutable view
	T immutableView();
}
