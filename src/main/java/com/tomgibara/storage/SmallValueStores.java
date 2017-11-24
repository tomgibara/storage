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

import java.util.Arrays;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.BitWriter;
import com.tomgibara.bits.Bits;
import com.tomgibara.storage.StoreAccessors.StoreInts;

abstract class SmallValueStore extends AbstractStore<Integer> implements StoreInts {

	// statics - ternary packing

	private static final byte[] TERNARY_PACK = new byte[1024];
	private static final int[] TERNARY_UNPACK = new int[243];

	private static byte tPack(int value) {
		int sum = 0;
		for (int i = 0; i < 5; i++) {
			sum = 3 * sum + (value & 3);
			value >>= 2;
		}
		return (byte) sum;
	}

	private static int tUnpack(byte value) {
		int v = value & 0xff;
		int bits = 0;
		for (int i = 0; i < 5; i++) {
			bits = (bits << 2) | (v % 3);
			v /= 3;
		}
		return bits;
	}

	static {
		for (int i = 0; i < 243; i++) {
			byte p = (byte) i;
			int u = tUnpack(p);
			TERNARY_PACK[u] = p;
			TERNARY_UNPACK[i] = u;
		}
	}

	// statics - quinary packing

	private static final int[] QUINARY_PACK = new int[512];
	private static final int[] QUINARY_UNPACK = new int[125];

	private static byte qPack(int value) {
		int sum = 0;
		for (int i = 0; i < 3; i++) {
			sum = 5 * sum + (value & 7);
			value >>= 3;
		}
		return (byte) sum;
	}

	private static int qUnpack(int v) {
		int bits = 0;
		for (int i = 0; i < 3; i++) {
			bits = (bits << 3) | (v % 5);
			v /= 5;
		}
		return bits;
	}

	static {
		for (int i = 0; i < 125; i++) {
			int u = qUnpack(i);
			QUINARY_PACK[u] = i;
			QUINARY_UNPACK[i] = u;
		}
	}

	// statics

	static Storage<Integer> newStorage(int range, StoreType<Integer> type) {
		if (type.nullGettable) {
			return SmallValueStore.newNullStorage(range);
		}
		if (type.nullSettable) {
			int nv = type.nullValue;
			if (nv < 0) throw new IllegalArgumentException("negative nullValue");
			if (nv >= range) throw new IllegalArgumentException("nullValue meets or exceeds range");
		}

		return SmallValueStore.newNonNullStorage(range, type);
	}

	// type must not be null gettable
	static SmallValueStorage newNonNullStorage(int range, StoreType<Integer> type) {
		int nullValue = type.nullSettable ? type.nullValue : -1;
		switch (range) {
		case 1:  return new SmallValueStorage(type, (size, value) -> new UnaryStore(checkedSize(size), nullValue, value));
		case 2:  return new SmallValueStorage(type, (size, value) -> new BinaryStore(checkedSize(size), nullValue, value));
		case 3:  return new SmallValueStorage(type, (size, value) -> new TernaryStore(checkedSize(size), nullValue, value));
		case 5:  return new SmallValueStorage(type, (size, value) -> new QuinaryStore(checkedSize(size), nullValue, value));
		default: return new SmallValueStorage(type, (size, value) -> new ArbitraryStore(checkedSize(size), nullValue, range, value));
		}
	}

	static NullSmallStorage newNullStorage(int range) {
		switch (range) {
		case 1:  return (size, value) -> new ZeroOrNullStore(checkedSize(size), value);
		case 2:  return (size, value) -> new NullableStore(new TernaryStore(checkedSize(size), 0, value));
		case 4:  return (size, value) -> new NullableStore(new QuinaryStore(checkedSize(size), 0, value));
		default: return (size, value) -> new NullableStore(new ArbitraryStore(checkedSize(size), 0, range + 1, value));
		}
	}

	private static int checkedSize(int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		return size;
	}

