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

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;

abstract class NullPrimitiveStore<V> extends AbstractStore<V> {

	private static final Storage<Byte> byteStorage = new Storage<Byte>() {
		@Override public StoreType<Byte> type() { return StoreType.BYTE; }
		@Override public NullPrimitiveStore<Byte> newStore(int size, Byte initialValue) { return new ByteStore(size, initialValue); }
	};

	private static final Storage<Float> floatStorage = new Storage<Float>() {
		@Override public StoreType<Float> type() { return StoreType.FLOAT; }
		@Override public NullPrimitiveStore<Float> newStore(int size, Float initialValue) { return new FloatStore(size, initialValue); }
	};

	private static final Storage<Character> charStorage = new Storage<Character>() {
		@Override public StoreType<Character> type() { return StoreType.CHAR; }
		@Override public NullPrimitiveStore<Character> newStore(int size, Character initialValue) { return new CharacterStore(size, initialValue); }
	};

	private static final Storage<Short> shortStorage = new Storage<Short>() {
		@Override public StoreType<Short> type() { return StoreType.SHORT; }
		@Override public NullPrimitiveStore<Short> newStore(int size, Short initialValue) { return new ShortStore(size, initialValue); }
	};

	private static final Storage<Long> longStorage = new Storage<Long>() {
		@Override public StoreType<Long> type() { return StoreType.LONG; }
		@Override public NullPrimitiveStore<Long> newStore(int size, Long initialValue) { return new LongStore(size, initialValue); }
	};

	private static final Storage<Integer> intStorage = new Storage<Integer>() {
		@Override public StoreType<Integer> type() { return StoreType.INT; }
		@Override public NullPrimitiveStore<Integer> newStore(int size, Integer initialValue) { return new IntegerStore(size, initialValue); }
	};

	private static final Storage<Double> doubleStorage = new Storage<Double>() {
		@Override public StoreType<Double> type() { return StoreType.DOUBLE; }
		@Override public NullPrimitiveStore<Double> newStore(int size, Double initialValue) { return new DoubleStore(size, initialValue); }
	};

	private static final Storage<Boolean> booleanStorage = new Storage<Boolean>() {
		@Override public StoreType<Boolean> type() { return StoreType.BOOLEAN; }
		@Override public NullPrimitiveStore<Boolean> newStore(int size, Boolean initialValue) { return new BooleanStore(size, initialValue); }
	};

	@SuppressWarnings("unchecked")
	static <V> Storage<V> newStorage(StoreType<V> type) {
		switch(Stores.hash(type.valueType)) {
		case Stores.BYTE:    return (Storage<V>) byteStorage;
		case Stores.FLOAT:   return (Storage<V>) floatStorage;
		case Stores.CHAR:    return (Storage<V>) charStorage;
		case Stores.SHORT:   return (Storage<V>) shortStorage;
		case Stores.LONG:    return (Storage<V>) longStorage;
		case Stores.INT:     return (Storage<V>) intStorage;
		case Stores.DOUBLE:  return (Storage<V>) doubleStorage;
		case Stores.BOOLEAN: return (Storage<V>) booleanStorage;
		default: throw new IllegalArgumentException(type.valueType.getName());
		}
	}

	@SuppressWarnings("unchecked")
	static <V> NullPrimitiveStore<V> newStore(Class<V> type, int size, V value) {
		switch(Stores.hash(type)) {
		case Stores.BYTE:    return (NullPrimitiveStore<V>) new ByteStore     (size, (Byte)      value);
		case Stores.FLOAT:   return (NullPrimitiveStore<V>) new FloatStore    (size, (Float)     value);
		case Stores.CHAR:    return (NullPrimitiveStore<V>) new CharacterStore(size, (Character) value);
		case Stores.SHORT:   return (NullPrimitiveStore<V>) new ShortStore    (size, (Short)     value);
		case Stores.LONG:    return (NullPrimitiveStore<V>) new LongStore     (size, (Long)      value);
		case Stores.INT:     return (NullPrimitiveStore<V>) new IntegerStore  (size, (Integer)   value);
		case Stores.DOUBLE:  return (NullPrimitiveStore<V>) new DoubleStore   (size, (Double)    value);
		case Stores.BOOLEAN: return (NullPrimitiveStore<V>) new BooleanStore  (size, (Boolean)   value);
		default: throw new IllegalArgumentException(type.getName());
		}
	}

