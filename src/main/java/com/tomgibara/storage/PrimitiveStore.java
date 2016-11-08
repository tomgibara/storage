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
import java.util.Spliterator;
import java.util.Spliterators;

abstract class PrimitiveStore<V> extends AbstractStore<V> {

	private static void failGrowCopy() {
		throw new IllegalArgumentException("cannot increase size, null not settable");
	}

	private static final <T> StoreType<T> newType(Class<T> clss, boolean nullSettable, T nullValue) {
		//TODO can we avoid intermediate object creation?
		StoreType<T> type = StoreType.of(clss);
		return nullSettable ? type.settingNullToValue(nullValue) : type.settingNullDisallowed();
	}

	private static abstract class PrimitiveStorage<P> implements Storage<P> {
		final StoreType<P> type;
		PrimitiveStorage(StoreType<P> type) { this.type = type; }
		@Override final public StoreType<P> type() { return type; }
	}

	private static final Storage<Byte> byteStorage(StoreType<Byte> type) {
		return new PrimitiveStorage<Byte>(type) {
			@Override public PrimitiveStore<Byte> newStore(int size, Byte initialValue) { return new ByteStore(size, type, initialValue); }
		};
	};

	private static final Storage<Float> floatStorage(StoreType<Float> type) {
		return new PrimitiveStorage<Float>(type) {
			@Override public PrimitiveStore<Float> newStore(int size, Float initialValue) { return new FloatStore(size, type, initialValue); }
		};
	};

	private static final Storage<Character> charStorage(StoreType<Character> type) {
		return new PrimitiveStorage<Character>(type) {
			@Override public PrimitiveStore<Character> newStore(int size, Character initialValue) { return new CharacterStore(size, type, initialValue); }
		};
	};

	private static final Storage<Short> shortStorage(StoreType<Short> type) {
		return new PrimitiveStorage<Short>(type) {
			@Override public PrimitiveStore<Short> newStore(int size, Short initialValue) { return new ShortStore(size, type, initialValue); }
		};
	};

	private static final Storage<Long> longStorage(StoreType<Long> type) {
		return new PrimitiveStorage<Long>(type) {
			@Override public PrimitiveStore<Long> newStore(int size, Long initialValue) { return new LongStore(size, type, initialValue); }
		};
	};

	private static final Storage<Integer> intStorage(StoreType<Integer> type) {
		return new PrimitiveStorage<Integer>(type) {
			@Override public PrimitiveStore<Integer> newStore(int size, Integer initialValue) { return new IntegerStore(size, type, initialValue); }
		};
	};

	private static final Storage<Double> doubleStorage(StoreType<Double> type) {
		return new PrimitiveStorage<Double>(type) {
			@Override public PrimitiveStore<Double> newStore(int size, Double initialValue) { return new DoubleStore(size, type, initialValue); }
		};
	};

	private static final Storage<Boolean> booleanStorage(StoreType<Boolean> type) {
		return new PrimitiveStorage<Boolean>(type) {
			@Override public PrimitiveStore<Boolean> newStore(int size, Boolean initialValue) { return new BooleanStore(size, type, initialValue); }
		};
	};

	@SuppressWarnings("unchecked")
	static <V> Storage<V> newStorage(StoreType<V> type) {
		switch((type.valueType.getName().hashCode() >> 8) & 0xf) {
		case Stores.BYTE:    return (Storage<V>) byteStorage   ((StoreType<Byte>     ) type);
		case Stores.FLOAT:   return (Storage<V>) floatStorage  ((StoreType<Float>    ) type);
		case Stores.CHAR:    return (Storage<V>) charStorage   ((StoreType<Character>) type);
		case Stores.SHORT:   return (Storage<V>) shortStorage  ((StoreType<Short>    ) type);
		case Stores.LONG:    return (Storage<V>) longStorage   ((StoreType<Long>     ) type);
		case Stores.INT:     return (Storage<V>) intStorage    ((StoreType<Integer>  ) type);
		case Stores.DOUBLE:  return (Storage<V>) doubleStorage ((StoreType<Double>   ) type);
		case Stores.BOOLEAN: return (Storage<V>) booleanStorage((StoreType<Boolean>  ) type);
		default: throw new IllegalArgumentException(type.valueType.getName());
		}
	}

