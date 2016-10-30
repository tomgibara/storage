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
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Bijection;
import com.tomgibara.fundament.Mapping;

final class ConstantStore<V> implements Store<V> {

	private final StoreType<V> type;
	private final V value;
	private final int size;

	ConstantStore(StoreType<V> type, V value, int size) {
		this.type = type;
		this.value = value;
		this.size = size;
	}

	@Override
	public StoreType<V> type() {
		return type;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public V get(int index) {
		return value;
	}

	@Override
	public boolean isNull(int index) {
		return false;
	}

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
		return new ConstantStore<>(type, value, newSize);
	}

	@Override
	public int count() {
		return size;
	}

	@Override
	public BitStore population() {
		return Bits.oneBits(size);
	}

	@Override
	public List<V> asList() {
		return Collections.nCopies(size, value);
	}

	@Override
	public Store<V> asTransformedBy(UnaryOperator<V> fn) {
		return new ConstantStore<>(type, fn.apply(value), size);
	}

	@Override
	public <W> Store<W> asTransformedBy(Bijection<V, W> fn) {
		return new ConstantStore<>(type.map(fn), fn.apply(value), size);
	}

	@Override
	public <W> Store<W> asTransformedBy(Mapping<V, W> fn) {
		return new ConstantStore<>(type.map(fn), fn.apply(value), size);
	}

	// transformedIterator defaulted

	@Override
	public Store<V> copiedBy(Storage<V> storage) {
		if (storage == null) throw new IllegalArgumentException("null storage");
		return storage.isStorageMutable() ? storage.newStore(size, value) : storage.newCopyOf(this);
	}

	// iterable methods

	@Override
	public Iterator<V> iterator() {
		return Collections.nCopies(size, value).iterator();
	}

	@Override
	public void forEach(Consumer<? super V> action) {
		for (int i = 0; i < size; i++) {
			action.accept(value);
		}
	}

	@Override
	public Spliterator<V> spliterator() {
		return new ConstantSpliterator<V>(value, size);
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
		int vh = value.hashCode();
		int h = 1;
		for (int i = 0; i < size; i++) {
			h = 31 * h + vh;
		}
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Store)) return false;
		Store<?> that = (Store<?>) obj;
		if (this.size != that.size()) return false;
		if (this.size != that.count()) return false;
		for (int i = 0; i < size; i++) {
			if (!value.equals(that.get(i))) return false;
		}
		return true;
	}

	@Override
	public String toString() {
		switch (size) {
		case 0: return "[]";
		case 1: return '[' + value.toString() + ']';
		default:
			String str = value.toString();
			StringBuilder sb = new StringBuilder(1 + (str.length() + 1) * size);
			sb.append('[').append(str).append(',');
			for (int i = 1; i < size; i++) {
				sb.append(',').append(str);
			}
			return sb.append(']').toString();
		}
	}
}
