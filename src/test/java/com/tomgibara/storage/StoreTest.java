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

import static com.tomgibara.storage.StoreNullity.settingNullAllowed;
import static com.tomgibara.storage.StoreNullity.settingNullDisallowed;
import static com.tomgibara.storage.StoreNullity.settingNullToValue;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Bijection;
import com.tomgibara.storage.StorageTest.Tri;

public class StoreTest {

	@Test
	public void testTransformedBy() {

		Store<Integer> s = Stores.intsWithNullity(settingNullAllowed(),1,2,3);
		assertTrue(s.nullity().nullGettable());
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
		Store<Integer> s = Stores.intsWithNullity(settingNullAllowed(),1,2,3);
		Bijection<Integer, String> fn = Bijection.fromFunctions(Integer.class, String.class, i -> i.toString(), t -> Integer.parseInt(t));
		Store<String> t = s.asTransformedBy(fn);
		assertEquals("1", t.get(0));
		t.set(0, "1000");
		assertEquals("1000", t.get(0));
		assertEquals(1000, s.get(0).intValue());
	}

	@Test
	public void testNullableResizedCopy() {
		Store<Integer> s = Stores.intsWithNullity(settingNullAllowed(),1,2,3);
		Store<Integer> t = s.resizedCopy(5);
		assertTrue(t.isMutable());
		assertTrue(s.nullity().nullGettable());
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
		checkIAE(() -> Stores.immutableObjectsWithNullity(settingNullDisallowed(),"A","B","C").resizedCopy(4));
		checkIAE(() -> Storage.generic(settingNullDisallowed()).newStore(0).resizedCopy(1));
		checkIAE(() -> Storage.typed(String.class, settingNullDisallowed()).newStore(0).resizedCopy(1));
		checkIAE(() -> Storage.typed(Tri.class, settingNullDisallowed()).newStore(0).resizedCopy(1));
		checkIAE(() -> Storage.typed(int.class, settingNullDisallowed()).newStore(0).resizedCopy(1));
		// check enlarging stores with null values
		assertEquals("", Stores.immutableObjectsWithNullity(settingNullToValue(""),"A","B","C").resizedCopy(4).get(3));
		assertEquals("", Storage.generic(settingNullToValue("")).newStore(0).resizedCopy(1).get(0));
		assertEquals("Moo", Storage.typed(String.class, settingNullToValue("Moo")).newStore(0).resizedCopy(1).get(0));
		assertEquals(Tri.EQUILATERAL, Storage.typed(Tri.class, settingNullToValue(Tri.EQUILATERAL)).newStore(0).resizedCopy(1).get(0));
		assertEquals(4, Storage.typed(int.class, settingNullToValue(4)).newStore(0).resizedCopy(1).get(0).intValue());
		// check with ints
		checkIAE(() -> Stores.ints(1,2,3).resizedCopy(5));
		Store<Integer> s = Stores.intsWithNullity(StoreNullity.settingNullToValue(0),1,2,3);
		Store<Integer> t = s.resizedCopy(5);
		assertTrue(t.isMutable());
		assertTrue(t.nullity().nullSettable());
		assertFalse(t.nullity().nullGettable());
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
		Store<Integer> t = Stores.intsWithNullity(settingNullAllowed(), 1, 2, 3);
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
		Store<Integer> s = Stores.intsWithNullity(settingNullAllowed(),0,2,4,6,8);
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
		Store<Integer> s = Stores.intsWithNullity(settingNullAllowed(),0,1,2,3,4,5,6);
		int size = s.size();
		s.set(3, null);
		s.set(6, null);
		BitStore p = Bits.store(size);
		s.forEach(i -> p.setBit(i, true));
		assertEquals(p, s.population());
	}

	@Test
	public void testCompact() {
		testCompact(Stores.intsWithNullity(settingNullAllowed()));
		testCompact(Stores.intsWithNullity(settingNullAllowed(),0,1,2,3));
		testCompact(Stores.ints(0,1,2,3));
		testCompact(Stores.intsWithNullity(settingNullAllowed(),0,1).immutableView());

		Store<Tri> tris = Storage.typed(Tri.class).newStore(4);
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
		if (!c.nullity().nullGettable()) return; // cannot test more
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
			Store<String> strs = Stores.objectsWithNullity(settingNullDisallowed(), "A", "B", "C", "D");
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
			Store<String> xs = Stores.constantStore("X", 11);
			Spliterator<String> s = xs.spliterator();
			assertEquals(11L, s.getExactSizeIfKnown());
			for (int i = 0; i < 11; i++) {
				s.tryAdvance(v -> assertEquals("X", v));
			}
			testSpliterator(xs);
		}
		{
			Store<Object> none = Stores.constantStore(null, 4);
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

	public void testSmallStoreResize() {
		Random r = new Random(0L);
		for (int i = 0; i < 1000; i++) {
			int range = 1 + r.nextInt(15);
			int nullValue = r.nextInt(range);
			int initValue = r.nextInt(range);
			int initSize = r.nextInt(50);
			int nextSize = r.nextInt(100);

			Store<Integer> store = Storage.smallValues(range, settingNullToValue(nullValue)).newStore(initSize);
			store.fill(initValue);
			store = store.resizedCopy(nextSize);

			Store<Integer> check = Storage.generic(settingNullToValue(nullValue)).newStore(initSize);
			check.fill(initValue);
			check = check.resizedCopy(nextSize);
			
			assertEquals(check, store);
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

}