	@SuppressWarnings("unchecked")
	static <V> PrimitiveStore<V> newStore(StoreType<V> type, int size, V value) {
		switch((type.valueType.getName().hashCode() >> 8) & 0xf) {
		case Stores.BYTE:    return (PrimitiveStore<V>) new ByteStore     (size, (StoreType<Byte>     ) type, (Byte)      value);
		case Stores.FLOAT:   return (PrimitiveStore<V>) new FloatStore    (size, (StoreType<Float>    ) type, (Float)     value);
		case Stores.CHAR:    return (PrimitiveStore<V>) new CharacterStore(size, (StoreType<Character>) type, (Character) value);
		case Stores.SHORT:   return (PrimitiveStore<V>) new ShortStore    (size, (StoreType<Short>    ) type, (Short)     value);
		case Stores.LONG:    return (PrimitiveStore<V>) new LongStore     (size, (StoreType<Long>     ) type, (Long)      value);
		case Stores.INT:     return (PrimitiveStore<V>) new IntegerStore  (size, (StoreType<Integer>  ) type, (Integer)   value);
		case Stores.DOUBLE:  return (PrimitiveStore<V>) new DoubleStore   (size, (StoreType<Double>   ) type, (Double)    value);
		case Stores.BOOLEAN: return (PrimitiveStore<V>) new BooleanStore  (size, (StoreType<Boolean>  ) type, (Boolean)   value);
		default: throw new IllegalArgumentException(type.valueType.getName());
		}
	}

