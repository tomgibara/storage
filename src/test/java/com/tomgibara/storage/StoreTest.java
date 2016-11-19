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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Bijection;
import com.tomgibara.fundament.Producer;
import com.tomgibara.storage.StorageTest.Tri;

public class StoreTest {

	@Test
	public void testTransformedBy() {

		Store<Integer> s = StoreType.of(int.class).settingNullAllowed().arrayAsStore(new int[] {1,2,3});
		assertTrue(s.type().nullGettable());
		Store<Integer> t = s.asTransformedBy(i -> 2 * i);
		assertEquals(s.count(), t.count());
		for (int i = 0; i < s.count(); i++) {
			assertEquals(s.get(i) * 2, t.get(i).intValue());
		}

		Iterator<Integer> it = t.iterator();
		assertTrue(it.hasNext());
		assertEquals(2, it.next().intValue());
		assertTrue(it.hasNext());
		assertEquals(4, it.next().intValue());
		assertTrue(it.hasNext());
		assertEquals(6, it.next().intValue());
		assertFalse(it.hasNext());

		assertTrue(s.isMutable());
		assertTrue(t.isMutable());
		t.set(0, null);
		assertNull(s.get(0));

		t = s.immutableView().asTransformedBy(i -> 2 * i);
		assertFalse(s.immutableView().isMutable());
		assertFalse(t.isMutable());
		try {
			t.transpose(0, 1);
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}
		s.set(0, null);
		assertNull(t.get(0));
	}

	@Test
	public void testTransformedByBijection() {
		Store<Integer> s = StoreType.of(int.class).settingNullAllowed().arrayAsStore(new int[] {1, 2, 3});
		Bijection<Integer, String> fn = Bijection.fromFunctions(Integer.class, String.class, i -> i.toString(), t -> Integer.parseInt(t));
		Store<String> t = s.asTransformedBy(fn);
		assertEquals("1", t.get(0));
		t.set(0, "1000");
		assertEquals("1000", t.get(0));
		assertEquals(1000, s.get(0).intValue());
	}

	@Test
	public void testNullableResizedCopy() {
		Store<Integer> s = StoreType.of(int.class).settingNullAllowed().arrayAsStore(new int[] {1, 2, 3});
		Store<Integer> t = s.resizedCopy(5);
		assertTrue(t.isMutable());
		assertTrue(s.type().nullGettable());
		assertEquals(asList(1,2,3,null,null), t.asList());
		Store<Integer> u = s.immutableCopy();
		t.set(0, 0);
		t.set(3, 0);
		t.set(4, 0);
		assertEquals(asList(0,2,3,0,0), t.asList());
		assertEquals(u.asList(), s.asList());
		assertEquals(asList(1,2), s.resizedCopy(2).asList());
	}

	@Test
	public void testResizedCopy() {
		// check prohibition on enlarging non-nullable stores
		checkIAE(() -> StoreType.generic().settingNullDisallowed().objectsAsStore("A","B","C").resizedCopy(4));
		checkIAE(() -> StoreType.generic().settingNullDisallowed().storage().newStore(0).resizedCopy(1));
		checkIAE(() -> StoreType.of(String.class).settingNullDisallowed().storage().newStore(0).resizedCopy(1));
		checkIAE(() -> StoreType.of(Tri.class).settingNullDisallowed().storage().newStore(0).resizedCopy(1));
		checkIAE(() -> StoreType.of(int.class).settingNullDisallowed().storage().newStore(0).resizedCopy(1));
		// check enlarging stores with null values
		assertEquals("", StoreType.generic().settingNullToValue("").objectsAsStore("A","B","C").resizedCopy(4).get(3));
		assertEquals("", StoreType.generic().settingNullToValue("").storage().newStore(0).resizedCopy(1).get(0));
		assertEquals("Moo", StoreType.of(String.class).settingNullToValue("Moo").storage().newStore(0).resizedCopy(1).get(0));
		assertEquals(Tri.EQUILATERAL, StoreType.of(Tri.class).settingNullToValue(Tri.EQUILATERAL).storage().newStore(0).resizedCopy(1).get(0));
		assertEquals(4, StoreType.of(int.class).settingNullToValue(4).storage().newStore(0).resizedCopy(1).get(0).intValue());
		// check with ints
		checkIAE(() -> Stores.ints(1,2,3).resizedCopy(5));
		Store<Integer> s = StoreType.of(int.class).settingNullToValue(0).arrayAsStore(new int[] {1,2,3});
		Store<Integer> t = s.resizedCopy(5);
		assertTrue(t.isMutable());
		assertTrue(t.type().nullSettable());
		assertFalse(t.type().nullGettable());
		assertEquals(asList(1,2,3,0,0), t.asList());
		Store<Integer> u = s.immutableCopy();
		t.set(0, 4);
		t.set(3, 4);
		t.set(4, 4);
		assertEquals(asList(4,2,3,4,4), t.asList());
		assertEquals(u.asList(), s.asList());
		assertEquals(asList(1,2), s.resizedCopy(2).asList());
	}

