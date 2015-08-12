package com.tomgibara.storage;

import java.util.Arrays;

import com.tomgibara.bits.BitVector;

abstract class PrimitiveStore<V> implements Store<V> {

	private static final int BYTE    =  1;
	private static final int FLOAT   =  2;
	private static final int CHAR    =  3;
	private static final int SHORT   =  4;
	private static final int LONG    =  6;
	private static final int INT     =  7;
	private static final int DOUBLE  = 11;
	private static final int BOOLEAN = 12;

	
	@SuppressWarnings("unchecked")
	static <V> PrimitiveStore<V> newStore(Class<V> type, int capacity) {
		switch((type.getName().hashCode() >> 8) & 0xf) {
		case BYTE:    return (PrimitiveStore<V>) new ByteStore     (capacity);
		case FLOAT:   return (PrimitiveStore<V>) new FloatStore    (capacity);
		case CHAR:    return (PrimitiveStore<V>) new CharacterStore(capacity);
		case SHORT:   return (PrimitiveStore<V>) new ShortStore    (capacity);
		case LONG:    return (PrimitiveStore<V>) new LongStore     (capacity);
		case INT:     return (PrimitiveStore<V>) new IntegerStore  (capacity);
		case DOUBLE:  return (PrimitiveStore<V>) new DoubleStore   (capacity);
		case BOOLEAN: return (PrimitiveStore<V>) new BooleanStore  (capacity);
		default: throw new IllegalArgumentException(type.getName());
		}
	}

	int size;
	BitVector populated;
	
	protected PrimitiveStore(BitVector populated, int size) {
		this.populated = populated;
		this.size = size;
	}
	
	protected PrimitiveStore(int capacity) {
		populated = new BitVector(capacity);
		this.size = 0;
	}

	// store
	
	@Override
	public int size() {
		return size;
	}
	
	public int capacity() {
		return populated.size();
	}
	
