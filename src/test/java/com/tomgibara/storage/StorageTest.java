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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class StorageTest {

	@Test
	public void testSmallValueStorage() {
		StoreType<Integer> ints = StoreType.of(int.class).settingNullToValue(0);
		{
			Storage<Integer> t = ints.smallValueStorage(4);
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
			Storage<Integer> t = ints.smallValueStorage(1);
			Store<Integer> s = t.newStore(10);
			assertEquals(Collections.nCopies(10, 0), s.asList());
			assertTrue(s.population().ones().isAll());
		}

		{
			Storage<Integer> t = ints.settingNullAllowed().smallValueStorage(1);
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
			Storage<Integer> t = ints.smallValueStorage(3);
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
			assertEquals(ints.smallValueStorage(1).newStore(23), s);
			s.fill(1);
			assertEquals(Collections.nCopies(23, 1), s.asList());
		}

		{
			Storage<Integer> t = ints.smallValueStorage(4);
			Store<Integer> s = t.newStore(10);
		}

		{
			Storage<Integer> t = ints.smallValueStorage(5);
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

		for (int range = 2; range < 16; range++) {
			Storage<Integer> t = ints.settingNullToValue(1).smallValueStorage(range);
			Store<Integer> s = t.newStore(10);
			assertEquals(Collections.nCopies(10, 1), s.asList());
			assertTrue(s.population().ones().isAll());
		}


	}

	@Test
	public void testEnumStorage() {

		Storage<Tri> t = StoreType.of(Tri.class).settingNullToValue(Tri.SCALENE).storage();
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
		assertEquals(s.type().nullValue(), s.get(0));

		assertEquals(Tri.ISOSCELES, StoreType.of(Tri.class).settingNullToValue(Tri.ISOSCELES).storage().newStore(1).get(0));

		testEnumStorageConstruction(t);
		testEnumStorageConstruction(StoreType.of(Tri.class).settingNullDisallowed().storage());
	}

	private void testEnumStorageConstruction(Storage<Tri> t) {
		{
			Store<Tri> orig = Stores.objects(Tri.EQUILATERAL, Tri.ISOSCELES, Tri.SCALENE);
			assertEquals(orig, t.newCopyOf(orig));
		}
		{
			Store<Tri> orig = StoreType.<Tri>generic().settingNullDisallowed().objectsAsStore(Tri.EQUILATERAL, Tri.ISOSCELES, Tri.SCALENE);
			assertEquals(orig, t.newCopyOf(orig));
		}
		{
			Tri[] arr = { Tri.EQUILATERAL, Tri.ISOSCELES, Tri.SCALENE };
			Assert.assertArrayEquals(arr, t.newStoreOf(arr).asList().toArray());
		}
	}

	@Test
	public void testNullEnumStorage() {

		Storage<Tri> t = StoreType.of(Tri.class).storage();
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
		assertNotNull(StoreType.of(String.class).settingNullToValue("").storage().newStore(10).get(0));
		assertNotNull(StoreType.generic().settingNullToValue(new Object()).storage().newStore(10).get(0));
	}

	@Test
	public void testNewStoreOf() {
		Store<String> muStr = StoreType.of(String.class).settingNullToValue("").storage().mutable().newStoreOf("One", "Two", "Three", null);
		assertTrue(muStr.isMutable());
		assertEquals("One", muStr.get(0));
		assertEquals("", muStr.get(3));

		Store<String> imStr = StoreType.of(String.class).storage().immutable().newStoreOf("One", "Two", "Three", null);
		assertFalse(imStr.isMutable());
		assertEquals("One", imStr.get(0));
		assertNull(imStr.get(3));

		Store<String> muGen = StoreType.<String>generic().storage().mutable().newStoreOf("X", "Y", "Z");
		assertEquals(Object.class, muGen.type().valueType());

		Store<Integer> muInt = StoreType.of(int.class).storage().mutable().newStoreOf(1,2,3,4);
		assertTrue(muInt.isMutable());
		assertEquals(1, muInt.get(0).intValue());
		assertEquals(4, muInt.get(3).intValue());
		assertEquals(int.class, muInt.type().valueType());
	}

	@Test
	public void testNewStore() throws Exception {
		// generic
		{
			StoreType<Object> type = StoreType.generic();
			assertEquals(0, type.storage().newStore(0).size());
			assertEquals(5, type.storage().newStore(5).size());
			assertEquals(0, type.storage().newStore(5).count());
			assertEquals(0, type.settingNullDisallowed().storage().newStore(0).size());
			checkIAE(() -> type.settingNullDisallowed().storage().newStore(5));
			assertEquals(5, type.settingNullToValue("").storage().newStore(5).size());
			assertEquals(5, type.settingNullToValue("").storage().newStore(5).count());
		}
		// typed regular
		{
			StoreType<String> type = StoreType.of(String.class);
			assertEquals(0, type.storage().newStore(0).size());
			assertEquals(5, type.storage().newStore(5).size());
			assertEquals(0, type.storage().newStore(5).count());
			assertEquals(0, type.settingNullDisallowed().storage().newStore(0).size());
			checkIAE(() -> type.settingNullDisallowed().storage().newStore(5));
			assertEquals(5, type.settingNullToValue("").storage().newStore(5).size());
			assertEquals(5, type.settingNullToValue("").storage().newStore(5).count());
		}
		// typed primitive
		{
			StoreType<Integer> type = StoreType.of(int.class);
			assertEquals(0, type.storage().newStore(0).size());
			assertEquals(5, type.storage().newStore(5).size());
			assertEquals(0, type.storage().newStore(5).count());
			assertEquals(0, type.settingNullDisallowed().storage().newStore(0).size());
			checkIAE(() -> type.settingNullDisallowed().storage().newStore(5));
			assertEquals(5, type.settingNullToValue(0).storage().newStore(5).size());
			assertEquals(5, type.settingNullToValue(0).storage().newStore(5).count());
		}
		// typed enum
		{
			StoreType<Tri> type = StoreType.of(Tri.class);
			assertEquals(0, type.storage().newStore(0).size());
			assertEquals(5, type.storage().newStore(5).size());
			assertEquals(0, type.storage().newStore(5).count());
			assertEquals(0, type.settingNullDisallowed().storage().newStore(0).size());
			checkIAE(() -> type.settingNullDisallowed().storage().newStore(5));
			assertEquals(5, type.settingNullToValue(Tri.SCALENE).storage().newStore(5).size());
			assertEquals(5, type.settingNullToValue(Tri.SCALENE).storage().newStore(5).count());
		}
		// small value
		StoreType<Integer> type = StoreType.of(int.class);
		for (int range = 1; range < 8; range++) {
			int r = range;
			assertEquals(0, type.settingNullAllowed().smallValueStorage(r).newStore(0).size());
			assertEquals(5, type.settingNullAllowed().smallValueStorage(r).newStore(5).size());
			assertEquals(0, type.settingNullAllowed().smallValueStorage(r).newStore(5).count());
			assertEquals(0, type.settingNullDisallowed().smallValueStorage(r).newStore(0).size());
			checkIAE(() ->  type.settingNullDisallowed().smallValueStorage(r).newStore(5));
			assertEquals(5, type.settingNullToValue(0).smallValueStorage(r).newStore(5).size());
			assertEquals(5, type.settingNullToValue(0).smallValueStorage(r).newStore(5).count());
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

	enum Tri {

		SCALENE,
		ISOSCELES,
		EQUILATERAL
	}
}