	@SuppressWarnings("unchecked")
	static <V> NullPrimitiveStore<V> newStore(Class<V> type, Object array) {
		switch (Stores.hash(type)) {
		case Stores.BYTE:    return (NullPrimitiveStore<V>) new ByteStore     ((byte   []) array);
		case Stores.FLOAT:   return (NullPrimitiveStore<V>) new FloatStore    ((float  []) array);
		case Stores.CHAR:    return (NullPrimitiveStore<V>) new CharacterStore((char   []) array);
		case Stores.SHORT:   return (NullPrimitiveStore<V>) new ShortStore    ((short  []) array);
		case Stores.LONG:    return (NullPrimitiveStore<V>) new LongStore     ((long   []) array);
		case Stores.INT:     return (NullPrimitiveStore<V>) new IntegerStore  ((int    []) array);
		case Stores.DOUBLE:  return (NullPrimitiveStore<V>) new DoubleStore   ((double []) array);
		case Stores.BOOLEAN: return (NullPrimitiveStore<V>) new BooleanStore  ((boolean[]) array);
		default: throw new IllegalArgumentException(type.getName());
		}
	}

	@SuppressWarnings("unchecked")
	static <V> NullPrimitiveStore<V> newStore(Store<V> store, int newSize) {
		StoreType<V> type = store.type();
		Object array = Stores.toPrimitiveArray(store, newSize, type.settingNullToDefault().nullValue);
		BitStore populated = Bits.resizedCopyOf(store.population(), newSize, false);
		int count = store.count();

		switch (Stores.hash(type.valueType)) {
		case Stores.BYTE:    return (NullPrimitiveStore<V>) new ByteStore     (populated, count, (byte   []) array);
		case Stores.FLOAT:   return (NullPrimitiveStore<V>) new FloatStore    (populated, count, (float  []) array);
		case Stores.CHAR:    return (NullPrimitiveStore<V>) new CharacterStore(populated, count, (char   []) array);
		case Stores.SHORT:   return (NullPrimitiveStore<V>) new ShortStore    (populated, count, (short  []) array);
		case Stores.LONG:    return (NullPrimitiveStore<V>) new LongStore     (populated, count, (long   []) array);
		case Stores.INT:     return (NullPrimitiveStore<V>) new IntegerStore  (populated, count, (int    []) array);
		case Stores.DOUBLE:  return (NullPrimitiveStore<V>) new DoubleStore   (populated, count, (double []) array);
		case Stores.BOOLEAN: return (NullPrimitiveStore<V>) new BooleanStore  (populated, count, (boolean[]) array);
		default: throw new IllegalArgumentException("invalid store type: " + type);
		}
	}

	int count;
	BitStore populated;

	protected NullPrimitiveStore(BitStore populated) {
		this.populated = populated.mutable();
		this.count = populated.ones().count();
	}

	protected NullPrimitiveStore(BitStore populated, int count) {
		this.populated = populated.mutable();
		this.count = count;
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
		populated.clear();
		count = 0;
	}

	@Override
	public void fill(V value) {
		if (value == null) { // simple case
			clear();
		} else { // more complex case
			if (!populated.isMutable()) throw immutableException();
			// do this first in case filling fails due to class error
			fillImpl(value);
			populated.fill();
			count = populated.size();
		}
	}

	@Override
	public V get(int index) {
		return populated.getBit(index) ? getImpl(index) : null;
	}

