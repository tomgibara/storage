package com.tomgibara.storage;

import java.util.Arrays;

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
	static <V> PrimitiveStore<V> newStore(Class<V> type, int size) {
		switch((type.getName().hashCode() >> 8) & 0xf) {
		case BYTE:    return (PrimitiveStore<V>) new ByteStore     (size);
		case FLOAT:   return (PrimitiveStore<V>) new FloatStore    (size);
		case CHAR:    return (PrimitiveStore<V>) new CharacterStore(size);
		case SHORT:   return (PrimitiveStore<V>) new ShortStore    (size);
		case LONG:    return (PrimitiveStore<V>) new LongStore     (size);
		case INT:     return (PrimitiveStore<V>) new IntegerStore  (size);
		case DOUBLE:  return (PrimitiveStore<V>) new DoubleStore   (size);
		case BOOLEAN: return (PrimitiveStore<V>) new BooleanStore  (size);
		default: throw new IllegalArgumentException(type.getName());
		}
	}

	final boolean mutable;
	
	protected PrimitiveStore() {
		mutable = true;
	}

	protected PrimitiveStore(boolean mutable) {
		this.mutable = mutable;
	}

	// store
	
	@Override
	public int count() {
		return size();
	}
	
	@Override
	public void fill(V value) {
		if (!mutable) throw new IllegalStateException("immutable");
		if (value == null) {
			clear();
		} else {
			fillImpl(value);
		}
	}

	@Override
	public V get(int index) {
		return getImpl(index);
	}

	@Override
	public V set(int index, V value) {
		if (value == null) throw new IllegalArgumentException("null not allowed");
		V previous = getImpl(index);
		setImpl(index, value);
		return previous;
	}
	
	@Override
	public Store<V> resizedCopy(int newSize) {
		if (newSize < 0) throw new IllegalArgumentException();
		return resize(newSize);
	}

	@Override
	public boolean isNullAllowed() {
		return false;
	}

	// for extension
	
	abstract protected V getImpl(int index);
	
	abstract protected void setImpl(int index, V value);
	
	abstract protected void fillImpl(V value);
	
	abstract protected Store<V> duplicate(boolean copy, boolean mutable);
	
	abstract protected Store<V> resize(int newSize);
	
	// mutability
	
	@Override
	public boolean isMutable() {
		return mutable;
	}
	
	@Override
	public Store<V> mutableCopy() {
		return duplicate(true, true);
	}

	@Override
	public Store<V> immutableCopy() {
		return duplicate(true, false);
	}
	
	// inner classes

	final static class ByteStore extends PrimitiveStore<Byte> {

		private byte[] values;

		ByteStore(int size) {
			this.values = new byte[size];
		}

		ByteStore(byte[] values) {
			this.values = values;
		}

		ByteStore(byte[] values, boolean mutable) {
			super(mutable);
			this.values = values;
		}

		@Override
		public Class<Byte> valueType() {
			return byte.class;
		}
		
		@Override
		public int size() {
			return values.length;
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
		protected ByteStore duplicate(boolean copy, boolean mutable) {
			return new ByteStore(copy ? values.clone() : values, mutable);
		}

		@Override
		protected ByteStore resize(int newSize) {
			return new ByteStore(Arrays.copyOf(values, newSize));
		}

	}

	final static class FloatStore extends PrimitiveStore<Float> {

		private float[] values;

		FloatStore(int size) {
			this.values = new float[size];
		}

		FloatStore(float[] values) {
			this.values = values;
		}

		FloatStore(float[] values, boolean mutable) {
			super(mutable);
			this.values = values;
		}

		@Override
		public Class<Float> valueType() {
			return float.class;
		}

		@Override
		public int size() {
			return values.length;
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
		protected FloatStore duplicate(boolean copy, boolean mutable) {
			return new FloatStore(copy ? values.clone() : values, mutable);
		}

		@Override
		protected FloatStore resize(int newSize) {
			return new FloatStore(Arrays.copyOf(values, newSize));
		}

	}

	final static class CharacterStore extends PrimitiveStore<Character> {

		private char[] values;

		CharacterStore(int size) {
			this.values = new char[size];
		}

		CharacterStore(char[] values) {
			this.values = values;
		}

		private CharacterStore(char[] values, boolean mutable) {
			super(mutable);
			this.values = values;
		}

		@Override
		public Class<Character> valueType() {
			return char.class;
		}

		@Override
		public int size() {
			return values.length;
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
		protected CharacterStore duplicate(boolean copy, boolean mutable) {
			return new CharacterStore(copy ? values.clone() : values, mutable);
		}

		@Override
		protected CharacterStore resize(int newSize) {
			return new CharacterStore(Arrays.copyOf(values, newSize));
		}

	}

	final static class ShortStore extends PrimitiveStore<Short> {

		private short[] values;

		ShortStore(short[] values) {
			this.values = values;
		}

		ShortStore(int size) {
			this.values = new short[size];
		}

		private ShortStore(short[] values, boolean mutable) {
			super(mutable);
			this.values = values;
		}

		@Override
		public Class<Short> valueType() {
			return short.class;
		}

		@Override
		public int size() {
			return values.length;
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
		protected ShortStore duplicate(boolean copy, boolean mutable) {
			return new ShortStore(copy ? values.clone() : values, mutable);
		}

		@Override
		protected ShortStore resize(int newSize) {
			return new ShortStore(Arrays.copyOf(values, newSize));
		}

	}

	final static class LongStore extends PrimitiveStore<Long> {

		private long[] values;

		LongStore(long[] values) {
			this.values = values;
		}

		LongStore(int size) {
			this.values = new long[size];
		}

		private LongStore(long[] values, boolean mutable) {
			super(mutable);
			this.values = values;
		}

		@Override
		public Class<Long> valueType() {
			return long.class;
		}

		@Override
		public int size() {
			return values.length;
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
		protected LongStore duplicate(boolean copy, boolean mutable) {
			return new LongStore(copy ? values.clone() : values, mutable);
		}

		@Override
		protected LongStore resize(int newSize) {
			return new LongStore(Arrays.copyOf(values, newSize));
		}

	}

	final static class IntegerStore extends PrimitiveStore<Integer> {

		private int[] values;

		IntegerStore(int[] values) {
			this.values = values;
		}

		IntegerStore(int size) {
			this.values = new int[size];
		}

		private IntegerStore(int[] values, boolean mutable) {
			super(mutable);
			this.values = values;
		}

		@Override
		public Class<Integer> valueType() {
			return int.class;
		}

		@Override
		public int size() {
			return values.length;
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
		protected IntegerStore duplicate(boolean copy, boolean mutable) {
			return new IntegerStore(copy ? values.clone() : values, mutable);
		}

		@Override
		protected IntegerStore resize(int newSize) {
			return new IntegerStore(Arrays.copyOf(values, newSize));
		}

	}

	final static class DoubleStore extends PrimitiveStore<Double> {

		private double[] values;

		DoubleStore(double[] values) {
			this.values = values;
		}

		DoubleStore(int size) {
			this.values = new double[size];
		}

		private DoubleStore(double[] values, boolean mutable) {
			super(mutable);
			this.values = values;
		}

		@Override
		public Class<Double> valueType() {
			return double.class;
		}

		@Override
		public int size() {
			return values.length;
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
		protected DoubleStore duplicate(boolean copy, boolean mutable) {
			return new DoubleStore(copy ? values.clone() : values, mutable);
		}

		@Override
		protected DoubleStore resize(int newSize) {
			return new DoubleStore(Arrays.copyOf(values, newSize));
		}

	}

	final static class BooleanStore extends PrimitiveStore<Boolean> {

		private boolean[] values;

		BooleanStore(boolean[] values) {
			this.values = values;
		}

		BooleanStore(int size) {
			this.values = new boolean[size];
		}

		private BooleanStore(boolean[] values, boolean mutable) {
			super(mutable);
			this.values = values;
		}

		@Override
		public Class<Boolean> valueType() {
			return boolean.class;
		}

		@Override
		public int size() {
			return values.length;
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
		protected BooleanStore duplicate(boolean copy, boolean mutable) {
			return new BooleanStore(copy ? values.clone() : values, mutable);
		}

		@Override
		protected BooleanStore resize(int newSize) {
			return new BooleanStore(Arrays.copyOf(values, newSize));
		}

	}

}