	@Override
	public void clear() {
		populated.set(false);
		size = 0;
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
				size --;
			} else {
				setImpl(index, value);
			}
			return previous;
		} else if (value != null) {
			setImpl(index, value);
			populated.setBit(index, true);
			size ++;
		}
		return null;
	}
	
	@Override
	public Store<V> withCapacity(int newCapacity) {
		return duplicate(populated.resizedCopy(newCapacity), true);
	}

	// for extension
	
	abstract protected V getImpl(int index);
	
	abstract protected void setImpl(int index, V value);
	
	abstract protected Store<V> duplicate(BitVector populated, boolean copy);
	
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
	public Store<V> mutableView() {
		if (!isMutable()) throw new IllegalStateException("Cannot take mutable view of immutable store");
		return duplicate(populated, false);
	}
	
	@Override
	public Store<V> immutableCopy() {
		return duplicate(populated.immutableCopy(), true);
	}
	
	@Override
	public Store<V> immutableView() {
		return new ImmutableStore<>(this);
	}
	
	// inner classes

	private final static class ByteStore extends PrimitiveStore<Byte> {

		private byte[] values;

		ByteStore(int capacity) {
			super(capacity);
			this.values = new byte[capacity];
		}

		private ByteStore(BitVector populated, int size, byte[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<? extends Byte> valueType() {
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
		protected ByteStore duplicate(BitVector populated, boolean copy) {
			if (!copy) return new ByteStore(populated, size, values);
			int capacity = populated.size();
			if (capacity == values.length) return new ByteStore(populated, size, values.clone());
			return new ByteStore(populated, size, Arrays.copyOf(values, capacity));
		}

	}

	private final static class FloatStore extends PrimitiveStore<Float> {

		private float[] values;

		FloatStore(int capacity) {
			super(capacity);
			this.values = new float[capacity];
		}

		private FloatStore(BitVector populated, int size, float[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<? extends Float> valueType() {
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
		protected FloatStore duplicate(BitVector populated, boolean copy) {
			if (!copy) return new FloatStore(populated, size, values);
			int capacity = populated.size();
			if (capacity == values.length) return new FloatStore(populated, size, values.clone());
			return new FloatStore(populated, size, Arrays.copyOf(values, capacity));
		}

	}

	private final static class CharacterStore extends PrimitiveStore<Character> {

		private char[] values;

		CharacterStore(int capacity) {
			super(capacity);
			this.values = new char[capacity];
		}

		private CharacterStore(BitVector populated, int size, char[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<? extends Character> valueType() {
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
		protected CharacterStore duplicate(BitVector populated, boolean copy) {
			if (!copy) return new CharacterStore(populated, size, values);
			int capacity = populated.size();
			if (capacity == values.length) return new CharacterStore(populated, size, values.clone());
			return new CharacterStore(populated, size, Arrays.copyOf(values, capacity));
		}

	}

	private final static class ShortStore extends PrimitiveStore<Short> {

		private short[] values;

		ShortStore(int capacity) {
			super(capacity);
			this.values = new short[capacity];
		}

		private ShortStore(BitVector populated, int size, short[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<? extends Short> valueType() {
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
		protected ShortStore duplicate(BitVector populated, boolean copy) {
			if (!copy) return new ShortStore(populated, size, values);
			int capacity = populated.size();
			if (capacity == values.length) return new ShortStore(populated, size, values.clone());
			return new ShortStore(populated, size, Arrays.copyOf(values, capacity));
		}

	}

	private final static class LongStore extends PrimitiveStore<Long> {

		private long[] values;

		LongStore(int capacity) {
			super(capacity);
			this.values = new long[capacity];
		}

		private LongStore(BitVector populated, int size, long[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<? extends Long> valueType() {
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
		protected LongStore duplicate(BitVector populated, boolean copy) {
			if (!copy) return new LongStore(populated, size, values);
			int capacity = populated.size();
			if (capacity == values.length) return new LongStore(populated, size, values.clone());
			return new LongStore(populated, size, Arrays.copyOf(values, capacity));
		}

	}

	private final static class IntegerStore extends PrimitiveStore<Integer> {

		private int[] values;

		IntegerStore(int capacity) {
			super(capacity);
			this.values = new int[capacity];
		}

		private IntegerStore(BitVector populated, int size, int[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<? extends Integer> valueType() {
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
		protected IntegerStore duplicate(BitVector populated, boolean copy) {
			if (!copy) return new IntegerStore(populated, size, values);
			int capacity = populated.size();
			if (capacity == values.length) return new IntegerStore(populated, size, values.clone());
			return new IntegerStore(populated, size, Arrays.copyOf(values, capacity));
		}

	}

	private final static class DoubleStore extends PrimitiveStore<Double> {

		private double[] values;

		DoubleStore(int capacity) {
			super(capacity);
			this.values = new double[capacity];
		}

		private DoubleStore(BitVector populated, int size, double[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<? extends Double> valueType() {
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
		protected DoubleStore duplicate(BitVector populated, boolean copy) {
			if (!copy) return new DoubleStore(populated, size, values);
			int capacity = populated.size();
			if (capacity == values.length) return new DoubleStore(populated, size, values.clone());
			return new DoubleStore(populated, size, Arrays.copyOf(values, capacity));
		}

	}

	private final static class BooleanStore extends PrimitiveStore<Boolean> {

		private boolean[] values;

		BooleanStore(int capacity) {
			super(capacity);
			this.values = new boolean[capacity];
		}

		private BooleanStore(BitVector populated, int size, boolean[] values) {
			super(populated, size);
			this.values = values;
		}

		@Override
		public Class<? extends Boolean> valueType() {
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
		protected BooleanStore duplicate(BitVector populated, boolean copy) {
			if (!copy) return new BooleanStore(populated, size, values);
			int capacity = populated.size();
			if (capacity == values.length) return new BooleanStore(populated, size, values.clone());
			return new BooleanStore(populated, size, Arrays.copyOf(values, capacity));
		}

	}

}
