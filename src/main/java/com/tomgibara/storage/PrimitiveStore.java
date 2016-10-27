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

import static com.tomgibara.storage.Stores.immutableException;

import java.util.Arrays;

abstract class PrimitiveStore<V> extends AbstractStore<V> {

	private static final int BYTE    =  1;
	private static final int FLOAT   =  2;
	private static final int CHAR    =  3;
	private static final int SHORT   =  4;
	private static final int LONG    =  6;
	private static final int INT     =  7;
	private static final int DOUBLE  = 11;
	private static final int BOOLEAN = 12;

	private static void failGrowCopy() {
		throw new IllegalArgumentException("cannot increase size, null not settable");
	}

	private static final <T> StoreNullity<T> newNullity(boolean nullSettable, T nullValue) {
		return nullSettable ? StoreNullity.settingNullToValue(nullValue) : StoreNullity.settingNullDisallowed();
	}

	private static abstract class PrimitiveStorage<P> implements Storage<P> {
		final StoreNullity<P> nullity;
		PrimitiveStorage(StoreNullity<P> nullity) { this.nullity = nullity; }
		@Override final public StoreNullity<P> nullity() { return nullity; }
	}

	private static final Storage<Byte> byteStorage(StoreNullity<Byte> nullity) {
		return new PrimitiveStorage<Byte>(nullity) {
			@Override public Class<Byte> valueType() { return byte.class; }
			@Override public PrimitiveStore<Byte> newStore(int size) { return new ByteStore(size, nullity); }
		};
	};

	private static final Storage<Float> floatStorage(StoreNullity<Float> nullity) {
		return new PrimitiveStorage<Float>(nullity) {
			@Override public Class<Float> valueType() { return float.class; }
			@Override public PrimitiveStore<Float> newStore(int size) { return new FloatStore(size, nullity); }
		};
	};

	private static final Storage<Character> charStorage(StoreNullity<Character> nullity) {
		return new PrimitiveStorage<Character>(nullity) {
			@Override public Class<Character> valueType() { return char.class; }
			@Override public PrimitiveStore<Character> newStore(int size) { return new CharacterStore(size, nullity); }
		};
	};

	private static final Storage<Short> shortStorage(StoreNullity<Short> nullity) {
		return new PrimitiveStorage<Short>(nullity) {
			@Override public Class<Short> valueType() { return short.class; }
			@Override public PrimitiveStore<Short> newStore(int size) { return new ShortStore(size, nullity); }
		};
	};

	private static final Storage<Long> longStorage(StoreNullity<Long> nullity) {
		return new PrimitiveStorage<Long>(nullity) {
			@Override public Class<Long> valueType() { return long.class; }
			@Override public PrimitiveStore<Long> newStore(int size) { return new LongStore(size, nullity); }
		};
	};

	private static final Storage<Integer> intStorage(StoreNullity<Integer> nullity) {
		return new PrimitiveStorage<Integer>(nullity) {
			@Override public Class<Integer> valueType() { return int.class; }
			@Override public PrimitiveStore<Integer> newStore(int size) { return new IntegerStore(size, nullity); }
		};
	};

	private static final Storage<Double> doubleStorage(StoreNullity<Double> nullity) {
		return new Storage<Double>() {
			@Override public Class<Double> valueType() { return double.class; }
			@Override public StoreNullity<Double> nullity() { return nullity; }
			@Override public PrimitiveStore<Double> newStore(int size) { return new DoubleStore(size, nullity); }
		};
	};

	private static final Storage<Boolean> booleanStorage(StoreNullity<Boolean> nullity) {
		return new Storage<Boolean>() {
			@Override public Class<Boolean> valueType() { return boolean.class; }
			@Override public StoreNullity<Boolean> nullity() { return nullity; }
			@Override public PrimitiveStore<Boolean> newStore(int size) { return new BooleanStore(size, nullity); }
		};
	};