	@Override
	public boolean isNull(int index) {
		return !populated.getBit(index);
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
	@SuppressWarnings("unchecked")
	public <W extends V> void setStore(int position, Store<W> store) {
		int size = checkSetStore(position, store);
		if (store.getClass() == this.getClass()) {
			setStoreImpl(position, (NullPrimitiveStore<V>)store, size);
		} else {
			setStoreImpl(position, store, size);
		}
	}

	@Override
	public BitStore population() {
		return populated.immutableView();
	}

	// for extension

	abstract protected V getImpl(int index);

	abstract protected void setImpl(int index, V value);

	abstract protected void fillImpl(V value);

	abstract protected Store<V> duplicate(BitStore populated, boolean copy);

	abstract protected void setStoreImpl(int position, NullPrimitiveStore<V> store, int size);

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

		ByteStore(int size, Byte initialValue) {
			super(Bits.bits(initialValue != null, size));
			this.values = new byte[size];
			if (count > 0) {
				Arrays.fill(values, initialValue);
			}
		}

		ByteStore(byte[] values) {
			super(Bits.oneBits(values.length));
			this.values = values;
		}

		ByteStore(BitStore populated, int count, byte[] values) {
			super(populated, count);
			this.values = values;
		}

		@Override
		public StoreType<Byte> type() {
			return StoreType.BYTE;
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

		@Override
		protected void setStoreImpl(int position, NullPrimitiveStore<Byte> store, int size) {
			ByteStore that = (ByteStore) store;
			this.populated.setStore(position, that.populated);
			System.arraycopy(that.values, 0, this.values, position, size);
		}
	}

	final static class FloatStore extends NullPrimitiveStore<Float> {

		private float[] values;

		FloatStore(int size, Float initialValue) {
			super(Bits.bits(initialValue != null, size));
			this.values = new float[size];
			if (count > 0) {
				Arrays.fill(values, initialValue);
			}
		}

		FloatStore(float[] values) {
			super(Bits.oneBits(values.length));
			this.values = values;
		}

		FloatStore(BitStore populated, int count, float[] values) {
			super(populated, count);
			this.values = values;
		}

		@Override
		public StoreType<Float> type() {
			return StoreType.FLOAT;
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

		@Override
		protected void setStoreImpl(int position, NullPrimitiveStore<Float> store, int size) {
			FloatStore that = (FloatStore) store;
			this.populated.setStore(position, that.populated);
			System.arraycopy(that.values, 0, this.values, position, size);
		}

	}

	final static class CharacterStore extends NullPrimitiveStore<Character> {

		private char[] values;

		CharacterStore(int size, Character initialValue) {
			super(Bits.bits(initialValue != null, size));
			this.values = new char[size];
			if (count > 0) {
				Arrays.fill(values, initialValue);
			}
		}

		CharacterStore(char[] values) {
			super(Bits.oneBits(values.length));
			this.values = values;
		}

		CharacterStore(BitStore populated, int count, char[] values) {
			super(populated, count);
			this.values = values;
		}

		@Override
		public StoreType<Character> type() {
			return StoreType.CHAR;
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

		@Override
		protected void setStoreImpl(int position, NullPrimitiveStore<Character> store, int size) {
			CharacterStore that = (CharacterStore) store;
			this.populated.setStore(position, that.populated);
			System.arraycopy(that.values, 0, this.values, position, size);
		}

	}

	final static class ShortStore extends NullPrimitiveStore<Short> {

		private short[] values;

		ShortStore(int size, Short initialValue) {
			super(Bits.bits(initialValue != null, size));
			this.values = new short[size];
			if (count > 0) {
				Arrays.fill(values, initialValue);
			}
		}

		ShortStore(short[] values) {
			super(Bits.oneBits(values.length));
			this.values = values;
		}

		ShortStore(BitStore populated, int count, short[] values) {
			super(populated, count);
			this.values = values;
		}

		@Override
		public StoreType<Short> type() {
			return StoreType.SHORT;
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

		@Override
		protected void setStoreImpl(int position, NullPrimitiveStore<Short> store, int size) {
			ShortStore that = (ShortStore) store;
			this.populated.setStore(position, that.populated);
			System.arraycopy(that.values, 0, this.values, position, size);
		}

	}

	final static class LongStore extends NullPrimitiveStore<Long> {

		private long[] values;

		LongStore(int size, Long initialValue) {
			super(Bits.bits(initialValue != null, size));
			this.values = new long[size];
			if (count > 0) {
				Arrays.fill(values, initialValue);
			}
		}

		LongStore(long[] values) {
			super(Bits.oneBits(values.length));
			this.values = values;
		}

		LongStore(BitStore populated, int count, long[] values) {
			super(populated, count);
			this.values = values;
		}

		@Override
		public StoreType<Long> type() {
			return StoreType.LONG;
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

		@Override
		protected void setStoreImpl(int position, NullPrimitiveStore<Long> store, int size) {
			LongStore that = (LongStore) store;
			this.populated.setStore(position, that.populated);
			System.arraycopy(that.values, 0, this.values, position, size);
		}

	}

	final static class IntegerStore extends NullPrimitiveStore<Integer> {

		private int[] values;

		IntegerStore(int size, Integer initialValue) {
			super(Bits.bits(initialValue != null, size));
			this.values = new int[size];
			if (count > 0) {
				Arrays.fill(values, initialValue);
			}
		}

		IntegerStore(int[] values) {
			super(Bits.oneBits(values.length));
			this.values = values;
		}

		IntegerStore(BitStore populated, int count, int[] values) {
			super(populated, count);
			this.values = values;
		}

		@Override
		public StoreType<Integer> type() {
			return StoreType.INT;
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

		@Override
		protected void setStoreImpl(int position, NullPrimitiveStore<Integer> store, int size) {
			IntegerStore that = (IntegerStore) store;
			this.populated.setStore(position, that.populated);
			System.arraycopy(that.values, 0, this.values, position, size);
		}

	}

	final static class DoubleStore extends NullPrimitiveStore<Double> {

		private double[] values;

		DoubleStore(int size, Double initialValue) {
			super(Bits.bits(initialValue != null, size));
			this.values = new double[size];
			if (count > 0) {
				Arrays.fill(values, initialValue);
			}
		}

		DoubleStore(double[] values) {
			super(Bits.oneBits(values.length));
			this.values = values;
		}

		DoubleStore(BitStore populated, int count, double[] values) {
			super(populated, count);
			this.values = values;
		}

		@Override
		public StoreType<Double> type() {
			return StoreType.DOUBLE;
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

		@Override
		protected void setStoreImpl(int position, NullPrimitiveStore<Double> store, int size) {
			DoubleStore that = (DoubleStore) store;
			this.populated.setStore(position, that.populated);
			System.arraycopy(that.values, 0, this.values, position, size);
		}

	}

	final static class BooleanStore extends NullPrimitiveStore<Boolean> {

		private boolean[] values;

		BooleanStore(int size, Boolean initialValue) {
			super(Bits.bits(initialValue != null, size));
			this.values = new boolean[size];
			if (count > 0) {
				Arrays.fill(values, initialValue);
			}
		}

		BooleanStore(boolean[] values) {
			super(Bits.oneBits(values.length));
			this.values = values;
		}

		BooleanStore(BitStore populated, int count, boolean[] values) {
			super(populated, count);
			this.values = values;
		}

		@Override
		public StoreType<Boolean> type() {
			return StoreType.BOOLEAN;
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

		@Override
		protected void setStoreImpl(int position, NullPrimitiveStore<Boolean> store, int size) {
			BooleanStore that = (BooleanStore) store;
			this.populated.setStore(position, that.populated);
			System.arraycopy(that.values, 0, this.values, position, size);
		}

	}

}