	// fields

	final int size;
	// -1 indicates not settable
	final int nullValue;

	// constructors

	SmallValueStore(int size, int nullValue) {
		this.size = size;
		this.nullValue = nullValue;
	}

	// methods

	@Override
	public StoreType<Integer> type() {
		return nullValue < 0 ? StoreType.INT.settingNullDisallowed() : StoreType.INT.settingNullToValue(nullValue);

	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isNull(int index) {
		if (index < 0 || index >= size) throw new IllegalArgumentException("invalid index");
		return false;
	}

	@Override
	public BitStore population() {
		return Bits.oneBits(size);
	}

	@Override
	public abstract SmallValueStore mutableCopy();

	@Override
	public abstract SmallValueStore immutableCopy();

	@Override
	public abstract SmallValueStore immutableView();

	@Override
	public abstract SmallValueStore resizedCopy(int newSize);

	@Override
	public void transpose(int i, int j) {
		checkIndex(i);
		checkIndex(j);
		if (i == j) return;
		setImpl(j, setImpl(i, getImpl(j)));
	}

	@Override
	boolean fastFill(int from, int to, Integer value) {
		if (from == 0 && to == size) {
			fillImpl(value);
		} else {
			fillImpl(from, to, value);
		}
		return true;
	}

	// store ints methods

	@Override
	public boolean isInt(int index) {
		return true;
	}

	@Override
	public int getInt(int index) {
		checkIndex(index);
		return getImpl(index);
	}

	@Override
	public void setInt(int index, int value) {
		checkIndex(index);
		checkImpl(value);
		if (!isMutable()) throw new IllegalStateException("immutable");
		setImpl(index, value);
	}

	// for extension

	abstract int range();

	// note: caller responsible for checking value is valid
	abstract int setImpl(int index, int value);

	// note: caller responsible for checking value is valid
	abstract int getImpl(int index);

	// note: caller responsible for checking value is valid
	abstract void fillImpl(int value);

	// note: caller responsible for checking value is valid
	abstract void fillImpl(int from, int to, int value);

	abstract void checkImpl(int value);

	// helper methods

	void checkIndex(int index) {
		if (index < 0) throw new IllegalArgumentException("negative index");
		if (index >= size) throw new IllegalArgumentException("index too large");
	}

	void checkNewSize(int newSize) {
		if (newSize < 0) throw new IllegalArgumentException("negative newSize");
		if (nullValue < 0 && newSize > size) throw new IllegalArgumentException("cannot create copy with greater size, no null value");
	}

	void initCheck(Integer initialValue) {
		if (initialValue == null && nullValue < 0 && size > 0) throw new IllegalArgumentException("cannot create sized store, no null value");
	}

	// assumes initCheck has already been performed
	void initFill(Integer initialValue) {
		int i = initialValue == null ? nullValue : initialValue.intValue();
		if (i > 0) fillImpl(i);
	}

	int settableValue(Object value) {
		if (value == null) return nullValue;
		return value instanceof Integer ? (Integer) value : -1;
	}

	// inner classes

	static final class SmallValueStorage implements Storage<Integer> {

		interface Factory { SmallValueStore newStore(int size, Integer value); }

		private final StoreType<Integer> type;
		private final Factory newStore;

		SmallValueStorage(StoreType<Integer> type, Factory newStore) {
			this.type = type;
			this.newStore = newStore;
		}

		@Override
		public StoreType<Integer> type() { return type; }

		@Override
		//TODO optimize other storage methods?
		public SmallValueStore newStore(int size, Integer value) {
			return newStore.newStore(size, value);
		}

	}

	private final static class UnaryStore extends SmallValueStore {

		private final boolean mutable;

		UnaryStore(int size, int nullValue, Integer initialValue) {
			super(size, nullValue);
			initCheck(initialValue);
			mutable = true;
			initFill(initialValue);
		}
		UnaryStore(int size, int nullValue, boolean mutable) {
			super(size, nullValue);
			this.mutable = mutable;
		}

		@Override
		public Integer get(int index) {
			checkIndex(index);
			return 0;
		}

		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			checkValue(value);
			checkMutable();
			return 0;
		}

		@Override
		public boolean isSettable(Object value) {
			return settableValue(value) == 0;
		}

		@Override
		public BitStore population() {
			return Bits.oneBits(size);
		}

		@Override
		public void fill(Integer value) {
			checkValue(value);
			checkMutable();
			fillImpl(value);
		}

		@Override
		public boolean isMutable() { return mutable; }

		@Override
		public UnaryStore mutableCopy() { return new UnaryStore(size, nullValue, true); }

		@Override
		public UnaryStore immutableCopy() { return new UnaryStore(size, nullValue, false); }

		@Override
		public UnaryStore immutableView() { return new UnaryStore(size, nullValue, false); }

		@Override
		public UnaryStore resizedCopy(int newSize) {
			checkNewSize(newSize);
			return new UnaryStore(newSize, nullValue, true);
		}

		@Override
		public <W extends Integer> void setStore(int position, Store<W> store) {
			int from = 0;
			int to = checkSetStore(position, store);
			if (store instanceof RangeStore<?>) {
				RangeStore<W> range = (RangeStore<W>) store;
				store = range.store;
				from = range.from;
				to = range.to;
			}
			if (store instanceof UnaryStore) {
				/* no op */
			} else {
				setStoreImpl(position, store, from, to);
			}
		}

		@Override
		public void transpose(int i, int j) {
			checkIndex(i);
			checkIndex(j);
			checkMutable();
		}

		private void checkValue(Integer value) {
			if (value == null) {
				if (nullValue < 0) StoreType.failNull();
			} else {
				checkImpl(value);
			}
		}

		private void checkMutable() {
			if (!mutable) throw immutableException();
		}

		@Override
		int range() { return 1; }

		@Override
		int getImpl(int index) { return 0; }

		@Override
		int setImpl(int index, int value) { return 0; }

		@Override
		void fillImpl(int value) { }

		@Override
		void fillImpl(int from, int to, int value) { }

		@Override
		void checkImpl(int value) {
			if (value != 0) throw new IllegalArgumentException("non-zero value");
		}

	}

