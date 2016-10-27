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

import static com.tomgibara.storage.StoreNullity.settingNullDisallowed;
import static com.tomgibara.storage.StoreNullity.settingNullToValue;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

public class StorageTest {

	@Test
	public void testSmallValueStorage() {

		{
			Storage<Integer> t = Storage.smallValues(4, false);
			Store<Integer> s = t.newStore(100);
			s.set(0, 0);
			s.set(1, 1);
			s.set(2, 2);
			s.set(3, 3);

			for (int i = 0; i < 4; i++) {
				assertEquals(i, s.get(i).intValue());
			}

			s.fill(0);
			assertEquals(t.newStore(100), s);

			s.fill(2);
			assertEquals(Collections.nCopies(100, 2), s.asList());
		}

		{
			Storage<Integer> t = Storage.smallValues(1, false);
			Store<Integer> s = t.newStore(10);
			assertEquals(Collections.nCopies(10, 0), s.asList());
			assertTrue(s.population().ones().isAll());
		}

		{
			Storage<Integer> t = Storage.smallValues(1, true);
			Store<Integer> s = t.newStore(10);
			s.set(0, 0);
			s.set(3, 0);
			s.set(6, 0);
			assertEquals(0, s.get(3).intValue());
			assertFalse(s.isNull(0));
			assertNull(s.get(1));
			assertTrue(s.isNull(1));
			assertNull(s.get(2));
			assertTrue(s.isNull(2));
			assertEquals("0001001001", s.population().toString());
		}

		{
			Storage<Integer> t = Storage.smallValues(3, false);
			Store<Integer> s = t.newStore(23);
			s.set(1, 1);
			s.set(2, 2);
			s.set(3, 0);
			s.set(4, 1);
			s.set(5, 2);
			for (int i = 0; i < 10; i++) {
				int v = i % 3;
				s.set(i, v);
				assertEquals(v, s.get(i).intValue());
			}
			for (int i = 0; i < 10; i++) {
				assertEquals(i % 3, s.get(i).intValue());
			}
			s.fill(0);
			assertEquals(Storage.smallValues(1, false).newStore(23), s);
			s.fill(1);
			assertEquals(Collections.nCopies(23, 1), s.asList());
		}

		{
			Storage<Integer> t = Storage.smallValues(4, false);
			Store<Integer> s = t.newStore(10);
		}

		{
			Storage<Integer> t = Storage.smallValues(5, false);
			Store<Integer> s = t.newStore(16);
			assertEquals(Collections.nCopies(16, 0), s.asList());
			for (int i = 0; i < 16; i++) {
				int v = i % 5;
				s.set(i, v);
				assertEquals(v, s.get(i).intValue());
			}
			for (int i = 0; i < 16; i++) {
				assertEquals(i % 5, s.get(i).intValue());
			}
		}
	}

	@Test
	public void testEnumStorage() {

		Storage<Tri> t = Storage.typed(Tri.class, settingNullToValue(Tri.SCALENE));
		Store<Tri> s = t.newStore(10);
		assertEquals(Tri.SCALENE, s.get(0));
		assertEquals(Tri.SCALENE, s.get(9));
		assertEquals(Tri.SCALENE, s.set(0, Tri.ISOSCELES));
		assertEquals(Tri.ISOSCELES, s.set(0, Tri.EQUILATERAL));
		assertEquals(Tri.EQUILATERAL, s.set(0, Tri.SCALENE));
		s.fill(Tri.ISOSCELES);
		assertEquals(s, s.mutableCopy());
		assertEquals(s, s.immutableCopy());
		assertEquals(Collections.nCopies(10, Tri.ISOSCELES), s.asList());
		s.asList().set(0, null);
		assertEquals(s.nullity().nullValue(), s.get(0));

		assertEquals(Tri.ISOSCELES, Storage.typed(Tri.class, settingNullToValue(Tri.ISOSCELES)).newStore(1).get(0));
	}

	@Test
	public void testNullEnumStorage() {

		Storage<Tri> t = Storage.typed(Tri.class);
		Store<Tri> s = t.newStore(10);
		assertEquals(null, s.set(0, Tri.ISOSCELES));
		assertEquals(Tri.ISOSCELES, s.set(0, Tri.EQUILATERAL));
		assertEquals(Tri.EQUILATERAL, s.set(0, Tri.SCALENE));
		s.fill(Tri.ISOSCELES);
		assertEquals(s, s.mutableCopy());
		assertEquals(s, s.immutableCopy());
		assertEquals(Collections.nCopies(10, Tri.ISOSCELES), s.asList());
		assertFalse(s.isNull(0));
		s.set(0, null);
		assertTrue(s.isNull(0));
	}

