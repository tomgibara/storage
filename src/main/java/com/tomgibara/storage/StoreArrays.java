/*
 * Copyright 2016 Tom Gibara
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

final class StoreArrays {

 	// package statics

	static final ByteStoreArray    bytes    = new ByteStoreArray();
	static final ShortStoreArray   shorts   = new ShortStoreArray();
	static final IntStoreArray     ints     = new IntStoreArray();
	static final LongStoreArray    longs    = new LongStoreArray();
	static final BooleanStoreArray booleans = new BooleanStoreArray();
	static final CharStoreArray    chars    = new CharStoreArray();
	static final FloatStoreArray   floats   = new FloatStoreArray();
	static final DoubleStoreArray  doubles  = new DoubleStoreArray();
	static final ObjectStoreArray  objects  = new ObjectStoreArray();

	private static StoreArray<?,?>[] primitives = {
		null,     bytes,    floats,   chars,    shorts,   null,    longs,    ints,
		null,     null,     null,     doubles,  booleans, null,    null,     null,
	};

	@SuppressWarnings("unchecked")
	static <A,C> StoreArray<A,C> forType(Class<?> type) {
		return (StoreArray<A,C>) (type.isPrimitive() ? primitives[Stores.hash(type)] : objects);
	}

	static <A,C> StoreArray<A,C> forArray(A array) {
		return forType(array.getClass().getComponentType());
	}

	// inner classes

	static abstract class StoreArray<A,C> {

		A copyOfRange(A vs, int from, int to, C nullValue) {
			int length = length(vs);
			int newLength = to - from;
			// trivial case, zero length
			if (newLength == 0) {
				return create(0);
			}
			// empty case, no overlap
			if (length == 0 || from >= length || to <= 0) {
				A copy = create(newLength);
				fill(copy, nullValue);
				return copy;
			}
			// zero indexed
			if (from == 0) {
				if (newLength == length) return copy(vs);
				A copy = copy(vs, newLength);
				if (newLength > length) fill(copy, length, newLength, nullValue);
				return copy;
			}
			// grow forwards
			if (from > 0) {
				A copy = copy(vs, from, to);
				if (to > length) fill(copy, length - from, newLength, nullValue);
				return copy;
			}
			// grow backwards
			{
				A copy = create(newLength);
				int limit = Math.min(length, to);
				System.arraycopy(vs, 0, copy, -from, limit);
				fill(copy, 0, -from, nullValue);
				fill(copy, limit + 1, newLength, nullValue);
				return copy;
			}
		}

		A copyOfRange(A vs, int from, int to) {
			int length = length(vs);
			int newLength = to - from;
			// trivial case, zero length
			if (newLength == 0) return create(0);
			// empty case, no overlap
			if (length == 0 || from >= length || to <= 0) return create(newLength);
			// zero indexed
			if (from == 0) return newLength == length ? copy(vs) : copy(vs, newLength);
			// grow forwards
			if (from > 0) return copy(vs, from, to);
			// grow backwards
			A copy = create(newLength);
			int limit = Math.min(length, to);
			System.arraycopy(vs, 0, copy, -from, limit);
			return copy;
		}

		void copyIntoArray(Store<C> store, A array, C nullValue) {
			int length = length(array);
			int limit = Math.min( store.size(), length);
			if (store.type().nullGettable) {
				for (int i = 0; i < limit; i++) {
					C v = store.get(i);
					set(array, i, v == null ? nullValue : v);
				}
			} else {
				for (int i = 0; i < limit; i++) {
					set(array, i, store.get(i));
				}
			}
			if (limit < length) fill(array, limit, length, nullValue);
		}

		abstract int length(A a);
		abstract A create(int length);
		abstract A copy(A a);
		abstract A copy(A a, int length);
		abstract A copy(A a, int from, int to);
		abstract void fill(A a, C n);
		abstract void fill(A a, int from, int to, C n);
		abstract void set(A a, int i, C v);
	}

	final static class ByteStoreArray extends StoreArray<byte[], Byte> {
		@Override int length(byte[] a) { return a.length; }
		@Override byte[] create(int length) { return new byte[length]; }
		@Override byte[] copy(byte[] a) { return a.clone(); }
		@Override byte[] copy(byte[] a, int length) { return Arrays.copyOf(a, length); }
		@Override byte[] copy(byte[] a, int from, int to) { return Arrays.copyOfRange(a, from, to); }
		@Override void fill(byte[] a, Byte n) { byte b = n; if (b != (byte) 0) Arrays.fill(a, b); }
		@Override void fill(byte[] a, int from, int to, Byte n) { byte b = n; if (b != (byte) 0) Arrays.fill(a, from, to, b); }
		@Override void set(byte[] a, int i, Byte v) { a[i] = v; }
	}

	final static class ShortStoreArray extends StoreArray<short[], Short> {
		@Override int length(short[] a) { return a.length; }
		@Override short[] create(int length) { return new short[length]; }
		@Override short[] copy(short[] a) { return a.clone(); }
		@Override short[] copy(short[] a, int length) { return Arrays.copyOf(a, length); }
		@Override short[] copy(short[] a, int from, int to) { return Arrays.copyOfRange(a, from, to); }
		@Override void fill(short[] a, Short n) { short b = n; if (b != (short) 0) Arrays.fill(a, b); }
		@Override void fill(short[] a, int from, int to, Short n) { short b = n; if (b != (short) 0) Arrays.fill(a, from, to, b); }
		@Override void set(short[] a, int i, Short v) { a[i] = v; }
	}

	final static class IntStoreArray extends StoreArray<int[], Integer> {
		@Override int length(int[] a) { return a.length; }
		@Override int[] create(int length) { return new int[length]; }
		@Override int[] copy(int[] a) { return a.clone(); }
		@Override int[] copy(int[] a, int length) { return Arrays.copyOf(a, length); }
		@Override int[] copy(int[] a, int from, int to) { return Arrays.copyOfRange(a, from, to); }
		@Override void fill(int[] a, Integer n) { int b = n; if (b != 0) Arrays.fill(a, b); }
		@Override void fill(int[] a, int from, int to, Integer n) { int b = n; if (b != 0) Arrays.fill(a, from, to, b); }
		@Override void set(int[] a, int i, Integer v) { a[i] = v; }
	}

	final static class LongStoreArray extends StoreArray<long[], Long> {
		@Override int length(long[] a) { return a.length; }
		@Override long[] create(int length) { return new long[length]; }
		@Override long[] copy(long[] a) { return a.clone(); }
		@Override long[] copy(long[] a, int length) { return Arrays.copyOf(a, length); }
		@Override long[] copy(long[] a, int from, int to) { return Arrays.copyOfRange(a, from, to); }
		@Override void fill(long[] a, Long n) { long b = n; if (b != 0L) Arrays.fill(a, b); }
		@Override void fill(long[] a, int from, int to, Long n) { long b = n; if (b != 0L) Arrays.fill(a, from, to, b); }
		@Override void set(long[] a, int i, Long v) { a[i] = v; }
	}

	final static class BooleanStoreArray extends StoreArray<boolean[], Boolean> {
		@Override int length(boolean[] a) { return a.length; }
		@Override boolean[] create(int length) { return new boolean[length]; }
		@Override boolean[] copy(boolean[] a) { return a.clone(); }
		@Override boolean[] copy(boolean[] a, int length) { return Arrays.copyOf(a, length); }
		@Override boolean[] copy(boolean[] a, int from, int to) { return Arrays.copyOfRange(a, from, to); }
		@Override void fill(boolean[] a, Boolean n) { boolean b = n; if (b) Arrays.fill(a, b); }
		@Override void fill(boolean[] a, int from, int to, Boolean n) { boolean b = n; if (b) Arrays.fill(a, from, to, b); }
		@Override void set(boolean[] a, int i, Boolean v) { a[i] = v; }
	}

	final static class CharStoreArray extends StoreArray<char[], Character> {
		@Override int length(char[] a) { return a.length; }
		@Override char[] create(int length) { return new char[length]; }
		@Override char[] copy(char[] a) { return a.clone(); }
		@Override char[] copy(char[] a, int length) { return Arrays.copyOf(a, length); }
		@Override char[] copy(char[] a, int from, int to) { return Arrays.copyOfRange(a, from, to); }
		@Override void fill(char[] a, Character n) { char b = n; if (b != (char) 0) Arrays.fill(a, b); }
		@Override void fill(char[] a, int from, int to, Character n) { char b = n; if (b != (char) 0) Arrays.fill(a, from, to, b); }
		@Override void set(char[] a, int i, Character v) { a[i] = v; }
	}

	final static class FloatStoreArray extends StoreArray<float[], Float> {
		@Override int length(float[] a) { return a.length; }
		@Override float[] create(int length) { return new float[length]; }
		@Override float[] copy(float[] a) { return a.clone(); }
		@Override float[] copy(float[] a, int length) { return Arrays.copyOf(a, length); }
		@Override float[] copy(float[] a, int from, int to) { return Arrays.copyOfRange(a, from, to); }
		@Override void fill(float[] a, Float n) { float b = n; if (b != (float) 0) Arrays.fill(a, b); }
		@Override void fill(float[] a, int from, int to, Float n) { float b = n; if (b != (float) 0) Arrays.fill(a, from, to, b); }
		@Override void set(float[] a, int i, Float v) { a[i] = v; }
	}

	final static class DoubleStoreArray extends StoreArray<double[], Double> {
		@Override int length(double[] a) { return a.length; }
		@Override double[] create(int length) { return new double[length]; }
		@Override double[] copy(double[] a) { return a.clone(); }
		@Override double[] copy(double[] a, int length) { return Arrays.copyOf(a, length); }
		@Override double[] copy(double[] a, int from, int to) { return Arrays.copyOfRange(a, from, to); }
		@Override void fill(double[] a, Double n) { double b = n; if (b != (double) 0) Arrays.fill(a, b); }
		@Override void fill(double[] a, int from, int to, Double n) { double b = n; if (b != (double) 0) Arrays.fill(a, from, to, b); }
		@Override void set(double[] a, int i, Double v) { a[i] = v; }
	}

	final static class ObjectStoreArray extends StoreArray<Object[], Object> {
		@Override int length(Object[] a) { return a.length; }
		@Override Object[] create(int length) { return new Object[length]; }
		@Override Object[] copy(Object[] a) { return a.clone(); }
		@Override Object[] copy(Object[] a, int length) { return Arrays.copyOf(a, length); }
		@Override Object[] copy(Object[] a, int from, int to) { return Arrays.copyOfRange(a, from, to); }
		@Override void fill(Object[] a, Object n) { Object b = n; if (b != null) Arrays.fill(a, b); }
		@Override void fill(Object[] a, int from, int to, Object n) { Object b = n; if (b != null) Arrays.fill(a, from, to, b); }
		@Override void set(Object[] a, int i, Object v) { a[i] = v; }
	}

}