	private final static class BinaryStore extends SmallValueStore {

		private final BitStore bits;

		BinaryStore(int size, int nullValue, Integer initialValue) {
			super(size, nullValue);
			initCheck(initialValue);
			bits = Bits.store(size);
			initFill(initialValue);
		}

		BinaryStore(BitStore bits, int nullValue) {
			super(bits.size(), nullValue);
			this.bits = bits;
		}

		@Override
		public Integer get(int index) {
			checkIndex(index);
			return valueOf( bits.getBit(index) );
		}

		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			boolean v = checkedValue(value);
			return valueOf( bits.getThenSetBit(index, v) );
		}

		@Override
		public boolean isSettable(Object value) {
			int i = settableValue(value);
			return i == 0 || i == 1;
		}

		@Override
		public void fill(Integer value) {
			bits.setAll(checkedValue(value));
		}

		@Override
		public boolean isMutable() { return bits.isMutable(); }

		@Override
		public BinaryStore mutableCopy() { return new BinaryStore(bits.mutableCopy(), nullValue); }

		@Override
		public BinaryStore immutableCopy() { return new BinaryStore(bits.immutableCopy(), nullValue); }

		@Override
		public BinaryStore immutableView() { return new BinaryStore(bits.immutable(), nullValue); }

		@Override
		public BinaryStore resizedCopy(int newSize) {
			checkNewSize(newSize);
			BitStore newBits = Bits.resizedCopyOf(bits, newSize, false);
			if (newSize > size && nullValue == 1) newBits.range(size, newSize).fill();
			return new BinaryStore(newBits, nullValue);
		}

