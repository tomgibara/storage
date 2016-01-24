package com.tomgibara.storage;

import java.util.Arrays;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.BitWriter;
import com.tomgibara.bits.Bits;

class SmallValueStorage implements Storage<Integer> {

	// ternary packing
	
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

	// quinary packing
	
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

	private final int range;
	private final boolean nullsAllowed;

	SmallValueStorage(int range, boolean nullsAllowed) {
		this.range = range;
		this.nullsAllowed = nullsAllowed;
	}

	@Override
	public Store<Integer> newStore(int size) throws IllegalArgumentException {
		if (size < 0) throw new IllegalArgumentException("negative size");
		if (nullsAllowed) {
			switch (range) {
			case 1: return new ZeroOrNullStore(size);
			case 2: return new NullableStore(new TernaryStore(size));
			case 4: return new NullableStore(new QuinaryStore(size));
			default: return new NullableStore(new ArbitraryStore(size, range + 1));
			}
		} else {
			switch (range) {
			case 1: return new ConstantStore(size);
			case 2: return new ZeroOrOneStore(size);
			case 3: return new TernaryStore(size);
			case 5: return new QuinaryStore(size);
			default: return new ArbitraryStore(size, range);
			}
		}
	}
	
	private static abstract class SmallValueStore extends AbstractStore<Integer> {

		final int size;
		
		SmallValueStore(int size) {
			this.size = size;
		}

		@Override
		public Class<Integer> valueType() {
			return Integer.class;
		}

		@Override
		public int size() {
			return size;
		}
		
		void checkIndex(int index) {
			if (index < 0) throw new IllegalArgumentException("negative index");
			if (index >= size) throw new IllegalArgumentException("index too large");
		}

	}

	private static abstract class NonNullStore extends SmallValueStore {
		
		NonNullStore(int size) { super(size); }

		@Override
		public boolean isNullAllowed() {
			return false;
		}

		@Override
		public BitStore population() {
			return Bits.oneBits(size);
		}
		
		@Override
		public abstract NonNullStore mutableCopy();

		@Override
		public abstract NonNullStore immutableCopy();

		@Override
		public abstract NonNullStore immutableView();

		@Override
		public void transpose(int i, int j) {
			checkIndex(i);
			checkIndex(j);
			if (i == j) return;
			setInt(j, setInt(i, getInt(j)));
		}

		abstract int setInt(int index, int value);
		
		abstract int getInt(int index);

		abstract void fillInt(int value);

	}

	//TODO return to extending SVS
	class ConstantStore extends NonNullStore {

		private final boolean mutable;
		
		ConstantStore(int size) {
			super(size);
			mutable = true;
		}
		ConstantStore(int size, boolean mutable) {
			super(size);
			this.mutable = mutable;
		}

		@Override
		public boolean isNullAllowed() {
			return false;
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
		public BitStore population() {
			return Bits.oneBits(size);
		}

		@Override
		public void fill(Integer value) {
			checkValue(value);
			checkMutable();
		}

		@Override
		public boolean isMutable() { return mutable; }

		@Override
		public ConstantStore mutableCopy() { return new ConstantStore(size, true); }

		@Override
		public ConstantStore immutableCopy() { return new ConstantStore(size, false); }

		@Override
		public ConstantStore immutableView() { return new ConstantStore(size, false); }

		@Override
		public void transpose(int i, int j) {
			checkIndex(i);
			checkIndex(j);
			checkMutable();
		}

		private void checkValue(int value) {
			if (value != 0) throw new IllegalArgumentException("non-zero value");
		}
		
		private void checkMutable() {
			if (!mutable) throw new IllegalStateException("immutable");
		}
		
		@Override
		int getInt(int index) { return 0; }
		
		@Override
		int setInt(int index, int value) { return 0; }
		
		@Override
		void fillInt(int value) { }
	}
	
	class ZeroOrNullStore extends SmallValueStore {

		private final BitStore bits;
		
		ZeroOrNullStore(int size) {
			super(size);
			bits = Bits.store(size);
		}

		ZeroOrNullStore(BitStore bits) {
			super(bits.size());
			this.bits = bits;
		}
		
