package com.tomgibara.storage;

import java.util.Arrays;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;

abstract class NullPrimitiveStore<V> extends AbstractStore<V> {

	private static final int BYTE    =  1;
	private static final int FLOAT   =  2;
	private static final int CHAR    =  3;
	private static final int SHORT   =  4;
	private static final int LONG    =  6;
	private static final int INT     =  7;
	private static final int DOUBLE  = 11;
	private static final int BOOLEAN = 12;

	@SuppressWarnings("unchecked")
	static <V> NullPrimitiveStore<V> newStore(Class<V> type, int size) {
		switch((type.getName().hashCode() >> 8) & 0xf) {
		case BYTE:    return (NullPrimitiveStore<V>) new ByteStore     (size);
		case FLOAT:   return (NullPrimitiveStore<V>) new FloatStore    (size);
		case CHAR:    return (NullPrimitiveStore<V>) new CharacterStore(size);
		case SHORT:   return (NullPrimitiveStore<V>) new ShortStore    (size);
		case LONG:    return (NullPrimitiveStore<V>) new LongStore     (size);
		case INT:     return (NullPrimitiveStore<V>) new IntegerStore  (size);
		case DOUBLE:  return (NullPrimitiveStore<V>) new DoubleStore   (size);
		case BOOLEAN: return (NullPrimitiveStore<V>) new BooleanStore  (size);
		default: throw new IllegalArgumentException(type.getName());
		}
	}

	int count;
	BitStore populated;
	
	protected NullPrimitiveStore(BitStore populated, int count) {
		this.populated = populated;
		this.count = count;
	}
	
	protected NullPrimitiveStore(int size) {
		populated = Bits.newBitStore(size);
		this.count = 0;
	}

	// store
	
	@Override
	public int count() {
		return count;
	}
	
	public int size() {
		return populated.size();
	}
	
	@Override
	public void clear() {
		populated.clearWithZeros();
		count = 0;
	}

	@Override
	public void fill(V value) {
		if (value == null) { // simple case
			clear();
		} else { // more complex case
			if (!populated.isMutable()) throw new IllegalStateException("immutable");
			// do this first in case filling fails due to class error
			fillImpl(value);
			populated.clearWithOnes();
			count = populated.size();
		}
	}

	@Override
	public V get(int index) {
		return populated.getBit(index) ? getImpl(index) : null;
	}

	@Override
	public V set(int index, V value) {
		if (populated.getBit(index)) {
			V previous = getImpl(index);
			if (value == null) {
				populated.setBit(index, false);
				count --;
			} else {
				setImpl(index, value);
			}
			return previous;
		} else if (value != null) {
			setImpl(index, value);
			populated.setBit(index, true);
			count ++;
		}
		return null;
	}
	
	@Override
	public Store<V> resizedCopy(int newSize) {
		return duplicate(Bits.resizedCopyOf(populated, newSize, false), true);
	}

	@Override
	public BitStore population() {
		return populated.immutableView();
	}

	@Override
	public boolean isNullAllowed() {
		return true;
	}

	// for extension
	
	abstract protected V getImpl(int index);
	
	abstract protected void setImpl(int index, V value);
	
	abstract protected void fillImpl(V value);
	
	abstract protected Store<V> duplicate(BitStore populated, boolean copy);
	
	// mutability
	
	@Override
	public boolean isMutable() {
		return populated.isMutable();
	}
	
	@Override
	public Store<V> mutableCopy() {
		return duplicate(populated.mutableCopy(), true);
	}

	@Override
	public Store<V> immutableCopy() {
		return duplicate(populated.immutableCopy(), true);
	}
	
	// inner classes

	final static class ByteStore extends NullPrimitiveStore<Byte> {

		private byte[] values;

		ByteStore(int size) {
			super(size);
			this.values = new byte[size];
		}

		ByteStore(byte[] values) {
			super(values.length);
			populated.flip();
			this.values = values;
		}

		private ByteStore(BitStore populated, int size, byte[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<Byte> valueType() {
			return byte.class;
		}

		@Override
		protected Byte getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Byte value) {
			values[index] = value;
		}

		@Override
		protected void fillImpl(Byte value) {
			Arrays.fill(values, value);
		}

		@Override
		protected ByteStore duplicate(BitStore populated, boolean copy) {
			if (!copy) return new ByteStore(populated, count, values);
			int size = populated.size();
			if (size == values.length) return new ByteStore(populated, count, values.clone());
			return new ByteStore(populated, count, Arrays.copyOf(values, size));
		}

	}

	final static class FloatStore extends NullPrimitiveStore<Float> {

		private float[] values;

		FloatStore(int size) {
			super(size);
			this.values = new float[size];
		}

		FloatStore(float[] values) {
			super(values.length);
			populated.flip();
			this.values = values;
		}

		private FloatStore(BitStore populated, int size, float[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<Float> valueType() {
			return float.class;
		}

		@Override
		protected Float getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Float value) {
			values[index] = value;
		}

		@Override
		protected void fillImpl(Float value) {
			Arrays.fill(values, value);
		}

		@Override
		protected FloatStore duplicate(BitStore populated, boolean copy) {
			if (!copy) return new FloatStore(populated, count, values);
			int size = populated.size();
			if (size == values.length) return new FloatStore(populated, count, values.clone());
			return new FloatStore(populated, count, Arrays.copyOf(values, size));
		}

	}

