/*
 * Copyright 2017 Tom Gibara
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

import java.util.function.Function;

abstract class NumberAccessor<P> {

	private final Store<?> store;
	private final Function<P, Object> convert;

	NumberAccessor(Store<?> store) {
		this.store = store;
		this.convert = convert();
	}

	boolean isAccessible(int index) {
		return store.get(index) instanceof Number;
	}

	Number get(int index) {
		Object value = store.get(index);
		if (!(value instanceof Number)) throw new RuntimeException("value at index is not a number");
		return (Number) value;
	}

	Function<P, Object> convert() {
		Class<?> type = store.type().valueType;
		if (!type.isPrimitive()) {
			Class<?> primitive = Stores.primitiveClassFor(type);
			if (primitive != null) {
				// primitive wrapper, fall through
				type = primitive; 
			} else if (type == Number.class || type == Object.class) {
				// special cases
				return identity(); 
			} else {
				// failure
				throw new IllegalArgumentException("store type not compatible with accessor");
			}
		}
		// primitive case
		Function<P, Object> fn = primitive(Stores.hash(type));
		if (fn == null) throw new IllegalArgumentException("store type not compatible with accessor");
		return fn;
	}

	/* Ugly, but prechecked during construction */
	@SuppressWarnings({ "unchecked" })
	void set(int index, P value) {
		((Store<Object>)store).set(index, value == null ? null : convert.apply(value));
	}

	abstract Function<P, Object> identity();

	abstract Function<P, Object> primitive(int hash);

	static class Bytes extends NumberAccessor<Byte> implements StoreAccessors.StoreBytes {

		Bytes(Store<?> store) { super(store); }

		@Override public boolean isByte(int index) { return isAccessible(index); }
		@Override public byte getByte(int index) { return get(index).byteValue(); }
		@Override public void setByte(int index, byte value) { set(index, value); }

		@Override Function<Byte, Object> identity() { return b -> b; }

		@Override
		Function<Byte, Object> primitive(int hash) {
			switch (hash) {
			case Stores.BYTE :  return v ->          v;
			case Stores.FLOAT:  return v -> (float)  v;
			case Stores.SHORT:  return v -> (short)  v;
			case Stores.LONG:   return v -> (long)   v;
			case Stores.INT:    return v -> (int)    v;
			case Stores.DOUBLE: return v -> (double) v;
			default: return null;
			}
		}

	}

	static class Shorts extends NumberAccessor<Short> implements StoreAccessors.StoreShorts {

		Shorts(Store<?> store) { super(store); }

		@Override public boolean isShort(int index) { return isAccessible(index); }
		@Override public short getShort(int index) { return get(index).shortValue(); }
		@Override public void setShort(int index, short value) { set(index, value); }

		@Override Function<Short, Object> identity() { return b -> b; }

		@Override
		Function<Short, Object> primitive(int hash) {
			switch (hash) {
			case Stores.BYTE :  return v ->          v;
			case Stores.FLOAT:  return v -> (float)  v.byteValue();
			case Stores.SHORT:  return v -> (short)  v.byteValue();
			case Stores.LONG:   return v -> (long)   v.byteValue();
			case Stores.INT:    return v -> (int)    v.byteValue();
			case Stores.DOUBLE: return v -> (double) v.byteValue();
			default: return null;
			}
		}

	}

	static class Ints extends NumberAccessor<Integer> implements StoreAccessors.StoreInts {

		Ints(Store<?> store) { super(store); }

		@Override public boolean isInt(int index) { return isAccessible(index); }
		@Override public int getInt(int index) { return get(index).intValue(); }
		@Override public void setInt(int index, int value) { set(index, value); }

		@Override Function<Integer, Object> identity() { return b -> b; }

		@Override
		Function<Integer, Object> primitive(int hash) {
			switch (hash) {
			case Stores.BYTE :  return v ->          v;
			case Stores.FLOAT:  return v -> (float)  v.intValue();
			case Stores.SHORT:  return v -> (short)  v.intValue();
			case Stores.LONG:   return v -> (long)   v.intValue();
			case Stores.INT:    return v -> (int)    v.intValue();
			case Stores.DOUBLE: return v -> (double) v.intValue();
			default: return null;
			}
		}

	}

	static class Longs extends NumberAccessor<Long> implements StoreAccessors.StoreLongs {

		Longs(Store<?> store) { super(store); }

		@Override public boolean isLong(int index) { return isAccessible(index); }
		@Override public long getLong(int index) { return get(index).longValue(); }
		@Override public void setLong(int index, long value) { set(index, value); }

		@Override Function<Long, Object> identity() { return b -> b; }

		@Override
		Function<Long, Object> primitive(int hash) {
			switch (hash) {
			case Stores.BYTE :  return v ->          v;
			case Stores.FLOAT:  return v -> (float)  v.longValue();
			case Stores.SHORT:  return v -> (short)  v.longValue();
			case Stores.LONG:   return v -> (long)   v.longValue();
			case Stores.INT:    return v -> (int)    v.longValue();
			case Stores.DOUBLE: return v -> (double) v.longValue();
			default: return null;
			}
		}

	}

	static class Floats extends NumberAccessor<Float> implements StoreAccessors.StoreFloats {

		Floats(Store<?> store) { super(store); }

		@Override public boolean isFloat(int index) { return isAccessible(index); }
		@Override public float getFloat(int index) { return get(index).floatValue(); }
		@Override public void setFloat(int index, float value) { set(index, value); }

		@Override Function<Float, Object> identity() { return b -> b; }

		@Override
		Function<Float, Object> primitive(int hash) {
			switch (hash) {
			case Stores.BYTE :  return v ->          v;
			case Stores.FLOAT:  return v -> (float)  v.byteValue();
			case Stores.SHORT:  return v -> (short)  v.byteValue();
			case Stores.LONG:   return v -> (long)   v.byteValue();
			case Stores.INT:    return v -> (int)    v.byteValue();
			case Stores.DOUBLE: return v -> (double) v.byteValue();
			default: return null;
			}
		}

	}

	static class Doubles extends NumberAccessor<Double> implements StoreAccessors.StoreDoubles {

		Doubles(Store<?> store) { super(store); }

		@Override public boolean isDouble(int index) { return isAccessible(index); }
		@Override public double getDouble(int index) { return get(index).doubleValue(); }
		@Override public void setDouble(int index, double value) { set(index, value); }

		@Override Function<Double, Object> identity() { return b -> b; }

		@Override
		Function<Double, Object> primitive(int hash) {
			switch (hash) {
			case Stores.BYTE :  return v ->          v;
			case Stores.FLOAT:  return v -> (float)  v.byteValue();
			case Stores.SHORT:  return v -> (short)  v.byteValue();
			case Stores.LONG:   return v -> (long)   v.byteValue();
			case Stores.INT:    return v -> (int)    v.byteValue();
			case Stores.DOUBLE: return v -> (double) v.byteValue();
			default: return null;
			}
		}

	}

}
