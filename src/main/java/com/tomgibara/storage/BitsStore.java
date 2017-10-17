package com.tomgibara.storage;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Bijection;
import com.tomgibara.fundament.Mapping;

//TODO transformed BitsStore does not report itself as mutable - possibly make transformedBy contract more lenient?
final class BitsStore extends AbstractStore<Boolean> {

	private final BitStore bits;

	BitsStore(BitStore bits) {
		this.bits = bits;
	}

	// store methods

	@Override
	public int size() {
		return bits.size();
	}

	@Override
	public Boolean get(int index) {
		return bits.getBit(index);
	}

	@Override
	public boolean isNull(int index) {
		return false;
	}

	@Override
	public StoreType<Boolean> type() {
		return StoreType.BOOLEAN_NN;
	}

	@Override
	public Boolean set(int index, Boolean value) {
		if (value == null) throw new IllegalArgumentException("null value");
		return bits.getThenSetBit(index, value);
	}

	@Override
	public boolean isSettable(Object value) {
		return value instanceof Boolean;
	}

	@Override
	public void clear() throws IllegalStateException {
		throw new IllegalStateException("null not settable");
	}

	@Override
	public void fill(Boolean value) {
		if (value == null) throw new IllegalArgumentException("null value");
		bits.setAll(value);
	}

	@Override
	public boolean compact() {
		return false;
	}

	@Override
	public Store<Boolean> resizedCopy(int newSize) {
		if (newSize > bits.size()) throw new IllegalArgumentException("cannot enlarge, null not settable");
		return new BitsStore( bits.range(0, newSize).mutableCopy() );
	}

	@Override
	public int count() {
		return bits.size();
	}

	@Override
	public BitStore population() {
		return Bits.oneBits(bits.size());
	}

	@Override
	public Store<Boolean> range(int from, int to) {
		return new BitsStore(bits.range(from, to));
	}

	@Override
	public List<Boolean> asList() {
		return bits.asList();
	}

	@Override
	public <W extends Boolean> void setStore(int position, Store<W> store) {
		BitStore bits;
		if (store instanceof BitsStore) {
			bits = ((BitsStore) store).bits;
		} else if (store.count() < store.size()) {
			throw new IllegalArgumentException("null not settable");
		} else {
			bits = new BitStore() {
				@Override public int size() { return store.size(); }
				@Override public boolean getBit(int index) { return store.get(index); }
			};
		}
		this.bits.setStore(position, bits);
	}

	@Override
	public Iterator<Boolean> iterator() {
		return bits.asList().iterator();
	}

	@Override
	public void forEach(Consumer<? super Boolean> action) {
		bits.asList().forEach(action);
	}

	@Override
	public void forEach(BiConsumer<Integer, ? super Boolean> action) {
		int size = bits.size();
		for (int i = 0; i < size; i++) {
			action.accept(i, bits.getBit(i));
		}
	}

	@Override
	public Store<Boolean> asTransformedBy(UnaryOperator<Boolean> op) {
		if (op == null) throw new IllegalArgumentException("null op");
		boolean zero = op.apply(false);
		boolean one = op.apply(true);
		if (!zero &&  one) return this.immutableView();
		if ( zero && !one) return new BitsStore(bits.flipped());
		return new BitsStore(Bits.bits(zero, bits.size()));
	}

	@Override
	public <W> Store<W> asTransformedBy(Mapping<Boolean, W> fn) {
		return transform(fn, false);
	}

	@Override
	public <W> Store<W> asTransformedBy(Bijection<Boolean, W> fn) {
		return transform(fn, true);
	}

	// transpose methods

	@Override
	public void transpose(int i, int j) {
		bits.permute().transpose(i, j);
	}

	// mutable methods

	@Override
	public boolean isMutable() {
		return bits.isMutable();
	}

	@Override
	public Store<Boolean> mutableCopy() {
		return new BitsStore(bits.mutableCopy());
	}

	@Override
	public Store<Boolean> immutableCopy() {
		return new BitsStore(bits.immutableCopy());
	}

	@Override
	public Store<Boolean> immutableView() {
		return isMutable() ? new BitsStore(bits.immutableView()) : this;
	}

	// abstract store methods

	@Override
	boolean fastFill(int from, int to, Boolean value) {
		bits.range(from, to).setAll(value);
		return true;
	}

	// private helper methods

