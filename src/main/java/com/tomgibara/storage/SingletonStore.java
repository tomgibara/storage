package com.tomgibara.storage;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;

// efficient implementations for immutable, non-null singletons
abstract class SingletonStore<V> extends AbstractStore<V> {

	// statics

	private static void checkIndex(int index) {
		if (index != 0) throw new IllegalArgumentException("invalid index");
	}

	// store methods

	@Override
	public int size() { return 1; }

	@Override
	public V get(int index) {
		checkIndex(index);
		return get();
	}

	@Override
	public boolean isNull(int index) {
		checkIndex(index);
		return false;
	}

	// type is abstract

	@Override
	public int count() {
		return 1;
	}

	@Override
	public BitStore population() {
		return Bits.oneBit();
	}

	@Override
	public Store<V> range(int from, int to) {
		if (from < 0) throw new IllegalArgumentException("negative from");
		if (from > to) throw new IllegalArgumentException("from exceeds to");
		if (to > 1) throw new IllegalArgumentException("to exceeds size");
		return from == to ? new EmptyStore<>(type(), false) : this;
	}

	@Override
	public List<V> asList() {
		return Collections.singletonList(get());
	}

	@Override
	public Iterator<V> iterator() {
		return asList().iterator();
	}

	@Override
	public void forEach(Consumer<? super V> action) {
		if (action == null) throw new IllegalArgumentException("null action");
		action.accept(get());
	}

	@Override
	public void forEach(BiConsumer<Integer, ? super V> action) {
		if (action == null) throw new IllegalArgumentException("null action");
		action.accept(0, get());
	}

	@Override
	public void transpose(int i, int j) {
		if (i != 0) throw new IllegalArgumentException("invalid i");
		if (j != 0) throw new IllegalArgumentException("invalid j");
	}


	abstract V get();

	static class ObjectStore<V> extends SingletonStore<V> {

		private final V v;

		ObjectStore(V v) { this.v = v; }

		@Override
		public StoreType<V> type() {
			return (StoreType<V>) StoreType.of(v.getClass()).settingNullDisallowed();
		}

		@Override V get() { return v; }
	}

	static class TypedStore<V> extends SingletonStore<V> {

		private final StoreType<V> type;
		private final V v;

		TypedStore(StoreType<V> type, V v) {
			this.type = type;
			this.v = v;
		}

		@Override public StoreType<V> type() { return type; }

		@Override V get() { return v; }
	}

	static class ByteStore extends SingletonStore<Byte> {
		private final byte v;
		public ByteStore(byte v) { this.v = v; }
		@Override public StoreType<Byte> type() { return StoreType.BYTE_NN; }
		@Override Byte get() { return v; }
	}

	static class BooleanStore extends SingletonStore<Boolean> {
		private final boolean v;
		public BooleanStore(boolean v) { this.v = v; }
		@Override public StoreType<Boolean> type() { return StoreType.BOOLEAN_NN; }
		@Override Boolean get() { return v; }
	}

	static class ShortStore extends SingletonStore<Short> {
		private final short v;
		public ShortStore(short v) { this.v = v; }
		@Override public StoreType<Short> type() { return StoreType.SHORT_NN; }
		@Override Short get() { return v; }
	}

	static class IntStore extends SingletonStore<Integer> {
		private final int v;
		public IntStore(int v) { this.v = v; }
		@Override public StoreType<Integer> type() { return StoreType.INT_NN; }
		@Override Integer get() { return v; }
	}

	static class LongStore extends SingletonStore<Long> {
		private final long v;
		public LongStore(long v) { this.v = v; }
		@Override public StoreType<Long> type() { return StoreType.LONG_NN; }
		@Override Long get() { return v; }
	}

	static class CharStore extends SingletonStore<Character> {
		private final char v;
		public CharStore(char v) { this.v = v; }
		@Override public StoreType<Character> type() { return StoreType.CHAR_NN; }
		@Override Character get() { return v; }
	}

	static class FloatStore extends SingletonStore<Float> {
		private final float v;
		public FloatStore(float v) { this.v = v; }
		@Override public StoreType<Float> type() { return StoreType.FLOAT_NN; }
		@Override Float get() { return v; }
	}

	static class DoubleStore extends SingletonStore<Double> {
		private final double v;
		public DoubleStore(double v) { this.v = v; }
		@Override public StoreType<Double> type() { return StoreType.DOUBLE_NN; }
		@Override Double get() { return v; }
	}


}