	final static class CharacterStore extends NullPrimitiveStore<Character> {

		private char[] values;

		CharacterStore(int size) {
			super(size);
			this.values = new char[size];
		}

		CharacterStore(char[] values) {
			super(values.length);
			populated.flip();
			this.values = values;
		}

		private CharacterStore(BitStore populated, int size, char[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<Character> valueType() {
			return char.class;
		}

		@Override
		protected Character getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Character value) {
			values[index] = value;
		}

		@Override
		protected void fillImpl(Character value) {
			Arrays.fill(values, value);
		}

		@Override
		protected CharacterStore duplicate(BitStore populated, boolean copy) {
			if (!copy) return new CharacterStore(populated, count, values);
			int size = populated.size();
			if (size == values.length) return new CharacterStore(populated, count, values.clone());
			return new CharacterStore(populated, count, Arrays.copyOf(values, size));
		}

	}

	final static class ShortStore extends NullPrimitiveStore<Short> {

		private short[] values;

		ShortStore(short[] values) {
			super(values.length);
			populated.flip();
			this.values = values;
		}

		ShortStore(int size) {
			super(size);
			this.values = new short[size];
		}

		private ShortStore(BitStore populated, int size, short[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<Short> valueType() {
			return short.class;
		}

		@Override
		protected Short getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Short value) {
			values[index] = value;
		}

		@Override
		protected void fillImpl(Short value) {
			Arrays.fill(values, value);
		}

		@Override
		protected ShortStore duplicate(BitStore populated, boolean copy) {
			if (!copy) return new ShortStore(populated, count, values);
			int size = populated.size();
			if (size == values.length) return new ShortStore(populated, count, values.clone());
			return new ShortStore(populated, count, Arrays.copyOf(values, size));
		}

	}

	final static class LongStore extends NullPrimitiveStore<Long> {

		private long[] values;

		LongStore(long[] values) {
			super(values.length);
			populated.flip();
			this.values = values;
		}

		LongStore(int size) {
			super(size);
			this.values = new long[size];
		}

		private LongStore(BitStore populated, int size, long[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<Long> valueType() {
			return long.class;
		}

		@Override
		protected Long getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Long value) {
			values[index] = value;
		}

		@Override
		protected void fillImpl(Long value) {
			Arrays.fill(values, value);
		}

		@Override
		protected LongStore duplicate(BitStore populated, boolean copy) {
			if (!copy) return new LongStore(populated, count, values);
			int size = populated.size();
			if (size == values.length) return new LongStore(populated, count, values.clone());
			return new LongStore(populated, count, Arrays.copyOf(values, size));
		}

	}

	final static class IntegerStore extends NullPrimitiveStore<Integer> {

		private int[] values;

		IntegerStore(int[] values) {
			super(values.length);
			populated.flip();
			this.values = values;
		}

		IntegerStore(int size) {
			super(size);
			this.values = new int[size];
		}

		private IntegerStore(BitStore populated, int size, int[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<Integer> valueType() {
			return int.class;
		}

		@Override
		protected Integer getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Integer value) {
			values[index] = value;
		}

		@Override
		protected void fillImpl(Integer value) {
			Arrays.fill(values, value);
		}

		@Override
		protected IntegerStore duplicate(BitStore populated, boolean copy) {
			if (!copy) return new IntegerStore(populated, count, values);
			int size = populated.size();
			if (size == values.length) return new IntegerStore(populated, count, values.clone());
			return new IntegerStore(populated, count, Arrays.copyOf(values, size));
		}

	}

	final static class DoubleStore extends NullPrimitiveStore<Double> {

		private double[] values;

		DoubleStore(double[] values) {
			super(values.length);
			populated.flip();
			this.values = values;
		}

		DoubleStore(int size) {
			super(size);
			this.values = new double[size];
		}

		private DoubleStore(BitStore populated, int size, double[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<Double> valueType() {
			return double.class;
		}

		@Override
		protected Double getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Double value) {
			values[index] = value;
		}

		@Override
		protected void fillImpl(Double value) {
			Arrays.fill(values, value);
		}

		@Override
		protected DoubleStore duplicate(BitStore populated, boolean copy) {
			if (!copy) return new DoubleStore(populated, count, values);
			int size = populated.size();
			if (size == values.length) return new DoubleStore(populated, count, values.clone());
			return new DoubleStore(populated, count, Arrays.copyOf(values, size));
		}

	}

	final static class BooleanStore extends NullPrimitiveStore<Boolean> {

		private boolean[] values;

		BooleanStore(boolean[] values) {
			super(values.length);
			populated.flip();
			this.values = values;
		}

		BooleanStore(int size) {
			super(size);
			this.values = new boolean[size];
		}

		private BooleanStore(BitStore populated, int size, boolean[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<Boolean> valueType() {
			return boolean.class;
		}

		@Override
		protected Boolean getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Boolean value) {
			values[index] = value;
		}

		@Override
		protected void fillImpl(Boolean value) {
			Arrays.fill(values, value);
		}

		@Override
		protected BooleanStore duplicate(BitStore populated, boolean copy) {
			if (!copy) return new BooleanStore(populated, count, values);
			int size = populated.size();
			if (size == values.length) return new BooleanStore(populated, count, values.clone());
			return new BooleanStore(populated, count, Arrays.copyOf(values, size));
		}

	}

}
