/*
 * Copyright 2015 Tom Gibara
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

import java.util.Arrays;
import java.util.Optional;

abstract class PrimitiveStore<V> extends AbstractStore<V> {

	private static final int BYTE    =  1;
	private static final int FLOAT   =  2;
	private static final int CHAR    =  3;
	private static final int SHORT   =  4;
	private static final int LONG    =  6;
	private static final int INT     =  7;
	private static final int DOUBLE  = 11;
	private static final int BOOLEAN = 12;

	@SuppressWarnings("unchecked")
	static <V> PrimitiveStore<V> newStore(Class<V> type, int size, V nullValue) {
		switch((type.getName().hashCode() >> 8) & 0xf) {
		case BYTE:    return (PrimitiveStore<V>) new ByteStore     (size, (Byte)      nullValue);
		case FLOAT:   return (PrimitiveStore<V>) new FloatStore    (size, (Float)     nullValue);
		case CHAR:    return (PrimitiveStore<V>) new CharacterStore(size, (Character) nullValue);
		case SHORT:   return (PrimitiveStore<V>) new ShortStore    (size, (Short)     nullValue);
		case LONG:    return (PrimitiveStore<V>) new LongStore     (size, (Long)      nullValue);
		case INT:     return (PrimitiveStore<V>) new IntegerStore  (size, (Integer)   nullValue);
		case DOUBLE:  return (PrimitiveStore<V>) new DoubleStore   (size, (Double)    nullValue);
		case BOOLEAN: return (PrimitiveStore<V>) new BooleanStore  (size, (Boolean)   nullValue);
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
	public boolean isNull(int index) {
		if (index < 0 || index >= size()) throw new IllegalArgumentException("invalid index");
		return false;
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

		private final byte[] values;
		private final byte nullValue;

		ByteStore(int size, byte nullValue) {
			values = new byte[size];
			this.nullValue = nullValue;
			if (nullValue != (byte) 0) Arrays.fill(values, nullValue);
		}

		ByteStore(byte[] values, byte nullValue) {
			this.values = values;
			this.nullValue = nullValue;
		}

		ByteStore(byte[] values, byte nullValue, boolean mutable) {
			super(mutable);
			this.values = values;
			this.nullValue = nullValue;
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
			values[index] = value == null ? nullValue : value.byteValue();
		}

		@Override
		protected void fillImpl(Byte value) {
			Arrays.fill(values, value == null ? nullValue : value.byteValue());
		}

		@Override
		protected ByteStore duplicate(boolean copy, boolean mutable) {
			return new ByteStore(copy ? values.clone() : values, nullValue, mutable);
		}

		@Override
		protected ByteStore resize(int newSize) {
			byte[] newValues = Arrays.copyOf(values, newSize);
			if (nullValue != (byte) 0) {
				int oldSize = values.length;
				if (newSize > oldSize) Arrays.fill(newValues, oldSize, newSize, nullValue);
			}
			return new ByteStore(newValues, nullValue);
		}

		@Override
		public Optional<Byte> nullValue() {
			return Optional.of(nullValue);
		}

	}

	final static class FloatStore extends PrimitiveStore<Float> {

		private final float[] values;
		private final float nullValue;

		FloatStore(int size, float nullValue) {
			this.values = new float[size];
			this.nullValue = nullValue;
			if (nullValue != (float) 0) Arrays.fill(values, nullValue);
		}

		FloatStore(float[] values, float nullValue) {
			this.values = values;
			this.nullValue = nullValue;
		}

		FloatStore(float[] values, float nullValue, boolean mutable) {
			super(mutable);
			this.values = values;
			this.nullValue = nullValue;
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
			values[index] = value == null ? nullValue : value.floatValue();
		}

		@Override
		protected void fillImpl(Float value) {
			Arrays.fill(values, value == null ? nullValue : value.floatValue());
		}

		@Override
		protected FloatStore duplicate(boolean copy, boolean mutable) {
			return new FloatStore(copy ? values.clone() : values, nullValue, mutable);
		}

		@Override
		protected FloatStore resize(int newSize) {
			float[] newValues = Arrays.copyOf(values, newSize);
			if (nullValue != (float) 0) {
				int oldSize = values.length;
				if (newSize > oldSize) Arrays.fill(newValues, oldSize, newSize, nullValue);
			}
			return new FloatStore(newValues, nullValue);
		}

		@Override
		public Optional<Float> nullValue() {
			return Optional.of(nullValue);
		}

	}

	final static class CharacterStore extends PrimitiveStore<Character> {

		private final char[] values;
		private final char nullValue;

		CharacterStore(int size, char nullValue) {
			this.values = new char[size];
			this.nullValue = nullValue;
			if (nullValue != (char) 0) Arrays.fill(values, nullValue);
		}

		CharacterStore(char[] values, char nullValue) {
			this.values = values;
			this.nullValue = nullValue;
		}

		private CharacterStore(char[] values, char nullValue, boolean mutable) {
			super(mutable);
			this.values = values;
			this.nullValue = nullValue;
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
			values[index] = value == null ? nullValue : value.charValue();
		}

		@Override
		protected void fillImpl(Character value) {
			Arrays.fill(values, value == null ? nullValue : value.charValue());
		}

		@Override
		protected CharacterStore duplicate(boolean copy, boolean mutable) {
			return new CharacterStore(copy ? values.clone() : values, nullValue, mutable);
		}

		@Override
		protected CharacterStore resize(int newSize) {
			char[] newValues = Arrays.copyOf(values, newSize);
			if (nullValue != (char) 0) {
				int oldSize = values.length;
				if (newSize > oldSize) Arrays.fill(newValues, oldSize, newSize, nullValue);
			}
			return new CharacterStore(newValues, nullValue);
		}

		@Override
		public Optional<Character> nullValue() {
			return Optional.of(nullValue);
		}

	}

	final static class ShortStore extends PrimitiveStore<Short> {

		private final short[] values;
		private final short nullValue;

		ShortStore(int size, short nullValue) {
			this.values = new short[size];
			this.nullValue = nullValue;
			if (nullValue != (short) 0) Arrays.fill(values, nullValue);
		}

		ShortStore(short[] values, short nullValue) {
			this.values = values;
			this.nullValue = nullValue;
		}

		private ShortStore(short[] values, short nullValue, boolean mutable) {
			super(mutable);
			this.values = values;
			this.nullValue = nullValue;
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
			values[index] = value == null ? nullValue : value.shortValue();
		}

		@Override
		protected void fillImpl(Short value) {
			Arrays.fill(values, value == null ? nullValue : value.shortValue());
		}

		@Override
		protected ShortStore duplicate(boolean copy, boolean mutable) {
			return new ShortStore(copy ? values.clone() : values, nullValue, mutable);
		}

		@Override
		protected ShortStore resize(int newSize) {
			short[] newValues = Arrays.copyOf(values, newSize);
			if (nullValue != (short) 0) {
				int oldSize = values.length;
				if (newSize > oldSize) Arrays.fill(newValues, oldSize, newSize, nullValue);
			}
			return new ShortStore(newValues, nullValue);
		}

		@Override
		public Optional<Short> nullValue() {
			return Optional.of(nullValue);
		}

	}

	final static class LongStore extends PrimitiveStore<Long> {

		private final long[] values;
		private final long nullValue;

		LongStore(int size, long nullValue) {
			this.values = new long[size];
			if (nullValue != (long) 0) Arrays.fill(values, nullValue);
			this.nullValue = nullValue;
		}

		LongStore(long[] values, long nullValue) {
			this.values = values;
			this.nullValue = nullValue;
		}

		private LongStore(long[] values, long nullValue, boolean mutable) {
			super(mutable);
			this.values = values;
			this.nullValue = nullValue;
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
			values[index] = value == null ? nullValue : value.longValue();
		}

		@Override
		protected void fillImpl(Long value) {
			Arrays.fill(values, value == null ? nullValue : value.longValue());
		}

		@Override
		protected LongStore duplicate(boolean copy, boolean mutable) {
			return new LongStore(copy ? values.clone() : values, nullValue, mutable);
		}

		@Override
		protected LongStore resize(int newSize) {
			long[] newValues = Arrays.copyOf(values, newSize);
			if (nullValue != (long) 0) {
				int oldSize = values.length;
				if (newSize > oldSize) Arrays.fill(newValues, oldSize, newSize, nullValue);
			}
			return new LongStore(newValues, nullValue);
		}

		@Override
		public Optional<Long> nullValue() {
			return Optional.of(nullValue);
		}

	}

	final static class IntegerStore extends PrimitiveStore<Integer> {

		private final int[] values;
		private final int nullValue;

		IntegerStore(int size, int nullValue) {
			this.values = new int[size];
			this.nullValue = nullValue;
			if (nullValue != (int) 0) Arrays.fill(values, nullValue);
		}

		IntegerStore(int[] values, int nullValue) {
			this.values = values;
			this.nullValue = nullValue;
		}

		private IntegerStore(int[] values, int nullValue, boolean mutable) {
			super(mutable);
			this.values = values;
			this.nullValue = nullValue;
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
			values[index] = value == null ? nullValue : value.intValue();
		}

		@Override
		protected void fillImpl(Integer value) {
			Arrays.fill(values, value == null ? nullValue : value.intValue());
		}

		@Override
		protected IntegerStore duplicate(boolean copy, boolean mutable) {
			return new IntegerStore(copy ? values.clone() : values, nullValue, mutable);
		}

		@Override
		protected IntegerStore resize(int newSize) {
			int[] newValues = Arrays.copyOf(values, newSize);
			if (nullValue != (int) 0) {
				int oldSize = values.length;
				if (newSize > oldSize) Arrays.fill(newValues, oldSize, newSize, nullValue);
			}
			return new IntegerStore(newValues, nullValue);
		}

		@Override
		public Optional<Integer> nullValue() {
			return Optional.of(nullValue);
		}

	}

	final static class DoubleStore extends PrimitiveStore<Double> {

		private final double[] values;
		private final double nullValue;

		DoubleStore(int size, double nullValue) {
			this.values = new double[size];
			this.nullValue = nullValue;
			if (nullValue != (double) 0) Arrays.fill(values, nullValue);
		}

		DoubleStore(double[] values, double nullValue) {
			this.values = values;
			this.nullValue = nullValue;
		}

		private DoubleStore(double[] values, double nullValue, boolean mutable) {
			super(mutable);
			this.values = values;
			this.nullValue = nullValue;
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
			values[index] = value == null ? nullValue : value.doubleValue();
		}

		@Override
		protected void fillImpl(Double value) {
			Arrays.fill(values, value == null ? nullValue : value.doubleValue());
		}

		@Override
		protected DoubleStore duplicate(boolean copy, boolean mutable) {
			return new DoubleStore(copy ? values.clone() : values, nullValue, mutable);
		}

		@Override
		protected DoubleStore resize(int newSize) {
			double[] newValues = Arrays.copyOf(values, newSize);
			if (nullValue != (double) 0) {
				int oldSize = values.length;
				if (newSize > oldSize) Arrays.fill(newValues, oldSize, newSize, nullValue);
			}
			return new DoubleStore(newValues, nullValue);
		}

		@Override
		public Optional<Double> nullValue() {
			return Optional.of(nullValue);
		}

	}

	final static class BooleanStore extends PrimitiveStore<Boolean> {

		private final boolean[] values;
		private final boolean nullValue;

		BooleanStore(int size, boolean nullValue) {
			this.values = new boolean[size];
			this.nullValue = nullValue;
			if (nullValue) Arrays.fill(values, nullValue);
		}

		BooleanStore(boolean[] values, boolean nullValue) {
			this.values = values;
			this.nullValue = nullValue;
		}

		private BooleanStore(boolean[] values, boolean nullValue, boolean mutable) {
			super(mutable);
			this.values = values;
			this.nullValue = nullValue;
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
			values[index] = value == null ? nullValue : value.booleanValue();
		}

		@Override
		protected void fillImpl(Boolean value) {
			Arrays.fill(values, value == null ? nullValue : value.booleanValue());
		}

		@Override
		protected BooleanStore duplicate(boolean copy, boolean mutable) {
			return new BooleanStore(copy ? values.clone() : values, nullValue, mutable);
		}

		@Override
		protected BooleanStore resize(int newSize) {
			boolean[] newValues = Arrays.copyOf(values, newSize);
			if (nullValue) {
				int oldSize = values.length;
				if (newSize > oldSize) Arrays.fill(newValues, oldSize, newSize, nullValue);
			}
			return new BooleanStore(newValues, nullValue);
		}

		@Override
		public Optional<Boolean> nullValue() {
			return Optional.of(nullValue);
		}

	}

}