		@Override
		public boolean isNullAllowed() {
			return true;
		}
		
		@Override
		public Integer get(int index) {
			checkIndex(index);
			return bits.getBit(index) ? 0 : null;
		}
		
		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			boolean v = checkedValue(value);
			return bits.getThenSetBit(index, v) ? 0 : null;
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
		
		private boolean checkedValue(Integer value) {
			if (value == null) return false;
			if (value == 0) return true;
			throw new IllegalArgumentException("value not null or zero");
		}
	}

	class ZeroOrOneStore extends SmallValueStore {

		private final BitStore bits;
		
		ZeroOrOneStore(int size) {
			super(size);
			bits = Bits.store(size);
		}

		ZeroOrOneStore(BitStore bits) {
			super(bits.size());
			this.bits = bits;
		}
		
		@Override
		public boolean isNullAllowed() {
			return false;
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
		public BitStore population() {
			return Bits.oneBits(size);
		}
		
		@Override
		public void fill(Integer value) {
			bits.setAll(checkedValue(value));
		}

		@Override
		public boolean isMutable() { return bits.isMutable(); }

		@Override
		public ZeroOrOneStore mutableCopy() { return new ZeroOrOneStore(bits.mutableCopy()); }

		@Override
		public ZeroOrOneStore immutableCopy() { return new ZeroOrOneStore(bits.immutableCopy()); }

		@Override
		public ZeroOrOneStore immutableView() { return new ZeroOrOneStore(bits.immutable()); }

		@Override
		public void transpose(int i, int j) {
			bits.permute().transpose(i, j);
		}

		private boolean checkedValue(Integer value) {
			if (value == null) throw new IllegalArgumentException("null value");
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

	private static final class TernaryStore extends NonNullStore {

		private final byte[] data;
		private final boolean mutable;

		TernaryStore(int size) {
			super(size);
			int length = (size + 4) / 5;
			data = new byte[length];
			mutable = true;
		}

		TernaryStore(int size, byte[] data, boolean mutable) {
			super(size);
			this.data = data;
			this.mutable = mutable;
		}

		@Override
		public Integer get(int index) {
			checkIndex(index);
			return getInt(index);
		}

		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			checkValue(value);
			return setInt(index, value);
		}

		@Override
		public void fill(Integer value) {
			checkValue(value);
			fillInt(value);
		}
		
		@Override
		public boolean isMutable() {
			return mutable;
		}

		@Override
		public TernaryStore mutableCopy() {
			return new TernaryStore(size, data.clone(), true);
		}

		@Override
		public TernaryStore immutableCopy() {
			return new TernaryStore(size, data.clone(), false);
		}

		@Override
		public TernaryStore immutableView() {
			return new TernaryStore(size, data, false);
		}

		@Override
		int getInt(int index) {
			int i = index / 5;
			int j = index % 5;
			byte p = data[i];
			int u = tUnpack(p);
			int d = (4-j) << 1;
			return (u >> d) & 3;
		}
		
		@Override
		int setInt(int index, int value) {
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
		void fillInt(int value) {
			int u = 0;
			for (int i = 0; i < 5; i++) {
				u <<= 2;
				u |= value;
			}
			Arrays.fill(data, tPack(u));
		}
		
		private void checkValue(Integer value) {
			if (value == null) throw new IllegalArgumentException("null value");
			if (value < 0) throw new IllegalArgumentException("negative value");
			if (value >= 3) throw new IllegalArgumentException("value too large");
		}
	}
	
	private static final class QuinaryStore extends NonNullStore {

		private final BitStore bits;

		QuinaryStore(int size) {
			super(size);
			bits = Bits.store(7 * size);
		}

		QuinaryStore(int size, BitStore bits) {
			super(size);
			this.bits = bits;
		}

		@Override
		public Integer get(int index) {
			checkIndex(index);
			return getInt(index);
		}

		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			checkValue(value);
			return setInt(index, value);
		}

		@Override
		public void fill(Integer value) {
			checkValue(value);
			fillInt(value);
		}
		
		@Override
		public boolean isMutable() {
			return bits.isMutable();
		}

		@Override
		public QuinaryStore mutableCopy() {
			return new QuinaryStore(size, bits.mutableCopy());
		}

		@Override
		public QuinaryStore immutableCopy() {
			return new QuinaryStore(size, bits.immutableCopy());
		}

		@Override
		public QuinaryStore immutableView() {
			return new QuinaryStore(size, bits.immutable());
		}

		@Override
		int getInt(int index) {
			int i = index / 3;
			int j = index % 3;
			int p = getBits(i);
			int u = qUnpack(p);
			int d = (2-j) * 3;
			return (u >> d) & 7;
		}
		
		@Override
		int setInt(int index, int value) {
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
		void fillInt(int value) {
			if (value == 0) {
				bits.clear();
			} else {
				int u = 0;
				for (int i = 0; i < 3; i++) {
					u <<= 3;
					u |= value;
				}
				int p = qPack(u);
				BitWriter w = bits.openWriter();
				for (int i = 0; i < size; i++) {
					w.write(p, 7);
				}
				w.flush();
			}
		}
		
		private void checkValue(Integer value) {
			if (value == null) throw new IllegalArgumentException("null value");
			if (value < 0) throw new IllegalArgumentException("negative value");
			if (value >= 5) throw new IllegalArgumentException("value too large");
		}
		
		private int getBits(int index) {
			return (int) bits.getBits(index * 7, 7);
		}
		
		private void setBits(int index, int value) {
			bits.setBits(index * 7, value, 7);
		}
	}
	
	private final static class ArbitraryStore extends NonNullStore {

		private final int range;
		private final int count;
		private final BitStore bits;

		ArbitraryStore(int size, int range) {
			super(size);
			this.range = range;
			this.count = Integer.highestOneBit(range - 1) << 1;
			bits = Bits.store(size * count);
		}

		ArbitraryStore(ArbitraryStore that, BitStore bits) {
			super(that.size());
			this.range = that.range;
			this.count = that.count;
			this.bits = bits;
		}

		@Override
		public Integer get(int index) {
			checkIndex(index);
			return getInt(index);
		}

		@Override
		public Integer set(int index, Integer value) {
			checkIndex(index);
			if (value == null) throw new IllegalArgumentException("null value");
			checkValue(value);
			return setInt(index, value);
		}

		@Override
		public void fill(Integer value) {
			if (value == null) throw new IllegalArgumentException("null value");
			checkValue(value);
			if (value == 0) {
				bits.clear();
			} else {
				fillInt(value);
			}
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
		int setInt(int index, int value) {
			int position = index * count;
			int v = (int) bits.getBits(position, count);
			bits.setBits(position, value, count);
			return v;
		}
		
		@Override
		int getInt(int index) {
			return (int) bits.getBits(index * count, count);
		}

		@Override
		void fillInt(int value) {
			BitWriter w = bits.openWriter();
			for (int i = 0; i < size; i++) {
				w.write(value, count);
			}
			w.flush();
		}

		private void checkValue(int value) {
			if (value < 0L) throw new IllegalArgumentException("negative value");
			if (value >= range) throw new IllegalArgumentException("value too large");
		}
		
	}

	private static class NullableStore extends AbstractStore<Integer> {
		
		private final NonNullStore wrapped;

		NullableStore(NonNullStore wrapped) {
			this.wrapped = wrapped;
		}
		
		@Override
		public Class<Integer> valueType() {
			return Integer.class;
		}

		@Override
		public int size() {
			return wrapped.size;
		}

		@Override
		public boolean isNullAllowed() {
			return true;
		}

		@Override
		public Integer get(int index) {
			wrapped.checkIndex(index);
			return unwrap(wrapped.getInt(index));
		}

		@Override
		public Integer set(int index, Integer value) {
			wrapped.checkIndex(index);
			return unwrap( wrapped.setInt(index, wrap(value)) );
		}
		
		@Override
		public void clear() {
			wrapped.fillInt(0);
		}

		@Override
		public void fill(Integer value) {
			wrapped.fillInt(wrap(value));
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
		
		Integer unwrap(int value) {
			return value == 0 ? null : value - 1;
		}
		
		int wrap(Integer value) {
			return value == null ? 0 : value.intValue() + 1;
		}
	}
}
