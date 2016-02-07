package com.tomgibara.storage;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.tomgibara.bits.BitStore;
import com.tomgibara.fundament.Bijection;

class TransformedStore<V,W> extends AbstractStore<W> {

	private final Store<V> store;
	private final Class<W> type;
	private final Bijection<V, W> fn;
	
	TransformedStore(Store<V> store, Class<W> type, Function<V, W> fn) {
		this(store, type, new OneWayBijection<>(fn));
	}

	TransformedStore(Store<V> store, Class<W> type, Bijection<V, W> fn) {
		this.store = store;
		this.type = type;
		this.fn = fn;
	}

	// store methods
	
	@Override
	public Class<W> valueType() {
		return type;
	}

	@Override
	public int size() {
		return store.size();
	}

	@Override
	public boolean isNullAllowed() {
		return store.isNullAllowed();
	}

	@Override
	public int count() {
		return store.count();
	}

	@Override
	public BitStore population() {
		return store.population();
	}

	@Override
	public W get(int index) {
		V v = store.get(index);
		if (v == null) return null;
		W w = fn.apply(v);
		if (w == null) throw new RuntimeException("mapping fn returned null");
		return w;
	}

	@Override
	public Store<W> resizedCopy(int newSize) {
		return new TransformedStore<>(store.resizedCopy(newSize), type, fn);
	}

	@Override
	public Store<W> asTransformedBy(UnaryOperator<W> fn) {
		return new TransformedStore<>(store, type, fn.compose(this.fn));
	}
	
	@Override
	public <X> Store<X> asTransformedBy(Class<X> type, Function<W, X> fn) {
		return new TransformedStore<>(store, type, fn.compose(this.fn));
	}

	// mutable
	
	@Override
	public void clear() {
		store.clear();
	}
	
	@Override
	public void fill(W value) {
		store.fill(fn.disapply(value));
	}

	@Override
	public W set(int index, W value) {
		return fn.apply( store.set(index, fn.disapply(value)) );
	}
	
	@Override
	public void transpose(int i, int j) {
		store.transpose(i, j);
	}
	
	// mutability methods

	@Override
	public boolean isMutable() {
		return store.isMutable();
	}
	
	@Override
	public Store<W> immutableCopy() {
		return new TransformedStore<V,W>(store.immutableCopy(), type, fn);
	}

	@Override
	public Store<W> immutableView() {
		return new TransformedStore<V,W>(store.immutableView(), type, fn);
	}

	// iterable methods
	
	@Override
	public Iterator<W> iterator() {
		return new StoreIterator.Transformed<>(store, fn);
	}

	// inner classes
	
	private static class OneWayBijection<V,W> implements Bijection<V, W> {
		
		private final Function<V,W> fn;
		
		OneWayBijection(Function<V,W> fn) {
			this.fn = fn;
		}
		
		@Override
		public W apply(V v) {
			return fn.apply(v);
		}
		
		@Override
		public V disapply(W w) {
			if (w != null) throw new IllegalArgumentException("non-null value");
			return null;
		}
		
	}
}