	@Test
	public void testObjectMethods() {
		Store<Integer> s = Stores.ints(1, 2, 3);
		Store<Integer> t = StoreType.of(int.class).settingNullAllowed().arrayAsStore(new int[] {1, 2, 3});
		Store<Object> u = Stores.objects(new Object[] {1,2,3});
		Store<Integer> v = Stores.ints(1, 2, 4);

		assertTrue(s.equals(s));
		assertTrue(s.equals(t));
		assertTrue(s.equals(u));
		assertFalse(s.equals(v));

		assertEquals(s.hashCode(), t.hashCode());
		assertEquals(s.hashCode(), u.hashCode());

		assertEquals(s.toString(), t.toString());
		assertEquals(s.toString(), u.toString());
	}

	@Test
	public void testIterator() {
		Store<Integer> s = StoreType.of(int.class).settingNullAllowed().arrayAsStore(new int[] {0,2,4,6,8});
		s.set(1, null);
		s.set(3, null);
		Iterator<Integer> i = s.iterator();
		assertTrue(i.hasNext());
		assertEquals(0, i.next().intValue());
		assertTrue(i.hasNext());
		assertEquals(4, i.next().intValue());
		assertTrue(i.hasNext());
		assertEquals(8, i.next().intValue());
		i.remove();
		assertFalse(i.hasNext());
	}

	@Test
	public void testForEach() {
		Store<Integer> s = StoreType.of(int.class).settingNullAllowed().arrayAsStore(new int[] {0,1,2,3,4,5,6});
		int size = s.size();
		s.set(3, null);
		s.set(6, null);
		BitStore p = Bits.store(size);
		s.forEach(i -> p.setBit(i, true));
		assertEquals(p, s.population());
	}

	@Test
	public void testCompact() {
		StoreType<Integer> ints = StoreType.of(int.class);
		testCompact(ints.settingNullAllowed().arrayAsStore(new int[] {}));
		testCompact(ints.settingNullAllowed().arrayAsStore(new int[] {0,1,2,3}));
		testCompact(Stores.ints(0,1,2,3));
		testCompact(ints.settingNullAllowed().arrayAsStore(new int[] {0,1}).immutableView());

		Store<Tri> tris = StoreType.of(Tri.class).storage().newStore(4);
		tris.set(0, Tri.EQUILATERAL);
		tris.set(1, Tri.ISOSCELES);
		tris.set(2, Tri.SCALENE);
		tris.set(3, Tri.EQUILATERAL);
		testCompact(tris);

		Store<String> strs = Stores.objects("One", "Two", "Three", "Four", "Five", "Six");
		testCompact(strs);
	}

	// s is a full store of even length
	private <E> void testCompact(Store<E> s) {
		int size = s.size();
		assertEquals(size, s.count());
		assertEquals(0, size & 1);
		if (!s.isMutable()) try {
			s.compact();
			fail("compacted immutable store");
		} catch (IllegalStateException e) {
			/* expected */
			return;
		}
		Store<E> c = s.mutableCopy();
		assertFalse(c.compact());
		assertEquals(s, c);
		if (!c.type().nullGettable()) return; // cannot test more
		for (int i = 0; i < c.size(); i += 2) c.set(i, null);
		assertEquals(c.size() > 0, c.compact());
		assertEquals(size / 2, c.count());
		for (int i = 0; i < size; i++) {
			assertEquals(i >= size / 2, c.isNull(i));
		}
		Store<E> cc = c.mutableCopy();
		cc.compact();
		assertEquals(c, cc);
	}

	@Test
	public void testImmutableCopy() {
		Store<String> org = Stores.immutableObjects("A", "B", "C", null);
		Store<String> cpy = org.immutableCopy();
		assertEquals(org, cpy);
		assertEquals(org.count(), cpy.count());
		assertFalse(cpy.isMutable());
		assertNotSame(org, cpy);
	}

