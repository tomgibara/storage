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

final class NullConstantStore<V> implements Store<V> {

	// fields

	private final Class<V> type;
	private final int size;

	// constructors

	NullConstantStore(Class<V> type, int size) {
		this.type = type;
		this.size = size;
	}

	// store methods

	@Override
	public Class<V> valueType() {
		return type;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public V get(int index) {
		return null;
	}

	@Override
	public boolean isNull(int index) {
		return true;
	}

	// nullValue() defaulted

	// set() defaulted

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
	public Store<V> resizedCopy(int newSize) {
		if (newSize == size) return this;
		if (newSize < 0) throw new IllegalArgumentException("negative newSize");
		return new NullConstantStore<>(type, newSize);
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public BitStore population() {
		return Bits.zeroBits(size);
	}

	@Override
	public List<V> asList() {
		return Collections.nCopies(size, null);
	}

	@Override
	public Store<V> asTransformedBy(UnaryOperator<V> fn) {
		return this;
	}

	@Override
	public <W> Store<W> asTransformedBy(Bijection<V, W> fn) {
		return new NullConstantStore<>(fn.rangeType(), size);
	}

	@Override
	public <W> Store<W> asTransformedBy(Mapping<V, W> fn) {
		return new NullConstantStore<>(fn.rangeType(), size);
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
		return Collections.nCopies(size, (V) null).iterator();
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
		// optimization possible
		int h = 1;
		for (int i = 0; i < size; i++) {
			h *= 31;
		}
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Store)) return false;
		Store<?> that = (Store<?>) obj;
		return this.size == that.size() && that.count() == 0;
	}

	@Override
	public String toString() {
		switch (size) {
		case 0: return "[]";
		case 1: return "[null]";
		default:
			StringBuilder sb = new StringBuilder(1 + 5 * size);
			sb.append("[null,");
			for (int i = 1; i < size; i++) {
				sb.append(",null");
			}
			return sb.append(']').toString();
		}
	}
}