	private <W> Store<W> transform(Mapping<Boolean, W> fn, boolean requireBijection) {
		if (fn == null) throw new IllegalArgumentException("null fn");
		W zero = fn.apply(false);
		W one = fn.apply(true);
		if (zero == null) throw new IllegalArgumentException("fn maps false to null");
		if (one == null) throw new IllegalArgumentException("fn maps true to null");
		if (requireBijection && one.equals(zero)) throw new IllegalArgumentException("fn not a bijection");
		return new Transformed<>(bits, fn.rangeType(), zero, one);
	}


	// inner classes

	private static final class Transformed<V> extends AbstractStore<V> {

		private final BitStore bits;
		private final V zero;
		private final V one;
		private final StoreType<V> type;
		private final boolean allowSet;

		Transformed(BitStore bits, Class<V> clss, V zero, V one) {
			this(bits, StoreType.of(clss).settingNullDisallowed(), zero, one, !zero.equals(one));
		}

		Transformed(Transformed<V> that, BitStore bits) {
			this(bits, that.type, that.zero, that.one, that.allowSet);
		}

		Transformed(BitStore bits, StoreType<V> type, V zero, V one, boolean allowSet) {
			this.bits = bits;
			this.type = type;
			this.zero = zero;
			this.one = one;
			this.allowSet = allowSet;
		}

		@Override
		public int size() {
			return bits.size();
		}

		@Override
		public StoreType<V> type() {
			return type;
		}

		@Override
		public V get(int index) {
			return valueOf(bits.getBit(index));
		}

		@Override
		public boolean isNull(int index) {
			return false;
		}

		@Override
		public V set(int index, V value) {
			checkAllowSet();
			return valueOf( bits.getThenSetBit(index, bitOf(value)) );
		}

		@Override
		public boolean isSettable(Object value) {
			return allowSet && value != null && (value.equals(zero) || value.equals(one));
		}

		@Override
		public void clear() throws IllegalStateException {
			throw new IllegalStateException("null not settable");
		}

		@Override
		public void fill(V value) {
			checkAllowSet();
			bits.setAll(bitOf(value));
		}

		@Override
		public boolean compact() {
			return false;
		}

		@Override
		public Store<V> resizedCopy(int newSize) {
			if (newSize > bits.size()) throw new IllegalArgumentException("cannot enlarge, null not settable");
			return new Transformed<>(this, bits.range(0, newSize).mutableCopy());
		}

		@Override
		public int count() {
			return bits.size();
		}

		@Override
		public BitStore population() {
			return Bits.oneBits(bits.size());
		}

		@Override
		public Store<V> range(int from, int to) {
			return new Transformed<>(this, bits.range(from, to));
		}

		@Override
		public <W extends V> void setStore(int position, Store<W> store) {
			checkAllowSet();
			if (store.count() < store.size()) throw new IllegalArgumentException("null not settable");
			BitStore bits = new BitStore() {
					@Override public int size() { return store.size(); }
					@Override public boolean getBit(int index) { return strictBitOf(store.get(index)); }
				};
			this.bits.setStore(position, bits);
		}

		@Override
		public void forEach(Consumer<? super V> action) {
			bits.asList().forEach(b -> {
				action.accept(valueOf(b));
			});
		}

		@Override
		public void forEach(BiConsumer<Integer, ? super V> action) {
			int size = bits.size();
			for (int i = 0; i < size; i++) {
				action.accept(i, valueOf(bits.getBit(i)));
			}
		}

		// transpose methods

		@Override
		public void transpose(int i, int j) {
			bits.permute().transpose(i, j);
		}

		// mutable methods

		@Override
		public boolean isMutable() {
			return bits.isMutable();
		}

		@Override
		public Store<V> mutableCopy() {
			return new Transformed<>(this, bits.mutableCopy());
		}

		@Override
		public Store<V> immutableCopy() {
			return new Transformed<>(this, bits.immutableCopy());
		}

		@Override
		public Store<V> immutableView() {
			return isMutable() ? new Transformed<>(this, bits.immutableView()) : this;
		}

		// abstract store methods

		@Override
		boolean fastFill(int from, int to, V value) {
			checkAllowSet();
			bits.range(from, to).setAll(bitOf(value));
			return true;
		}

		// private helper methods

		private V valueOf(boolean bit) {
			return bit ? one : zero;
		}

		private boolean bitOf(V value) {
			if (value == null) throw new IllegalArgumentException("null value");
			return value.equals(one);
		}

		private <W extends V> boolean strictBitOf(W value) {
			if (value == null) throw new IllegalArgumentException("null value");
			if (value.equals(zero)) return false;
			if (value.equals(one)) return true;
			throw new IllegalArgumentException("unsupported value");
		}

		private void checkAllowSet() {
			if (!allowSet) throw new IllegalStateException("Cannot set value on non-bijective transformation.");
		}

}
}