		@Override
		public <W extends Integer> void setStore(int position, Store<W> store) {
			int from = 0;
			int to = checkSetStore(position, store);
			if (store instanceof RangeStore<?>) {
				RangeStore<W> range = (RangeStore<W>) store;
				store = range.store;
				from = range.from;
				to = range.to;
			}
			if (store instanceof BinaryStore) {
				bits.setStore(position, ((BinaryStore) store).bits.range(from, to));
			} else {
				setStoreImpl(position, store, from, to);
			}
		}

		@Override
		public void transpose(int i, int j) {
			bits.permute().transpose(i, j);
		}

		@Override
		int range() { return 2; }

		@Override
		int getImpl(int index) {
			return valueOf( bits.getBit(index) );
		}

		@Override
		int setImpl(int index, int value) {
			return valueOf( bits.getThenSetBit(index, value != 0) );
		}

		@Override
		void fillImpl(int value) {
			bits.setAll(value != 0);
		}

		@Override
		void fillImpl(int from, int to, int value) {
			bits.range(from, to).setAll(value != 0);
		}

		@Override
		void checkImpl(int value) {
			if (value != 0 && value != 1) throw new IllegalArgumentException("value not 0 or 1");
		}

		private boolean checkedValue(Integer value) {
			if (value == null) {
				if (nullValue < 0) StoreType.failNull();
				return nullValue > 0;
			}
			switch (value) {
			case 0 : return false;
			case 1 : return true;
			default: throw new IllegalArgumentException("value not 0 or 1");
			}
		}