	@Test
	public void testSpliterator() {
		{
			Store<String> strs = Stores.objects("A", "B", null, "D");
			Spliterator<String> s = strs.spliterator();
			s.tryAdvance(v -> assertEquals("A", v));
			s.tryAdvance(v -> assertEquals("B", v));
			s.tryAdvance(v -> assertEquals("D", v));
			assertFalse(s.tryAdvance(v -> fail()));
			testSpliterator(strs);
			testSpliterator(strs.immutable());
		}
		{
			Store<String> strs = StoreType.of(String.class).settingNullDisallowed().objectsAsStore("A", "B", "C", "D");
			Spliterator<String> s = strs.spliterator();
			assertEquals(4L, s.getExactSizeIfKnown());
			s.tryAdvance(v -> assertEquals("A", v));
			s.tryAdvance(v -> assertEquals("B", v));
			s.tryAdvance(v -> assertEquals("C", v));
			s.tryAdvance(v -> assertEquals("D", v));
			assertFalse(s.tryAdvance(v -> fail()));
			testSpliterator(strs);
			testSpliterator(strs.immutable());
		}
		{
			Store<Integer> ints = Stores.ints(1,2,3,4);
			Spliterator<Integer> s = ints.spliterator();
			assertEquals(4L, s.getExactSizeIfKnown());
			for (int i = 1; i <= 4; i++) {
				final int e = i;
				s.tryAdvance(v -> assertEquals(e, v.intValue()));
			}
			assertFalse(s.tryAdvance(v -> fail()));
			testSpliterator(ints);
		}
		{
			Store<Double> doubles = Stores.doubles(1.0,2.0,3.0,4.0);
			Spliterator<Double> s = doubles.spliterator();
			assertEquals(4L, s.getExactSizeIfKnown());
			for (int i = 1; i <= 4; i++) {
				final double e = i;
				s.tryAdvance(v -> assertEquals(e, v.doubleValue(), 0.0));
			}
			assertFalse(s.tryAdvance(v -> fail()));
			testSpliterator(doubles);
		}
		{
			Store<Long> longs = Stores.longs(1L,2L,3L,4L);
			Spliterator<Long> s = longs.spliterator();
			assertEquals(4L, s.getExactSizeIfKnown());
			for (int i = 1; i <= 4; i++) {
				final long e = i;
				s.tryAdvance(v -> assertEquals(e, v.intValue()));
			}
			assertFalse(s.tryAdvance(v -> fail()));
			testSpliterator(longs);
		}
		{
			Store<String> xs = StoreType.of(String.class).constantStore("X", 11);
			Spliterator<String> s = xs.spliterator();
			assertEquals(11L, s.getExactSizeIfKnown());
			for (int i = 0; i < 11; i++) {
				s.tryAdvance(v -> assertEquals("X", v));
			}
			testSpliterator(xs);
		}
		{
			Store<Object> none = StoreType.generic().constantStore(null, 4);
			Spliterator<Object> s = none.spliterator();
			assertFalse(s.tryAdvance(v -> fail()));
			testSpliterator(none);
		}
	}

	private <V> void testSpliterator(Store<V> s) {
		List<V> expected = new ArrayList<>(s.asList());
		expected.removeIf(v -> v == null);
		assertEquals(expected, StreamSupport.stream(s.spliterator(), false).collect(Collectors.toList()));
	}

