package com.tomgibara.storage;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;

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
			action.accept(i, get(size));
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
}
