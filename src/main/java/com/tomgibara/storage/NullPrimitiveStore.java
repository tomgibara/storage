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
		@Override public Class<Byte> valueType() { return byte.class; }
		@Override public NullPrimitiveStore<Byte> newStore(int size, Byte initialValue) { return new ByteStore(size, initialValue); }
	};

	private static final Storage<Float> floatStorage = new Storage<Float>() {
		@Override public Class<Float> valueType() { return float.class; }
		@Override public NullPrimitiveStore<Float> newStore(int size, Float initialValue) { return new FloatStore(size, initialValue); }
	};

	private static final Storage<Character> charStorage = new Storage<Character>() {
		@Override public Class<Character> valueType() { return char.class; }
		@Override public NullPrimitiveStore<Character> newStore(int size, Character initialValue) { return new CharacterStore(size, initialValue); }
	};

	private static final Storage<Short> shortStorage = new Storage<Short>() {
		@Override public Class<Short> valueType() { return short.class; }
		@Override public NullPrimitiveStore<Short> newStore(int size, Short initialValue) { return new ShortStore(size, initialValue); }
	};

	private static final Storage<Long> longStorage = new Storage<Long>() {
		@Override public Class<Long> valueType() { return long.class; }
		@Override public NullPrimitiveStore<Long> newStore(int size, Long initialValue) { return new LongStore(size, initialValue); }
	};

	private static final Storage<Integer> intStorage = new Storage<Integer>() {
		@Override public Class<Integer> valueType() { return int.class; }
		@Override public NullPrimitiveStore<Integer> newStore(int size, Integer initialValue) { return new IntegerStore(size, initialValue); }
	};

	private static final Storage<Double> doubleStorage = new Storage<Double>() {
		@Override public Class<Double> valueType() { return double.class; }
		@Override public NullPrimitiveStore<Double> newStore(int size, Double initialValue) { return new DoubleStore(size, initialValue); }
	};

	private static final Storage<Boolean> booleanStorage = new Storage<Boolean>() {
		@Override public Class<Boolean> valueType() { return boolean.class; }
		@Override public NullPrimitiveStore<Boolean> newStore(int size, Boolean initialValue) { return new BooleanStore(size, initialValue); }
	};

	@SuppressWarnings("unchecked")
	static <V> Storage<V> newStorage(Class<V> type) {
		switch((type.getName().hashCode() >> 8) & 0xf) {
		case Stores.BYTE:    return (Storage<V>) byteStorage;
		case Stores.FLOAT:   return (Storage<V>) floatStorage;
		case Stores.CHAR:    return (Storage<V>) charStorage;
		case Stores.SHORT:   return (Storage<V>) shortStorage;
		case Stores.LONG:    return (Storage<V>) longStorage;
		case Stores.INT:     return (Storage<V>) intStorage;
		case Stores.DOUBLE:  return (Storage<V>) doubleStorage;
		case Stores.BOOLEAN: return (Storage<V>) booleanStorage;
		default: throw new IllegalArgumentException(type.getName());
		}
	}

	@SuppressWarnings("unchecked")
	static <V> NullPrimitiveStore<V> newStore(Class<V> type, int size, V value) {
		switch((type.getName().hashCode() >> 8) & 0xf) {
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

	int count;
	BitStore populated;

	protected NullPrimitiveStore(BitStore populated, int count) {
		this.populated = populated;
		this.count = count;
	}

	protected NullPrimitiveStore(int size, boolean filled) {
		populated = Bits.store(size);
		if (filled) {
			populated.fill();
			count = size;
		} else {
			count = 0;
		}
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
	public BitStore population() {
		return populated.immutableView();
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

		ByteStore(int size, Byte initialValue) {
			super(size, false);
			this.values = new byte[size];
			if (initialValue != null) {
				Arrays.fill(values, initialValue);
				count = size;
			}
		}

		ByteStore(byte[] values) {
			super(values.length, true);
			this.values = values;
		}

		private ByteStore(BitStore populated, int count, byte[] values) {
			super(populated, count);
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

		FloatStore(int size, Float initialValue) {
			super(size, false);
			this.values = new float[size];
			if (initialValue != null) {
				Arrays.fill(values, initialValue);
				count = size;
			}
		}

		FloatStore(float[] values) {
			super(values.length, true);
			this.values = values;
		}

		private FloatStore(BitStore populated, int count, float[] values) {
			super(populated, count);
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

		CharacterStore(int size, Character initialValue) {
			super(size, false);
			this.values = new char[size];
			if (initialValue != null) {
				Arrays.fill(values, initialValue);
				count = size;
			}
		}

		CharacterStore(char[] values) {
			super(values.length, true);
			this.values = values;
		}

		private CharacterStore(BitStore populated, int count, char[] values) {
			super(populated, count);
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

		ShortStore(int size, Short initialValue) {
			super(size, false);
			this.values = new short[size];
			if (initialValue != null) {
				Arrays.fill(values, initialValue);
				count = size;
			}
		}

		ShortStore(short[] values) {
			super(values.length, true);
			this.values = values;
		}

		private ShortStore(BitStore populated, int count, short[] values) {
			super(populated, count);
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

		LongStore(int size, Long initialValue) {
			super(size, false);
			this.values = new long[size];
			if (initialValue != null) {
				Arrays.fill(values, initialValue);
				count = size;
			}
		}

		LongStore(long[] values) {
			super(values.length, true);
			this.values = values;
		}

		private LongStore(BitStore populated, int count, long[] values) {
			super(populated, count);
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

		IntegerStore(int size, Integer initialValue) {
			super(size, false);
			this.values = new int[size];
			if (initialValue != null) {
				Arrays.fill(values, initialValue);
				count = size;
			}
		}

		IntegerStore(int[] values) {
			super(values.length, true);
			this.values = values;
		}

		private IntegerStore(BitStore populated, int count, int[] values) {
			super(populated, count);
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

		DoubleStore(int size, Double initialValue) {
			super(size, false);
			this.values = new double[size];
			if (initialValue != null) {
				Arrays.fill(values, initialValue);
				count = size;
			}
		}

		DoubleStore(double[] values) {
			super(values.length, true);
			this.values = values;
		}

		private DoubleStore(BitStore populated, int count, double[] values) {
			super(populated, count);
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

		BooleanStore(int size, Boolean initialValue) {
			super(size, false);
			this.values = new boolean[size];
			if (initialValue != null) {
				Arrays.fill(values, initialValue);
				count = size;
			}
		}

		BooleanStore(boolean[] values) {
			super(values.length, true);
			this.values = values;
		}

		private BooleanStore(BitStore populated, int count, boolean[] values) {
			super(populated, count);
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