	@Test
	public void testSmallStoreResize() {
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			int range = 1 + r.nextInt(15);
			int nullValue = r.nextInt(range);
			int initValue = r.nextInt(range);
			int initSize = r.nextInt(50);
			int nextSize = r.nextInt(100);

			Store<Integer> store = StoreType.of(int.class).settingNullToValue(nullValue).smallValueStorage(range).newStore(initSize);
			store.fill(initValue);
			store = store.resizedCopy(nextSize);

			Store<Integer> check = StoreType.<Integer>generic().settingNullToValue(nullValue).storage().newStore(initSize);
			check.fill(initValue);
			check = check.resizedCopy(nextSize);

			assertEquals(check, store);
		}
	}

	@Test
	public void testIsSettable() {
		{
			Store<Integer> store = StoreType.of(int.class).settingNullDisallowed().storage().newStore(1, 0);
			assertTrue(store.isSettable(0));
			assertFalse(store.isSettable(null));
			assertTrue(store.isSettable(new Integer(79357945)));
			assertFalse(store.isSettable(0L));
			assertFalse(store.isSettable(""));
			assertFalse(store.isSettable(new Object()));
		}
		{
			Store<Integer> store = StoreType.of(int.class).settingNullAllowed().storage().newStore(1, 0);
			assertTrue(store.isSettable(0));
			assertTrue(store.isSettable(null));
			assertTrue(store.isSettable(new Integer(79357945)));
			assertFalse(store.isSettable(0L));
			assertFalse(store.isSettable(""));
			assertFalse(store.isSettable(new Object()));
		}
		{
			Store<Integer> store = StoreType.of(int.class).settingNullToDefault().storage().newStore(1, 0);
			assertTrue(store.isSettable(0));
			assertTrue(store.isSettable(null));
			assertTrue(store.isSettable(new Integer(79357945)));
			assertFalse(store.isSettable(0L));
			assertFalse(store.isSettable(""));
			assertFalse(store.isSettable(new Object()));
		}
		{
			Store<Object> store = StoreType.generic().settingNullAllowed().storage().newStore(1, 0);
			assertTrue(store.isSettable(0));
			assertTrue(store.isSettable(null));
			assertTrue(store.isSettable(new Integer(79357945)));
			assertTrue(store.isSettable(0L));
			assertTrue(store.isSettable(""));
			assertTrue(store.isSettable(new Object()));
		}
		{
			Store<Object> store = StoreType.generic().settingNullDisallowed().storage().newStore(1, 0);
			assertTrue(store.isSettable(0));
			assertFalse(store.isSettable(null));
			assertTrue(store.isSettable(new Integer(79357945)));
			assertTrue(store.isSettable(0L));
			assertTrue(store.isSettable(""));
			assertTrue(store.isSettable(new Object()));
		}
		{
			Store<String> store = StoreType.of(String.class).settingNullAllowed().storage().newStore(1);
			assertFalse(store.isSettable(0));
			assertTrue(store.isSettable(null));
			assertFalse(store.isSettable(new Integer(79357945)));
			assertFalse(store.isSettable(0L));
			assertTrue(store.isSettable(""));
			assertFalse(store.isSettable(new Object()));
		}
		{
			Store<String> store = StoreType.of(String.class).settingNullDisallowed().storage().newStore(1, "");
			assertFalse(store.isSettable(0));
			assertFalse(store.isSettable(null));
			assertFalse(store.isSettable(new Integer(79357945)));
			assertFalse(store.isSettable(0L));
			assertTrue(store.isSettable(""));
			assertFalse(store.isSettable(new Object()));
		}
		{
			StoreType<Integer> ints = StoreType.of(int.class);
			for (int n = 0; n < 3; n++) {
				StoreType<Integer> type;
				switch (n) {
				case 0: type = ints.settingNullDisallowed(); break;
				case 1: type = ints.settingNullAllowed(); break;
				case 2: type = ints.settingNullToDefault(); break;
				default: throw new IllegalStateException();
				}
				for (int r = 1; r < 10; r++) {
					Store<Integer> store = type.smallValueStorage(r).newStore(0);
					assertFalse(store.isSettable(this));
					assertFalse(store.isSettable(0L));
					assertEquals(n != 0, store.isSettable(null));
					for (int v = -2; v < 15; v++) {
						assertEquals("setting " + v + " on " + r, v >= 0 && v < r, store.isSettable(v));
					}
				}
			}
		}
	}

	@Test
	public void testClear() {
		checkISE(() -> Stores.ints(1,2,3,4,5).clear());
		IntFunction<Store<?>> nulls = size -> StoreType.generic().constantStore(null, size);
		{
			Store<Integer> objs = Stores.objects(1,2,3,4);
			objs.clear();
			assertEquals(nulls.apply(4), objs);
		}
		for (int r = 1; r < 10; r++) {
			Store<Integer> s = StoreType.of(int.class).smallValueStorage(r).newStoreOf(0,0,0,0);
			s.clear();
			assertEquals(nulls.apply(4), s);
		}
	}

	@Test
	public void testSetStore() {
		testSetStore(int.class);
		testSetStore(Integer.class);
	}

	private <V> void testSetStore(Class<?> clss) {
		StoreType<Integer> type = (StoreType<Integer>) StoreType.of(clss);
		testSetStore(type.settingNullAllowed());
		testSetStore(type.settingNullDisallowed());
		testSetStore(type.settingNullToValue(9));
	}

	private <V> void testSetStore(StoreType<Integer> type) {
		Store<Integer> intsA = type.storage().newStore(10, 0);
		Store<Integer> intsB = type.storage().newStore(5, 1);
		if (type.nullSettable()) {
			intsB.set(1, null);
			intsB.set(3, null);
		}
		testSetStore(intsA, 0, intsB);
		testSetStore(intsA, 5, intsB);
	}

	private <V> void testSetStore(Store<V> dst, int position, Store<V> src) {
		Store<V> t0 = dst.mutableCopy();
		for (int i = 0; i < src.size(); i++) {
			t0.set(position + i, src.get(i));
		}
		Store<V> t1 = dst.mutableCopy();
		t1.setStore(position, src);
		Store<V> t2 = dst.mutableCopy();
		t2.setStore(position, new DefaultStore<>(src));
		assertEquals(t0, t1);
		assertEquals(t0, t2);
		assertEquals(new DefaultStore<>(t0).size(), t0.size());
		assertEquals(new DefaultStore<>(t1).size(), t1.size());
		assertEquals(new DefaultStore<>(t2).size(), t2.size());
	}

	@Test
	public void testSetStoreRandom() {
		Random r = new Random(0L);
		int tests = 100;
		Function<StoreType<Object>, Store<Object>> p = StorageTestUtil.randomStores(r);
		StorageTestUtil.forAllTypes(t -> {
			for (int test = 0; test < tests; test++) {
				Store<Object> a = p.apply((StoreType<Object>) t);
				Store<Object> b = p.apply((StoreType<Object>) t);
				testSetStore(r, a, b);
			}
		});
	}

	private void testSetStore(Random r, Store<Object> a, Store<Object> b) {
		Store<Object> bg;
		Store<Object> sm;
		int pos = r.nextInt(Math.abs(b.size() - a.size()) + 1);
		if (a.size() < b .size()) {
			sm = a;
			bg = b;
		} else {
			sm = b;
			bg = a;
		}
		bg.setStore(pos, sm);
		bg.setStore(pos, new DefaultStore<>(sm));
	}

	@Test
	public void testRange() {
		testRandom(this::testRange);
	}

	private <V> void testRange(Random r, Store<V> store) {
		int to = r.nextInt(store.size() + 1);
		int from = r.nextInt(to + 1);
		int rangeSize = to - from;
		Store<V> range = store.range(from, to);
		assertEquals(rangeSize, range.size());
		BitStore p1 = store.population();
		BitStore p2 = range.population();
		for (int i = 0; i < rangeSize; i++) {
			assertEquals(range.get(i), store.get(from + i));
			assertEquals(p2.getBit(i), p1.getBit(from + i));
		}
		if (store.type().nullSettable()) {
			Store<V> resized = range.resizedCopy(rangeSize * 2);
			assertEquals(range, resized.range(0, rangeSize));
			resized.setStore(rangeSize, range);
			for (int i = 0; i < rangeSize; i++) {
				assertEquals(range.get(i), resized.get(rangeSize + i));
			}
		}
	}

	@Test
	public void testObjectMethodsRandom() {
		testRandom(this::testObjectMethods);
	}

	private <V> void testObjectMethods(Random r, Store<V> store) {
		DefaultStore<V> def = new DefaultStore<V>(store);
		String type = store.getClass().getName();
		assertEquals(type, store, def);
		assertEquals(type, store.hashCode(), def.hashCode());
		assertEquals(type, store.toString(), def.toString());
	}

	@Test
	public void testFillAndClear() {
		testRandom(this::testFillAndClear);
	}

	private <V> void testFillAndClear(Random r, Store<V> store) {
		V value = store.type().settingNullToDefault().nullValue;
		if (value != null || store.type().nullSettable()) {
			store.fill(value);
			assertEquals(store.type().constantStore(value, store.size()), store);
		}
		if (store.type().nullSettable()) {
			store.fill(null);
			assertEquals(store.type().constantStore(null, store.size()), store);
		}
	}

	private void checkIAE(Runnable r) {
		try {
			r.run();
			fail("expected IAE");
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}

	private void checkISE(Runnable r) {
		try {
			r.run();
			fail("expected ISE");
		} catch (IllegalStateException e) {
			/* expected */
		}
	}

	private static final class DefaultStore<V> extends AbstractStore<V> {
		private final Store<V> store;
		DefaultStore(Store<V> store) { this.store = store; }
		@Override public int size() { return store.size(); }
		@Override public V get(int index) { return store.get(index); }
	}
	
	private void testRandom(RandomTest rt) {
		Random r = new Random(0L);
		int tests = 100;
		Function<StoreType<Object>, Store<Object>> p = StorageTestUtil.randomStores(r);
		StorageTestUtil.forAllTypes(t -> {
			for (int test = 0; test < tests; test++) {
				Store<Object> store = p.apply((StoreType<Object>) t);
				rt.perform(r, store);
			}
		});
		for (int range = 1; range < 16; range++) {
			Producer<Store<Integer>> p1 = StorageTestUtil.randomSmallValueStores(r, range, true);
			Producer<Store<Integer>> p2 = StorageTestUtil.randomSmallValueStores(r, range, false);
			for (int test = 0; test < tests; test++) {
				rt.perform(r, p1.produce());
				rt.perform(r, p2.produce());
			}
		}
	}

	private interface RandomTest {
		
		<V> void perform(Random r, Store<V> store);
	}
}
