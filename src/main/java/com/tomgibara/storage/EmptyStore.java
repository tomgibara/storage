package com.tomgibara.storage;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Bijection;
import com.tomgibara.fundament.Mapping;

final class EmptyStore<V> implements Store<V> {

	private final Class<V> type;
	private final boolean mutable;

	EmptyStore(Class<V> type, boolean mutable) {
		this.type = type;
		this.mutable = mutable;
	}

	// store methods

	@Override
	public Iterator<V> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public boolean isMutable() {
		return mutable;
	}

	@Override
	public Store<V> mutableCopy() {
		return mutable();
	}

	@Override
	public Store<V> immutableCopy() {
		return immutable();
	}

	@Override
	public Store<V> immutableView() {
		return immutable();
	}

	@Override
	public Store<V> immutable() {
		return mutable ? new EmptyStore<>(type, false) : this;
	}

	@Override
	public Store<V> mutable() {
		return mutable ? this : new EmptyStore<>(type, true);
	}

	@Override
	public void transpose(int i, int j) {
		if (i != 0) throw new IllegalArgumentException("invalid i");
		if (j != 0) throw new IllegalArgumentException("invalid j");
	}

	@Override
	public Class<V> valueType() {
		return type;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public V get(int index) {
		throw new IllegalArgumentException("invalid index");
	}

	@Override
	public V set(int index, V value) {
		throw new IllegalArgumentException("invalid index");
	}

	@Override
	public List<V> asList() {
		return Collections.emptyList();
	}

	@Override
	public <W> Store<W> asTransformedBy(Bijection<V, W> fn) {
		return new EmptyStore<>(fn.rangeType(), mutable);
	}

	@Override
	public <W> Store<W> asTransformedBy(Mapping<V, W> fn) {
		return new EmptyStore<>(fn.rangeType(), mutable);
	}

	@Override
	public Store<V> asTransformedBy(UnaryOperator<V> fn) {
		return new EmptyStore<>(type, mutable);
	}

	@Override
	public <W> Iterator<W> transformedIterator(BiFunction<Integer, V, W> fn) {
		return Collections.emptyIterator();
	}

	@Override
	public <W> Iterator<W> transformedIterator(Function<V, W> fn) {
		return Collections.emptyIterator();
	}

	@Override
	public Spliterator<V> spliterator() {
		return Spliterators.emptySpliterator();
	}

	@Override
	public boolean compact() {
		return false;
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public Store<V> copiedBy(Storage<V> storage) {
		return storage.newStore(0);
	}

	@Override
	public boolean isNull(int index) {
		throw new IllegalArgumentException("invalid index");
	}

	@Override
	public BitStore population() {
		return Bits.noBits();
	}

	@Override
	public void clear() { }

	@Override
	public void forEach(BiConsumer<Integer, ? super V> action) { }

	@Override
	public void forEach(Consumer<? super V> action) { }

	@Override
	public void fill(V value) { }

	// Object methods

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Store)) return false;
		Store<?> that = (Store<?>) obj;
		return this.size() == that.size();
	}

	@Override
	public String toString() {
		return "[]";
	}
}