	@SuppressWarnings("unchecked")
	static <V> PrimitiveStore<V> newStore(StoreType<V> type, Object array) {
		switch((type.valueType.getName().hashCode() >> 8) & 0xf) {
		case Stores.BYTE:    return (PrimitiveStore<V>) new ByteStore     ((byte   []) array, (StoreType<Byte>     ) type);
		case Stores.FLOAT:   return (PrimitiveStore<V>) new FloatStore    ((float  []) array, (StoreType<Float>    ) type);
		case Stores.CHAR:    return (PrimitiveStore<V>) new CharacterStore((char   []) array, (StoreType<Character>) type);
		case Stores.SHORT:   return (PrimitiveStore<V>) new ShortStore    ((short  []) array, (StoreType<Short>    ) type);
		case Stores.LONG:    return (PrimitiveStore<V>) new LongStore     ((long   []) array, (StoreType<Long>     ) type);
		case Stores.INT:     return (PrimitiveStore<V>) new IntegerStore  ((int    []) array, (StoreType<Integer>  ) type);
		case Stores.DOUBLE:  return (PrimitiveStore<V>) new DoubleStore   ((double []) array, (StoreType<Double>   ) type);
		case Stores.BOOLEAN: return (PrimitiveStore<V>) new BooleanStore  ((boolean[]) array, (StoreType<Boolean>  ) type);
		default: throw new IllegalArgumentException(type.valueType.getName());
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
	public boolean isSettable(Object value) {
		if (value == null) return nullSettable;
		Class<?> clss = value.getClass();
		return clss == primitiveType() || clss == wrapperType();
	}

	@Override
	public V set(int index, V value) {
		if (value == null && !nullSettable) StoreType.failNull();
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
	@SuppressWarnings("unchecked")
	public <W extends V> void setStore(int position, Store<W> store) {
		int size = checkSetStore(position, store);
		if (store.getClass() == this.getClass()) {
			System.arraycopy(((PrimitiveStore<V>) store).values(), 0, values(), position, size);
		} else {
			setStoreImpl(position, store, size);
		}
	}

	// for extension

	abstract protected Class<?> primitiveType();

	abstract protected Class<?> wrapperType();

	abstract protected Object values();

	abstract protected V getImpl(int index);

	abstract protected void setImpl(int index, V value);

	abstract protected void fillImpl(V value);

	abstract protected PrimitiveStore<V> duplicate(boolean copy, boolean mutable);

	abstract protected PrimitiveStore<V> resize(int newSize);

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

		ByteStore(int size, StoreType<Byte> type, Byte initialValue) {
			super(type.nullSettable);
			if (initialValue == null) checkSize(size);
			values = new byte[size];
			nullValue = nullSettable ? type.nullValue : (byte) 0;
			if (initialValue == null) initialValue = nullValue;
			if (nullValue != (byte) 0) Arrays.fill(values, initialValue);
		}

		ByteStore(byte[] values, StoreType<Byte> type) {
			super(type.nullSettable);
			this.values = values;
			this.nullValue = nullSettable ? type.nullValue : (byte) 0;
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
		public int size() {
			return values.length;
		}

		@Override
		protected Class<?> primitiveType() {
			return byte.class;
		}

		@Override
		protected Class<?> wrapperType() {
			return Byte.class;
		}

		@Override
		protected Object values() {
			return values;
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
		public StoreType<Byte> type() {
			return newType(byte.class, nullSettable, nullValue);
		}

	}

	final static class FloatStore extends PrimitiveStore<Float> {

		private final float[] values;
		private final float nullValue;

		FloatStore(int size, StoreType<Float> type, Float initialValue) {
			super(type.nullSettable);
			if (initialValue == null) checkSize(size);
			values = new float[size];
			nullValue = nullSettable ? type.nullValue : 0.0f;
			if (initialValue == null) initialValue = nullValue;
			if (nullValue != (float) 0) Arrays.fill(values, nullValue);
		}

		FloatStore(float[] values, StoreType<Float> type) {
			super(type.nullSettable);
			this.values = values;
			this.nullValue = nullSettable ? type.nullValue : 0.0f;
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
		public int size() {
			return values.length;
		}

		@Override
		protected Class<?> primitiveType() {
			return float.class;
		}

		@Override
		protected Class<?> wrapperType() {
			return Float.class;
		}

		@Override
		protected Object values() {
			return values;
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
		public StoreType<Float> type() {
			return newType(float.class, nullSettable, nullValue);
		}

	}

	final static class CharacterStore extends PrimitiveStore<Character> {

		private final char[] values;
		private final char nullValue;

		CharacterStore(int size, StoreType<Character> type, Character initialValue) {
			super(type.nullSettable);
			if (initialValue == null) checkSize(size);
			values = new char[size];
			nullValue = nullSettable ? type.nullValue : '\0';
			if (initialValue == null) initialValue = nullValue;
			if (nullValue != (char) 0) Arrays.fill(values, nullValue);
		}

		CharacterStore(char[] values, StoreType<Character> type) {
			super(type.nullSettable);
			this.values = values;
			this.nullValue = nullSettable ? type.nullValue : '\0';
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
		public int size() {
			return values.length;
		}

		@Override
		protected Class<?> primitiveType() {
			return char.class;
		}

		@Override
		protected Class<?> wrapperType() {
			return Character.class;
		}

		@Override
		protected Object values() {
			return values;
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
		public StoreType<Character> type() {
			return newType(char.class, nullSettable, nullValue);
		}

	}

	final static class ShortStore extends PrimitiveStore<Short> {

		private final short[] values;
		private final short nullValue;

		ShortStore(int size, StoreType<Short> type, Short initialValue) {
			super(type.nullSettable);
			if (initialValue == null) checkSize(size);
			values = new short[size];
			nullValue = nullSettable ? type.nullValue : (short) 0;
			if (initialValue == null) initialValue = nullValue;
			if (nullValue != (short) 0) Arrays.fill(values, nullValue);
		}

		ShortStore(short[] values, StoreType<Short> type) {
			super(type.nullSettable);
			this.values = values;
			this.nullValue = nullSettable ? type.nullValue : (short) 0;
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
		public int size() {
			return values.length;
		}

		@Override
		protected Class<?> primitiveType() {
			return short.class;
		}

		@Override
		protected Class<?> wrapperType() {
			return Short.class;
		}

		@Override
		protected Object values() {
			return values;
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
		public StoreType<Short> type() {
			return newType(short.class, nullSettable, nullValue);
		}

	}

	final static class LongStore extends PrimitiveStore<Long> {

		private final long[] values;
		private final long nullValue;

		LongStore(int size, StoreType<Long> type, Long initialValue) {
			super(type.nullSettable);
			if (initialValue == null) checkSize(size);
			values = new long[size];
			nullValue = nullSettable ? type.nullValue : 0L;
			if (initialValue == null) initialValue = nullValue;
			if (nullValue != (long) 0) Arrays.fill(values, nullValue);
		}

		LongStore(long[] values, StoreType<Long> type) {
			super(type.nullSettable);
			this.values = values;
			this.nullValue = nullSettable ? type.nullValue : 0L;
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
		public int size() {
			return values.length;
		}

		@Override
		protected Class<?> primitiveType() {
			return long.class;
		}

		@Override
		protected Class<?> wrapperType() {
			return Long.class;
		}

		@Override
		protected Object values() {
			return values;
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
		public StoreType<Long> type() {
			return newType(long.class, nullSettable, nullValue);
		}

		@Override
		public Spliterator.OfLong spliterator() {
			return Spliterators.spliterator(values, Spliterator.ORDERED | Spliterator.NONNULL);
		}
	}

	final static class IntegerStore extends PrimitiveStore<Integer> {

		private final int[] values;
		private final int nullValue;

		IntegerStore(int size, StoreType<Integer> type, Integer initialValue) {
			super(type.nullSettable);
			if (initialValue == null) checkSize(size);
			values = new int[size];
			nullValue = nullSettable ? type.nullValue : 0;
			if (initialValue == null) initialValue = nullValue;
			if (nullValue != (int) 0) Arrays.fill(values, nullValue);
		}

		IntegerStore(int[] values, StoreType<Integer> type) {
			super(type.nullSettable);
			this.values = values;
			this.nullValue = nullSettable ? type.nullValue : 0;
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
		public int size() {
			return values.length;
		}

		@Override
		protected Class<?> primitiveType() {
			return int.class;
		}

		@Override
		protected Class<?> wrapperType() {
			return Integer.class;
		}

		@Override
		protected Object values() {
			return values;
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
		public StoreType<Integer> type() {
			return newType(int.class, nullSettable, nullValue);
		}

		@Override
		public Spliterator.OfInt spliterator() {
			return Spliterators.spliterator(values, Spliterator.ORDERED | Spliterator.NONNULL);
		}
	}

	final static class DoubleStore extends PrimitiveStore<Double> {

		private final double[] values;
		private final double nullValue;

		DoubleStore(int size, StoreType<Double> type, Double initialValue) {
			super(type.nullSettable);
			if (initialValue == null) checkSize(size);
			values = new double[size];
			nullValue = nullSettable ? type.nullValue : 0.0;
			if (initialValue == null) initialValue = nullValue;
			if (nullValue != (double) 0) Arrays.fill(values, nullValue);
		}

		DoubleStore(double[] values, StoreType<Double> type) {
			super(type.nullSettable);
			this.values = values;
			this.nullValue = nullSettable ? type.nullValue : 0.0;
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
		public int size() {
			return values.length;
		}

		@Override
		protected Class<?> primitiveType() {
			return double.class;
		}

		@Override
		protected Class<?> wrapperType() {
			return Double.class;
		}

		@Override
		protected Object values() {
			return values;
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
		public StoreType<Double> type() {
			return newType(double.class, nullSettable, nullValue);
		}

		@Override
		public Spliterator.OfDouble spliterator() {
			return Spliterators.spliterator(values, Spliterator.ORDERED | Spliterator.NONNULL);
		}
	}

	final static class BooleanStore extends PrimitiveStore<Boolean> {

		private final boolean[] values;
		private final boolean nullValue;

		BooleanStore(int size, StoreType<Boolean> type, Boolean initialValue) {
			super(type.nullSettable);
			if (initialValue == null) checkSize(size);
			values = new boolean[size];
			nullValue = nullSettable && type.nullValue;
			if (initialValue == null) initialValue = nullValue;
			if (nullValue) Arrays.fill(values, nullValue);
		}

		BooleanStore(boolean[] values, StoreType<Boolean> type) {
			super(type.nullSettable);
			this.values = values;
			this.nullValue = nullSettable && type.nullValue;
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
		public int size() {
			return values.length;
		}

		@Override
		protected Class<?> primitiveType() {
			return boolean.class;
		}

		@Override
		protected Class<?> wrapperType() {
			return Boolean.class;
		}

		@Override
		protected Object values() {
			return values;
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
		public StoreType<Boolean> type() {
			return newType(boolean.class, nullSettable, nullValue);
		}

	}

}