	@Test
	public void testNonNullStorage() {
		assertNotNull(Storage.typed(String.class, settingNullToValue("")).newStore(10).get(0));
		assertNotNull(Storage.generic(settingNullToValue(new Object())).newStore(10).get(0));
	}

	@Test
	public void testNewStoreOf() {
		Store<String> muStr = Storage.typed(String.class, settingNullToValue("")).mutable().newStoreOf("One", "Two", "Three", null);
		assertTrue(muStr.isMutable());
		assertEquals("One", muStr.get(0));
		assertEquals("", muStr.get(3));

		Store<String> imStr = Storage.typed(String.class).immutable().newStoreOf("One", "Two", "Three", null);
		assertFalse(imStr.isMutable());
		assertEquals("One", imStr.get(0));
		assertNull(imStr.get(3));

		Store<String> muGen = Storage.<String>generic().mutable().newStoreOf("X", "Y", "Z");
		assertEquals(Object.class, muGen.valueType());

		Store<Integer> muInt = Storage.typed(int.class).mutable().newStoreOf(1,2,3,4);
		assertTrue(muInt.isMutable());
		assertEquals(1, muInt.get(0).intValue());
		assertEquals(4, muInt.get(3).intValue());
		assertEquals(int.class, muInt.valueType());
	}

	@Test
	public void testNewStore() throws Exception {
		// generic
		assertEquals(0, Storage.generic().newStore(0).size());
		assertEquals(5, Storage.generic().newStore(5).size());
		assertEquals(0, Storage.generic().newStore(5).count());
		assertEquals(0, Storage.generic(settingNullDisallowed()).newStore(0).size());
		checkIAE(() -> Storage.generic(settingNullDisallowed()).newStore(5));
		assertEquals(5, Storage.generic(settingNullToValue("")).newStore(5).size());
		assertEquals(5, Storage.generic(settingNullToValue("")).newStore(5).count());
		// typed regular
		assertEquals(0, Storage.typed(String.class).newStore(0).size());
		assertEquals(5, Storage.typed(String.class).newStore(5).size());
		assertEquals(0, Storage.typed(String.class).newStore(5).count());
		assertEquals(0, Storage.typed(String.class, settingNullDisallowed()).newStore(0).size());
		checkIAE(() -> Storage.typed(String.class, settingNullDisallowed()).newStore(5));
		assertEquals(5, Storage.typed(String.class, settingNullToValue("")).newStore(5).size());
		assertEquals(5, Storage.typed(String.class, settingNullToValue("")).newStore(5).count());
		// typed primitive
		assertEquals(0, Storage.typed(int.class).newStore(0).size());
		assertEquals(5, Storage.typed(int.class).newStore(5).size());
		assertEquals(0, Storage.typed(int.class).newStore(5).count());
		assertEquals(0, Storage.typed(int.class, settingNullDisallowed()).newStore(0).size());
		checkIAE(() ->  Storage.typed(int.class, settingNullDisallowed()).newStore(5));
		assertEquals(5, Storage.typed(int.class, settingNullToValue(0)).newStore(5).size());
		assertEquals(5, Storage.typed(int.class, settingNullToValue(0)).newStore(5).count());
		// typed enum
		assertEquals(0, Storage.typed(Tri.class).newStore(0).size());
		assertEquals(5, Storage.typed(Tri.class).newStore(5).size());
		assertEquals(0, Storage.typed(Tri.class).newStore(5).count());
		assertEquals(0, Storage.typed(Tri.class, settingNullDisallowed()).newStore(0).size());
		checkIAE(() ->  Storage.typed(Tri.class, settingNullDisallowed()).newStore(5));
		assertEquals(5, Storage.typed(Tri.class, settingNullToValue(Tri.SCALENE)).newStore(5).size());
		assertEquals(5, Storage.typed(Tri.class, settingNullToValue(Tri.SCALENE)).newStore(5).count());
		// small value
		assertEquals(0, Storage.smallValues(4,true).newStore(0).size());
		assertEquals(5, Storage.smallValues(4,true).newStore(5).size());
		assertEquals(0, Storage.smallValues(4,true).newStore(5).count());
		assertEquals(0, Storage.smallValues(4,false).newStore(0).size());
		assertEquals(5, Storage.smallValues(4,false).newStore(5).size());
		assertEquals(5, Storage.smallValues(4,false).newStore(5).count());
	}

	private void checkIAE(Runnable r) {
		try {
			r.run();
			fail("expected IAE");
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}

	enum Tri {

		SCALENE,
		ISOSCELES,
		EQUILATERAL
	}
}
