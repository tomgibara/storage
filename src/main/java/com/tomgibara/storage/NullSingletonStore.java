/*
 * Copyright 2017 Tom Gibara
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

import static com.tomgibara.storage.Stores.immutableException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Bijection;
import com.tomgibara.fundament.Mapping;

final class NullSingletonStore<V> implements Store<V> {

	// statics

	private void checkIndex(int index) {
		if (index != 0) throw new IllegalArgumentException("invalid index");
	}

	private static final NullSingletonStore<Object> generic = new NullSingletonStore<>(StoreType.OBJECT);

	@SuppressWarnings("unchecked")
	static <V> NullSingletonStore<V> generic() {
		return (NullSingletonStore<V>) generic;
	}

	// fields

	private final StoreType<V> type;

	// constructors

	NullSingletonStore(StoreType<V> type) {
		this.type = type;
	}

	// store methods

	@Override
	public StoreType<V> type() {
		return type;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public V get(int index) {
		checkIndex(index);
		return null;
	}

	@Override
	public boolean isNull(int index) {
		checkIndex(index);
		return true;
	}

	@Override
	public V set(int index, V value) {
		checkIndex(index);
		throw immutableException();
	}
	@Override
	public void clear() {
		throw immutableException();
	}

	@Override
	public void fill(V value) {
		throw immutableException();
	}

	@Override
	public boolean compact() {
		throw immutableException();
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public BitStore population() {
		return Bits.zeroBit();
	}

	@Override
	public Store<V> range(int from, int to) {
		if (from < 0) throw new IllegalArgumentException("negative from");
		if (from > to) throw new IllegalArgumentException("from exceeds to");
		if (to > 1) throw new IllegalArgumentException("to exceeds size");
		return to - from == 0 ? new EmptyStore<>(type, false) : this;
	}

	@Override
	public List<V> asList() {
		return Collections.singletonList(null);
	}

	@Override
	public Store<V> asTransformedBy(UnaryOperator<V> fn) {
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <W> Store<W> asTransformedBy(Bijection<V, W> fn) {
		StoreType<W> newType = type.map(fn);
		return newType.equals(type) ? (Store<W>) this : new NullSingletonStore<>(newType);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <W> Store<W> asTransformedBy(Mapping<V, W> fn) {
		StoreType<W> newType = type.map(fn);
		return newType.equals(type) ? (Store<W>) this : new NullSingletonStore<>(newType);
	}

	@Override
	public <W> Iterator<W> transformedIterator(BiFunction<Integer, V, W> fn) {
		return Collections.emptyIterator();
	}

	@Override
	public <W> Iterator<W> transformedIterator(Function<V, W> fn) {
		return Collections.emptyIterator();
	}

	// copiedBy defaulted

	@Override
	public Iterator<V> iterator() {
		return asList().iterator();
	}

	@Override
	public void forEach(Consumer<? super V> action) {
		/* no op */
	}

	@Override
	public Spliterator<V> spliterator() {
		return Spliterators.emptySpliterator();
	}

	// transposable methods

	@Override
	public void transpose(int i, int j) {
		/* no op */
	}

	// mutation methods

	// isMutable defaulted

	@Override
	public Store<V> immutableCopy() {
		return this;
	}

	@Override
	public Store<V> immutableView() {
		return this;
	}

	// object methods

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Store)) return false;
		Store<?> that = (Store<?>) obj;
		return that.size() == 1 && that.count() == 0;
	}

	@Override
	public String toString() {
		return "[null]";
	}

}