		private int valueOf(boolean v) {
			return v ? 1 : 0;
		}

	}

	private final static class TernaryStore extends SmallValueStore {

		private final byte[] data;
		private final boolean mutable;

		TernaryStore(int size, int nullValue, Integer initialValue) {
			super(size, nullValue);
			initCheck(initialValue);
			int length = (size + 4) / 5;
			data = new byte[length];
			mutable = true;
			initFill(initialValue);
		}

		TernaryStore(int size, int nullValue, byte[] data, boolean mutable) {
			super(size, nullValue);
			this.data = data;
			this.mutable = mutable;
		}

		@Override
		public Integer get(int index) {
			checkIndex(index);
			return getImpl(index);
		}

		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			return setImpl(index, checkedValue(value));
		}

		@Override
		public boolean isSettable(Object value) {
			int i = settableValue(value);
			return i >= 0 && i < 3;
		}

		@Override
		public void fill(Integer value) {
			fillImpl(checkedValue(value));
		}

		@Override
		public boolean isMutable() {
			return mutable;
		}

		@Override
		public TernaryStore mutableCopy() {
			return new TernaryStore(size, nullValue, data.clone(), true);
		}

		@Override
		public TernaryStore immutableCopy() {
			return new TernaryStore(size, nullValue, data.clone(), false);
		}

		@Override
		public TernaryStore immutableView() {
			return new TernaryStore(size, nullValue, data, false);
		}

		@Override
		public TernaryStore resizedCopy(int newSize) {
			checkNewSize(newSize);
			int newLength = (newSize + 4) / 5;
			byte[] newData = Arrays.copyOfRange(data, 0, newLength);
			TernaryStore store = new TernaryStore(newSize, nullValue, newData, true);
			if (newSize > size) {
				int limit = Math.min(size / 5 * 5 + 5, newSize);
				for (int i = size; i < limit; i++) store.setImpl(i, nullValue);
				if (newLength > data.length) {
					Arrays.fill(newData, data.length, newLength, fiveCopies(nullValue));
				}
			}
			return store;
		}

		@Override
		int range() { return 3; }

		@Override
		int getImpl(int index) {
			int i = index / 5;
			int j = index % 5;
			byte p = data[i];
			int u = tUnpack(p);
			int d = (4-j) << 1;
			return (u >> d) & 3;
		}

		@Override
		int setImpl(int index, int value) {
			int i = index / 5;
			int j = index % 5;
			byte p = data[i];
			int u = tUnpack(p);
			int d = (4-j) << 1;
			int v = (u >> d) & 3;
			u &= ~(3 << d);
			u |= value << d;
			data[i] = tPack(u);
			return v;
		}

		@Override
		void fillImpl(int value) {
			Arrays.fill(data, fiveCopies(value));
		}

		@Override
		void fillImpl(int from, int to, int value) {
			for (int i = from; i < to; i++) {
				setImpl(i, value);
			}
		}

		@Override
		void checkImpl(int value) {
			if (value < 0) throw new IllegalArgumentException("negative value");
			if (value >= 3) throw new IllegalArgumentException("value too large");
		}

		private int checkedValue(Integer value) {
			if (value == null) {
				if (nullValue < 0) StoreType.failNull();
				return nullValue;
			}
			checkImpl(value);
			return value;
		}

		private byte fiveCopies(int value) {
			int u = 0;
			for (int i = 0; i < 5; i++) {
				u <<= 2;
				u |= value;
			}
			return tPack(u);
		}
	}

	private final static class QuinaryStore extends SmallValueStore {

		private final BitStore bits;

		QuinaryStore(int size, int nullValue, Integer initialValue) {
			super(size, nullValue);
			initCheck(initialValue);
			bits = Bits.store((size + 2) / 3 * 7);
			initFill(initialValue);
		}

		QuinaryStore(int size, int nullValue, BitStore bits) {
			super(size, nullValue);
			this.bits = bits;
		}

		@Override
		public Integer get(int index) {
			checkIndex(index);
			return getImpl(index);
		}

		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			return setImpl(index, checkedValue(value));
		}

		@Override
		public boolean isSettable(Object value) {
			int i = settableValue(value);
			return i >= 0 && i < 5;
		}

		@Override
		public void fill(Integer value) {
			fillImpl(checkedValue(value));
		}

		@Override
		public boolean isMutable() {
			return bits.isMutable();
		}

		@Override
		public QuinaryStore mutableCopy() {
			return new QuinaryStore(size, nullValue, bits.mutableCopy());
		}

		@Override
		public QuinaryStore immutableCopy() {
			return new QuinaryStore(size, nullValue, bits.immutableCopy());
		}

		@Override
		public QuinaryStore immutableView() {
			return new QuinaryStore(size, nullValue, bits.immutable());
		}

		@Override
		public QuinaryStore resizedCopy(int newSize) {
			checkNewSize(newSize);
			BitStore newBits = Bits.resizedCopyOf(bits, newSize * 7, false);
			QuinaryStore store = new QuinaryStore(newSize, nullValue, newBits);
			if (size < newSize) {
				int limit = Math.min(size / 3 * 3 + 3, newSize);
				for (int i = size; i < limit; i++) {
					store.setImpl(i, nullValue);
				}
				int nbs = newBits.size();
				int obs = bits.size();
				if (nbs > obs) {
					int p = fiveCopies(nullValue);
					BitWriter w = newBits.openWriter(obs, nbs);
					for (int i = (nbs - obs) / 7; i > 0; i--) w.write(p, 7);
					w.flush();
				}
			}
			return store;
		}

		@Override
		int range() { return 5; }

		@Override
		int getImpl(int index) {
			int i = index / 3;
			int j = index % 3;
			int p = getBits(i);
			int u = qUnpack(p);
			int d = (2-j) * 3;
			return (u >> d) & 7;
		}

		@Override
		int setImpl(int index, int value) {
			int i = index / 3;
			int j = index % 3;
			int p = getBits(i);
			int u = qUnpack(p);
			int d = (2-j) * 3;
			int v = (u >> d) & 7;
			u &= ~(7 << d);
			u |= value << d;
			setBits(i, qPack(u));
			return v;
		}

		@Override
		void fillImpl(int value) {
			if (value == 0) {
				bits.clear();
			} else {
				int p = fiveCopies(value);
				BitWriter w = bits.openWriter();
				int limit = bits.size() / 7;
				for (int i = 0; i < limit; i++) {
					w.write(p, 7);
				}
				w.flush();
			}
		}

		@Override
		void fillImpl(int from, int to, int value) {
			for (int i = from; i < to; i++) {
				setImpl(i, value);
			}
		}

		@Override
		void checkImpl(int value) {
			if (value < 0) throw new IllegalArgumentException("negative value");
			if (value >= 5) throw new IllegalArgumentException("value too large");
		}

		private int checkedValue(Integer value) {
			if (value == null) {
				if (nullValue < 0) StoreType.failNull();
				return nullValue;
			}
			checkImpl(value);
			return value;
		}

		private int getBits(int index) {
			return (int) bits.getBits(index * 7, 7);
		}

		private void setBits(int index, int value) {
			bits.setBits(index * 7, value, 7);
		}

		private int fiveCopies(int value) {
			int u = 0;
			for (int i = 0; i < 3; i++) {
				u <<= 3;
				u |= value;
			}
			return qPack(u);
		}
	}

	private final static class ArbitraryStore extends SmallValueStore /* implements StoreInts */ {

		private final int range;
		private final int count;
		private final BitStore bits;

		ArbitraryStore(int size, int nullValue, int range, Integer initialValue) {
			super(size, nullValue);
			initCheck(initialValue);
			this.range = range;
			count = 32 - Integer.numberOfLeadingZeros(range - 1);
			bits = Bits.store(size * count);
			initFill(initialValue);
		}

		ArbitraryStore(ArbitraryStore that, BitStore bits) {
			super(bits.size() / that.count, that.nullValue);
			this.range = that.range;
			this.count = that.count;
			this.bits = bits;
		}

		@Override
		public Integer get(int index) {
			checkIndex(index);
			return getImpl(index);
		}

		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			return setImpl(index, checkedValue(value));
		}

		@Override
		public boolean isSettable(Object value) {
			int i = settableValue(value);
			return i >= 0 && i < range;
		}

		@Override
		public void fill(Integer value) {
			fillImpl(checkedValue(value));
		}

		@Override
		public boolean isMutable() { return bits.isMutable(); }

		@Override
		public ArbitraryStore mutableCopy() { return new ArbitraryStore(this, bits.mutableCopy()); }

		@Override
		public ArbitraryStore immutableCopy() { return new ArbitraryStore(this, bits.immutableCopy()); }

		@Override
		public ArbitraryStore immutableView() { return new ArbitraryStore(this, bits.immutable()); }

		@Override
		public ArbitraryStore resizedCopy(int newSize) {
			checkNewSize(newSize);
			BitStore newBits = Bits.resizedCopyOf(bits, newSize * count, false);
			ArbitraryStore store = new ArbitraryStore(this, newBits);
			if (size < newSize) {
				BitWriter w = newBits.openWriter(size * count, newSize * count);
				for (int i = size; i < newSize; i++) {
					w.write(nullValue, count);
				}
				w.flush();
			}
			return store;
		}

		@Override
		public <W extends Integer> void setStore(int position, Store<W> store) {
			int from = 0;
			int to = checkSetStore(position, store);
			if (store instanceof RangeStore<?>) {
				RangeStore<W> range = (RangeStore<W>) store;
				store = range.store;
				from = range.from;
				to = range.to;
			}
			if (store instanceof ArbitraryStore) {
				bits.setStore(position * count, ((ArbitraryStore) store).bits.range(from * count, to * count));
			} else {
				setStoreImpl(position, store, from, to);
			}
		}

		@Override
		int range() {
			return range;
		}

		@Override
		int setImpl(int index, int value) {
			int position = index * count;
			int v = (int) bits.getBits(position, count);
			bits.setBits(position, value, count);
			return v;
		}

		@Override
		int getImpl(int index) {
			return (int) bits.getBits(index * count, count);
		}

		@Override
		void fillImpl(int value) {
			writeFill(bits, value);
		}

		@Override
		void fillImpl(int from, int to, int value) {
			writeFill(bits.range(from * count, to * count), value);
		}

		@Override
		void checkImpl(int value) {
			if (value < 0) throw new IllegalArgumentException("negative value");
			if (value >= range) throw new IllegalArgumentException("value too large");
		}

		private int checkedValue(Integer value) {
			if (value == null) {
				if (nullValue < 0) StoreType.failNull();
				return nullValue;
			}
			checkImpl(value);
			return value;
		}

		private void writeFill(BitStore bits, int value) {
			if (value == 0) {
				bits.clear();
			} else if (value == (1 << count) - 1) {
				bits.fill();
			} else {
				int length = bits.size() / count;
				BitWriter w = bits.openWriter();
				for (int i = 0; i < length; i++) {
					w.write(value, count);
				}
				w.flush();
			}
		}
	}

	// nullable stores

	interface NullSmallStorage extends Storage<Integer> {
		@Override default public StoreType<Integer> type() { return StoreType.INT; }
	}

	static class ZeroOrNullStore extends AbstractStore<Integer> implements StoreInts {

		private static final Integer ZERO = 0;
		private final int size;
		private final BitStore bits;

		ZeroOrNullStore(int size, Integer initialValue) {
			this.size = size;
			bits = Bits.store(size);
			if (initialValue != null) {
				bits.setAll(true);
			}
		}

		ZeroOrNullStore(BitStore bits) {
			this.size = bits.size();
			this.bits = bits;
		}

		@Override
		public StoreType<Integer> type() {
			return StoreType.INT;
		}

		public int size() {
			return size;
		}

		public Class<Integer> valueType() {
			return Integer.class;
		}

		@Override
		public Integer get(int index) {
			checkIndex(index);
			return bits.getBit(index) ? 0 : null;
		}

		@Override
		public boolean isNull(int index) {
			checkIndex(index);
			return !bits.getBit(index);
		}

		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			boolean v = checkedValue(value);
			return bits.getThenSetBit(index, v) ? 0 : null;
		}

		@Override
		public boolean isSettable(Object value) {
			return value == null || ZERO.equals(value);
		}

		@Override
		public void clear() {
			bits.clear();
		}

		@Override
		public BitStore population() {
			return bits.immutableView();
		}

		@Override
		public void fill(Integer value) {
			bits.setAll(checkedValue(value));
		}

		@Override
		public Store<Integer> resizedCopy(int newSize) {
			return new ZeroOrNullStore(Bits.resizedCopyOf(bits, newSize, false));
		}

		@Override
		public <W extends Integer> void setStore(int position, Store<W> store) {
			int from = 0;
			int to = checkSetStore(position, store);
			if (store instanceof RangeStore<?>) {
				RangeStore<W> range = (RangeStore<W>) store;
				store = range.store;
				from = range.from;
				to = range.to;
			}
			if (store instanceof ZeroOrNullStore) {
				bits.setStore(position, ((ZeroOrNullStore) store).bits.range(from, to));
			} else {
				setStoreImpl(position, store, from, to);
			}
		}

		@Override
		public boolean isMutable() { return bits.isMutable(); }

		@Override
		public Store<Integer> mutableCopy() { return new ZeroOrNullStore(bits.mutableCopy()); }

		@Override
		public Store<Integer> immutableCopy() { return new ZeroOrNullStore(bits.immutableCopy()); }

		@Override
		public Store<Integer> immutableView() { return new ZeroOrNullStore(bits.immutable()); }

		@Override
		public void transpose(int i, int j) {
			bits.permute().transpose(i, j);
		}

		// store ints

		@Override
		public boolean isInt(int index) {
			return !isNull(index);
		}

		@Override
		public int getInt(int index) {
			checkIndex(index);
			if (!bits.getBit(index)) throw new RuntimeException("null, not an int");
			return 0;
		}

		@Override
		public void setInt(int index, int value) {
			checkIndex(index);
			if (value != 0) throw new IllegalArgumentException("value not zero");
			bits.setBit(index, true);
		}

		// private helper methods

		private void checkIndex(int index) {
			if (index < 0) throw new IllegalArgumentException("negative index");
			if (index >= size) throw new IllegalArgumentException("index too large");
		}

		private boolean checkedValue(Integer value) {
			if (value == null) return false;
			if (value == 0) return true;
			throw new IllegalArgumentException("value not null or zero");
		}
	}

	private static class NullableStore extends AbstractStore<Integer> implements StoreInts {

		private final SmallValueStore wrapped;

		NullableStore(SmallValueStore wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public StoreType<Integer> type() {
			return StoreType.INT;
		}

		@Override
		public int size() {
			return wrapped.size;
		}

		@Override
		public Integer get(int index) {
			wrapped.checkIndex(index);
			return unwrap(wrapped.getImpl(index));
		}

		@Override
		public boolean isNull(int index) {
			wrapped.checkIndex(index);
			return wrapped.getImpl(index) == 0;
		}

		@Override
		public Integer set(int index, Integer value) {
			wrapped.checkIndex(index);
			return unwrap( wrapped.setImpl(index, wrap(value)) );
		}

		@Override
		public boolean isSettable(Object value) {
			if (value == null) return true;
			if (!(value instanceof Integer)) return false;
			int i = ((Integer) value) + 1;
			return i >= 1 && i < wrapped.range();
		}

		@Override
		public void clear() {
			wrapped.fillImpl(0);
		}

		@Override
		public void fill(Integer value) {
			wrapped.fillImpl(wrap(value));
		}

		@Override
		public Store<Integer> resizedCopy(int newSize) {
			return new NullableStore(wrapped.resizedCopy(newSize));
		}

		@Override
		public <W extends Integer> void setStore(int position, Store<W> store) {
			if (store instanceof NullableStore) {
				wrapped.setStore(position, ((NullableStore) store).wrapped);
			} else {
				super.setStore(position, store);
			}
		}

		@Override
		public boolean isMutable() {
			return wrapped.isMutable();
		}

		@Override
		public Store<Integer> mutableCopy() {
			return new NullableStore(wrapped.mutableCopy());
		}

		@Override
		public Store<Integer> immutableCopy() {
			return new NullableStore(wrapped.immutableCopy());
		}

		@Override
		public Store<Integer> immutableView() {
			return new NullableStore(wrapped.immutableView());
		}

		@Override
		public void transpose(int i, int j) {
			wrapped.transpose(i, j);
		}

		// store ints

		@Override
		public boolean isInt(int index) {
			return !isNull(index);
		}

		@Override
		public int getInt(int index) {
			wrapped.checkIndex(index);
			int value = wrapped.getImpl(index);
			if (value == 0) throw new RuntimeException("null, not an int");
			return value - 1;
		}

		@Override
		public void setInt(int index, int value) {
			if (!wrapped.isMutable()) throw new IllegalStateException("immutable");
			wrapped.checkIndex(index);
			wrapped.setImpl(index, wrapImpl(value));
		}

		// private helper methods

		private Integer unwrap(int value) {
			return value == 0 ? null : value - 1;
		}

		private int wrap(Integer value) {
			return value == null ? 0 : wrapImpl(value);
		}

		private int wrapImpl(int value) {
			if (value < 0) throw new IllegalArgumentException("negative value");
			if (++value >= wrapped.range()) throw new IllegalArgumentException("value too large");
			return value;
		}
	}
}