	@SuppressWarnings("unchecked")
	static <V> Storage<V> newStorage(Class<V> type, StoreNullity<V> nullity) {
		switch((type.getName().hashCode() >> 8) & 0xf) {
		case Stores.BYTE:    return (Storage<V>) byteStorage   ((StoreNullity<Byte>     ) nullity);
		case Stores.FLOAT:   return (Storage<V>) floatStorage  ((StoreNullity<Float>    ) nullity);
		case Stores.CHAR:    return (Storage<V>) charStorage   ((StoreNullity<Character>) nullity);
		case Stores.SHORT:   return (Storage<V>) shortStorage  ((StoreNullity<Short>    ) nullity);
		case Stores.LONG:    return (Storage<V>) longStorage   ((StoreNullity<Long>     ) nullity);
		case Stores.INT:     return (Storage<V>) intStorage    ((StoreNullity<Integer>  ) nullity);
		case Stores.DOUBLE:  return (Storage<V>) doubleStorage ((StoreNullity<Double>   ) nullity);
		case Stores.BOOLEAN: return (Storage<V>) booleanStorage((StoreNullity<Boolean>  ) nullity);
		default: throw new IllegalArgumentException(type.getName());
		}
	}

	@SuppressWarnings("unchecked")
	static <V> PrimitiveStore<V> newStore(Class<V> type, int size, StoreNullity<V> nullity) {
		switch((type.getName().hashCode() >> 8) & 0xf) {
		case BYTE:    return (PrimitiveStore<V>) new ByteStore     (size, (StoreNullity<Byte>     ) nullity);
		case FLOAT:   return (PrimitiveStore<V>) new FloatStore    (size, (StoreNullity<Float>    ) nullity);
		case CHAR:    return (PrimitiveStore<V>) new CharacterStore(size, (StoreNullity<Character>) nullity);
		case SHORT:   return (PrimitiveStore<V>) new ShortStore    (size, (StoreNullity<Short>    ) nullity);
		case LONG:    return (PrimitiveStore<V>) new LongStore     (size, (StoreNullity<Long>     ) nullity);
		case INT:     return (PrimitiveStore<V>) new IntegerStore  (size, (StoreNullity<Integer>  ) nullity);
		case DOUBLE:  return (PrimitiveStore<V>) new DoubleStore   (size, (StoreNullity<Double>   ) nullity);
		case BOOLEAN: return (PrimitiveStore<V>) new BooleanStore  (size, (StoreNullity<Boolean>  ) nullity);
		default: throw new IllegalArgumentException(type.getName());
		}
	}

	final boolean mutable;
	final boolean nullSettable;

	protected PrimitiveStore(boolean nullSettable) {
		mutable = true;
		this.nullSettable = nullSettable;
	}

	protected PrimitiveStore(boolean mutable, boolean nullSettable) {
		this.mutable = mutable;
		this.nullSettable = nullSettable;
	}

	// store

	@Override
	public int count() {
		return size();
	}

