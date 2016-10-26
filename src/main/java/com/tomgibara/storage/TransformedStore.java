/*
 * Copyright 2016 Tom Gibara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tomgibara.storage;

import java.util.Iterator;

import com.tomgibara.bits.BitStore;
import com.tomgibara.fundament.Bijection;
import com.tomgibara.fundament.Mapping;

class TransformedStore<V,W> extends AbstractStore<W> {

	private static <V, W> Bijection<V, W> oneWay(Mapping<V, W> fn) {
		Class<V> domainType = fn.domainType();
		Class<W> rangeType = fn.rangeType();
		return new Bijection<V, W>() {
			@Override public Class<V> domainType() { return domainType; }
			@Override public Class<W> rangeType() { return rangeType; }
			@Override public W apply(V t) { return fn.apply(t); }
			@Override public V disapply(W w) { if (w == null) return null; else throw new IllegalArgumentException("non-null value"); }
			@Override public boolean isInDomain(Object obj) { return obj == null || domainType.isInstance(obj); }
			@Override public boolean isInRange(Object obj) { return obj == null || rangeType.isInstance(obj); }
		};
	}
	
	private final Store<V> store;
	private final Bijection<V, W> fn;

	TransformedStore(Store<V> store, Mapping<V, W> fn) {
		this(store, oneWay(fn));
	}

	TransformedStore(Store<V> store, Bijection<V, W> fn) {
		this.store = store;
		this.fn = fn;
	}

	// store methods

	@Override
	public Class<W> valueType() {
		return fn.rangeType();
	}

	@Override
	public int size() {
		return store.size();
	}

	@Override
	public StoreNullity<W> nullity() {
		return store.nullity().map(fn);
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
	public boolean isNull(int index) {
		return store.isNull(index);
	}

	@Override
	public Store<W> resizedCopy(int newSize) {
		return new TransformedStore<>(store.resizedCopy(newSize), fn);
	}

	@Override
	public <X> Store<X> asTransformedBy(Mapping<W, X> fn) {
		return new TransformedStore<>(store, oneWay(fn).compose(this.fn));
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
		return new TransformedStore<V,W>(store.immutableCopy(), fn);
	}

	@Override
	public Store<W> immutableView() {
		return new TransformedStore<V,W>(store.immutableView(), fn);
	}

	// iterable methods

	@Override
	public Iterator<W> iterator() {
		return new StoreIterator.Transformed<>(store, fn);
	}

}