	@Override
	public void fill(V value) {
		if (!mutable) throw immutableException();
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
		if (value == null && !nullSettable) StoreNullity.failNull();
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

	// helper methods

	void checkSize(int size) {
		if (!nullSettable && size > 0) throw new IllegalArgumentException("no null value with which to populate store");
	}

	// inner classes

	final static class ByteStore extends PrimitiveStore<Byte> {

		private final byte[] values;
		private final byte nullValue;

		ByteStore(int size, StoreNullity<Byte> nullity) {
			super(nullity.nullSettable());
			checkSize(size);
			values = new byte[size];
			this.nullValue = nullSettable ? nullity.nullValue() : (byte) 0;
			if (nullValue != (byte) 0) Arrays.fill(values, nullValue);
		}

		ByteStore(byte[] values, StoreNullity<Byte> nullity) {
			super(nullity.nullSettable());
			this.values = values;
			this.nullValue = nullSettable ? nullity.nullValue() : (byte) 0;
		}

		private ByteStore(byte[] values, byte nullValue, boolean nullSettable) {
			super(nullSettable);
			this.values = values;
			this.nullValue = nullValue;
		}

		private ByteStore(byte[] values, byte nullValue, boolean mutable, boolean nullSettable) {
			super(mutable, nullSettable);
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
			return new ByteStore(copy ? values.clone() : values, nullValue, mutable, nullSettable);
		}

		@Override
		protected ByteStore resize(int newSize) {
			int oldSize = values.length;
			boolean growing = newSize > oldSize;
			if (growing && !nullSettable) failGrowCopy();
			byte[] newValues = Arrays.copyOf(values, newSize);
			if (growing && nullValue != (byte) 0) Arrays.fill(newValues, oldSize, newSize, nullValue);
			return new ByteStore(newValues, nullValue, nullSettable);
		}

		@Override
		public StoreNullity<Byte> nullity() {
			return newNullity(nullSettable, nullValue);
		}

	}

	final static class FloatStore extends PrimitiveStore<Float> {

		private final float[] values;
		private final float nullValue;

		FloatStore(int size, StoreNullity<Float> nullity) {
			super(nullity.nullSettable());
			checkSize(size);
			this.values = new float[size];
			this.nullValue = nullSettable ? nullity.nullValue() : 0.0f;
			if (nullValue != (float) 0) Arrays.fill(values, nullValue);
		}

		FloatStore(float[] values, StoreNullity<Float> nullity) {
			super(nullity.nullSettable());
			this.values = values;
			this.nullValue = nullSettable ? nullity.nullValue() : 0.0f;
		}

		private FloatStore(float[] values, float nullValue, boolean nullSettable) {
			super(nullSettable);
			this.values = values;
			this.nullValue = nullValue;
		}

		private FloatStore(float[] values, float nullValue, boolean mutable, boolean nullSettable) {
			super(mutable, nullSettable);
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
			return new FloatStore(copy ? values.clone() : values, nullValue, mutable, nullSettable);
		}

		@Override
		protected FloatStore resize(int newSize) {
			int oldSize = values.length;
			boolean growing = newSize > oldSize;
			if (growing && !nullSettable) failGrowCopy();
			float[] newValues = Arrays.copyOf(values, newSize);
			if (growing && nullValue != 0.0f) Arrays.fill(newValues, oldSize, newSize, nullValue);
			return new FloatStore(newValues, nullValue, nullSettable);
		}

		@Override
		public StoreNullity<Float> nullity() {
			return newNullity(nullSettable, nullValue);
		}

	}

	final static class CharacterStore extends PrimitiveStore<Character> {

		private final char[] values;
		private final char nullValue;

		CharacterStore(int size, StoreNullity<Character> nullity) {
			super(nullity.nullSettable());
			checkSize(size);
			this.values = new char[size];
			this.nullValue = nullSettable ? nullity.nullValue() : '\0';
			if (nullValue != (char) 0) Arrays.fill(values, nullValue);
		}

		CharacterStore(char[] values, StoreNullity<Character> nullity) {
			super(nullity.nullSettable());
			this.values = values;
			this.nullValue = nullSettable ? nullity.nullValue() : '\0';
		}

		private CharacterStore(char[] values, char nullValue, boolean nullSettable) {
			super(nullSettable);
			this.values = values;
			this.nullValue = nullValue;
		}

		private CharacterStore(char[] values, char nullValue, boolean mutable, boolean nullSettable) {
			super(mutable, nullSettable);
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
			return new CharacterStore(copy ? values.clone() : values, nullValue, mutable, nullSettable);
		}

		@Override
		protected CharacterStore resize(int newSize) {
			int oldSize = values.length;
			boolean growing = newSize > oldSize;
			if (growing && !nullSettable) failGrowCopy();
			char[] newValues = Arrays.copyOf(values, newSize);
			if (growing && nullValue != '\0') Arrays.fill(newValues, oldSize, newSize, nullValue);
			return new CharacterStore(newValues, nullValue, nullSettable);
		}

		@Override
		public StoreNullity<Character> nullity() {
			return newNullity(nullSettable, nullValue);
		}

	}

	final static class ShortStore extends PrimitiveStore<Short> {

		private final short[] values;
		private final short nullValue;

		ShortStore(int size, StoreNullity<Short> nullity) {
			super(nullity.nullSettable());
			checkSize(size);
			this.values = new short[size];
			this.nullValue = nullSettable ? nullity.nullValue() : (short) 0;
			if (nullValue != (short) 0) Arrays.fill(values, nullValue);
		}

		ShortStore(short[] values, StoreNullity<Short> nullity) {
			super(nullity.nullSettable());
			this.values = values;
			this.nullValue = nullSettable ? nullity.nullValue() : (short) 0;
		}

		private ShortStore(short[] values, short nullValue, boolean nullSettable) {
			super(nullSettable);
			this.values = values;
			this.nullValue = nullValue;
		}

		private ShortStore(short[] values, short nullValue, boolean mutable, boolean nullSettable) {
			super(mutable, nullSettable);
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
			return new ShortStore(copy ? values.clone() : values, nullValue, mutable, nullSettable);
		}

		@Override
		protected ShortStore resize(int newSize) {
			int oldSize = values.length;
			boolean growing = newSize > oldSize;
			if (growing && !nullSettable) failGrowCopy();
			short[] newValues = Arrays.copyOf(values, newSize);
			if (growing && nullValue != (short) 0) Arrays.fill(newValues, oldSize, newSize, nullValue);
			return new ShortStore(newValues, nullValue, nullSettable);
		}

		@Override
		public StoreNullity<Short> nullity() {
			return newNullity(nullSettable, nullValue);
		}

	}

	final static class LongStore extends PrimitiveStore<Long> {

		private final long[] values;
		private final long nullValue;

		LongStore(int size, StoreNullity<Long> nullity) {
			super(nullity.nullSettable());
			checkSize(size);
			this.values = new long[size];
			this.nullValue = nullSettable ? nullity.nullValue() : 0L;
			if (nullValue != (long) 0) Arrays.fill(values, nullValue);
		}

		LongStore(long[] values, StoreNullity<Long> nullity) {
			super(nullity.nullSettable());
			this.values = values;
			this.nullValue = nullSettable ? nullity.nullValue() : 0L;
		}

		private LongStore(long[] values, long nullValue, boolean nullSettable) {
			super(nullSettable);
			this.values = values;
			this.nullValue = nullValue;
		}

		private LongStore(long[] values, long nullValue, boolean mutable, boolean nullSettable) {
			super(mutable, nullSettable);
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
			return new LongStore(copy ? values.clone() : values, nullValue, mutable, nullSettable);
		}

		@Override
		protected LongStore resize(int newSize) {
			int oldSize = values.length;
			boolean growing = newSize > oldSize;
			if (growing && !nullSettable) failGrowCopy();
			long[] newValues = Arrays.copyOf(values, newSize);
			if (growing && nullValue != 0L) Arrays.fill(newValues, oldSize, newSize, nullValue);
			return new LongStore(newValues, nullValue, nullSettable);
		}

		@Override
		public StoreNullity<Long> nullity() {
			return newNullity(nullSettable, nullValue);
		}

	}

	final static class IntegerStore extends PrimitiveStore<Integer> {

		private final int[] values;
		private final int nullValue;

		IntegerStore(int size, StoreNullity<Integer> nullity) {
			super(nullity.nullSettable());
			checkSize(size);
			this.values = new int[size];
			this.nullValue = nullSettable ? nullity.nullValue() : 0;
			if (nullValue != (int) 0) Arrays.fill(values, nullValue);
		}

		IntegerStore(int[] values, StoreNullity<Integer> nullity) {
			super(nullity.nullSettable());
			this.values = values;
			this.nullValue = nullSettable ? nullity.nullValue() : 0;
		}

		private IntegerStore(int[] values, int nullValue, boolean nullSettable) {
			super(nullSettable);
			this.values = values;
			this.nullValue = nullValue;
		}

		private IntegerStore(int[] values, int nullValue, boolean mutable, boolean nullSettable) {
			super(mutable, nullSettable);
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
			return new IntegerStore(copy ? values.clone() : values, nullValue, mutable, nullSettable);
		}

		@Override
		protected IntegerStore resize(int newSize) {
			int oldSize = values.length;
			boolean growing = newSize > oldSize;
			if (growing && !nullSettable) failGrowCopy();
			int[] newValues = Arrays.copyOf(values, newSize);
			if (growing && nullValue != 0) Arrays.fill(newValues, oldSize, newSize, nullValue);
			return new IntegerStore(newValues, nullValue, nullSettable);
		}

		@Override
		public StoreNullity<Integer> nullity() {
			return newNullity(nullSettable, nullValue);
		}

	}

	final static class DoubleStore extends PrimitiveStore<Double> {

		private final double[] values;
		private final double nullValue;

		DoubleStore(int size, StoreNullity<Double> nullity) {
			super(nullity.nullSettable());
			checkSize(size);
			this.values = new double[size];
			this.nullValue = nullSettable ? nullity.nullValue() : 0.0;
			if (nullValue != (double) 0) Arrays.fill(values, nullValue);
		}

		DoubleStore(double[] values, StoreNullity<Double> nullity) {
			super(nullity.nullSettable());
			this.values = values;
			this.nullValue = nullSettable ? nullity.nullValue() : 0.0;
		}

		private DoubleStore(double[] values, double nullValue, boolean nullSettable) {
			super(nullSettable);
			this.values = values;
			this.nullValue = nullValue;
		}

		private DoubleStore(double[] values, double nullValue, boolean mutable, boolean nullSettable) {
			super(mutable, nullSettable);
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
			return new DoubleStore(copy ? values.clone() : values, nullValue, mutable, nullSettable);
		}

		@Override
		protected DoubleStore resize(int newSize) {
			int oldSize = values.length;
			boolean growing = newSize > oldSize;
			if (growing && !nullSettable) failGrowCopy();
			double[] newValues = Arrays.copyOf(values, newSize);
			if (growing && nullValue != 0L) Arrays.fill(newValues, oldSize, newSize, nullValue);
			return new DoubleStore(newValues, nullValue, nullSettable);
		}

		@Override
		public StoreNullity<Double> nullity() {
			return newNullity(nullSettable, nullValue);
		}

	}

	final static class BooleanStore extends PrimitiveStore<Boolean> {

		private final boolean[] values;
		private final boolean nullValue;

		BooleanStore(int size, StoreNullity<Boolean> nullity) {
			super(nullity.nullSettable());
			checkSize(size);
			this.values = new boolean[size];
			this.nullValue = nullSettable && nullity.nullValue();
			if (nullValue) Arrays.fill(values, nullValue);
		}

		BooleanStore(boolean[] values, StoreNullity<Boolean> nullity) {
			super(nullity.nullSettable());
			this.values = values;
			this.nullValue = nullSettable && nullity.nullValue();
		}

		private BooleanStore(boolean[] values, boolean nullValue, boolean nullSettable) {
			super(nullSettable);
			this.values = values;
			this.nullValue = nullValue;
		}

		private BooleanStore(boolean[] values, boolean nullValue, boolean mutable, boolean nullSettable) {
			super(mutable, nullSettable);
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
			return new BooleanStore(copy ? values.clone() : values, nullValue, mutable, nullSettable);
		}

		@Override
		protected BooleanStore resize(int newSize) {
			int oldSize = values.length;
			boolean growing = newSize > oldSize;
			if (growing && !nullSettable) failGrowCopy();
			boolean[] newValues = Arrays.copyOf(values, newSize);
			if (growing && nullValue) Arrays.fill(newValues, oldSize, newSize, nullValue);
			return new BooleanStore(newValues, nullValue, nullSettable);
		}

		@Override
		public StoreNullity<Boolean> nullity() {
			return newNullity(nullSettable, nullValue);
		}

	}

}
