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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Month;

import org.junit.Test;

import com.tomgibara.fundament.Consumer;
import com.tomgibara.fundament.Mapping;

public class StoreTypeTest {

	@Test
	public void testOf() {
		try {
			StoreType.of(null);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		Consumer<Class<?>> chk = clss -> assertEquals(clss, StoreType.of(clss).valueType());
		chk.consume(byte.class);
		chk.consume(short.class);
		chk.consume(int.class);
		chk.consume(long.class);
		chk.consume(boolean.class);
		chk.consume(char.class);
		chk.consume(float.class);
		chk.consume(double.class);
		chk.consume(Object.class);
		chk.consume(String.class);
	}

	@Test
	public void testEmpty() {
		assertFalse(StoreType.generic().emptyStore().isMutable());
		assertFalse(StoreType.of(String.class).emptyStore().isMutable());
	}

	@Test
	public void testObjectAsStore() {
		{
			StoreType<String> t = StoreType.of(String.class).settingNullDisallowed();
			Store<String> s = t.objectAsStore("Test");
			assertEquals("Test", s.get(0));
			assertEquals(t, s.type());
		}
		{
			StoreType<Integer> t = StoreType.of(Integer.class).settingNullDisallowed();
			Store<Integer> s = t.objectAsStore(24);
			assertEquals(24, s.get(0).intValue());
			assertEquals(t, s.type());
		}
		{
			StoreType<Integer> t = StoreType.of(Integer.class).settingNullAllowed();
			Store<Integer> s = t.objectAsStore(24);
			assertEquals(24, s.get(0).intValue());
			assertEquals(t, s.type());
		}
		{
			StoreType<Integer> t = StoreType.of(int.class).settingNullDisallowed();
			Store<Integer> s = t.objectAsStore(24);
			assertEquals(24, s.get(0).intValue());
			assertEquals(t, s.type());
		}
	}

	@Test
	public void testPrimitiveNullability() {

		{ // not supporting null
			Store<Integer> ints = StoreType.of(int.class).settingNullToValue(0).arrayAsStore(new int[] {0,1,2,3});
			ints.set(0, null);
			assertEquals(0, ints.get(0).intValue());
		}
		{ // supporting null
			Store<Integer> ints = StoreType.of(int.class).settingNullAllowed().arrayAsStore(new int[] {1,2,3});
			ints.set(0, null);
			assertNull(ints.get(0));
		}
	}

	@Test
	public void testInitialCount() {
		StoreType<Integer> type = StoreType.of(int.class).settingNullAllowed();
		assertEquals(4, type.arrayAsStore(new int[] {0,1,2,3}).count());
		assertEquals(0, type.arrayAsStore(new int[0]).count());
	}

	@Test
	public void testPrimitiveIsNull() {
		Store<Integer> ints = StoreType.of(int.class).settingNullAllowed().arrayAsStore(new int[] {1,2,3});
		ints.set(0, null);
		assertTrue( ints.isNull(0) );
		assertFalse( ints.isNull(1) );
	}

	@Test
	public void testConstructors() {
		assertTrue(StoreType.generic().settingNullAllowed().nullSettable());
		assertTrue(StoreType.generic().settingNullAllowed().nullGettable());
		assertFalse(StoreType.generic().settingNullDisallowed().nullSettable());
		assertFalse(StoreType.generic().settingNullDisallowed().nullGettable());
		assertTrue(StoreType.generic().settingNullToValue("").nullSettable());
		assertFalse(StoreType.generic().settingNullToValue("").nullGettable());
		assertTrue(StoreType.generic().settingNullToValue(null).nullSettable());
		assertTrue(StoreType.generic().settingNullToValue(null).nullGettable());
	}

	enum EmptyEnum { }

	enum NonEmptyEnum { DOG, CAT; }

	@Test
	public void testEnumDefault() {
		assertEquals(NonEmptyEnum.DOG, StoreType.of(NonEmptyEnum.class).settingNullToDefault().nullValue());
		assertTrue(StoreType.of(NonEmptyEnum.class).settingNullToDefault().nullSettable());
		assertFalse(StoreType.of(EmptyEnum.class).settingNullToDefault().nullSettable());
	}

	@Test
	public void testDefaultNullValue() {
		assertEquals("", StoreType.of(String.class).settingNullToDefault().nullValue());
		assertEquals(0, StoreType.of(int.class).settingNullToDefault().nullValue().intValue());
		assertEquals(0, StoreType.of(Integer.class).settingNullToDefault().nullValue().intValue());
		assertEquals(false, StoreType.of(boolean.class).settingNullToDefault().nullValue().booleanValue());
		assertEquals(Month.JANUARY, StoreType.of(Month.class).settingNullToDefault().nullValue());
		assertNull(StoreType.generic().settingNullToDefault().nullValue());
		assertFalse(StoreType.generic().settingNullToDefault().nullSettable());
	}

	@Test
	public void testToString() {
		assertEquals("java.lang.Object (null allowed)", StoreType.generic().toString());
		assertEquals("int (null set to 0)", StoreType.of(int.class).settingNullToValue(0).toString());
		assertEquals("java.lang.String (null disallowed)", StoreType.of(String.class).settingNullDisallowed().toString());
	}

	@Test
	public void testMapping() {
		Store<Integer> ints = Stores.ints(1,2,3,4);
		Store<Float> floats = ints.asTransformedBy(Mapping.fromFunction(int.class, float.class, i -> i * 0.5f));
		StoreType<Float> t = StoreType.of(float.class).settingNullDisallowed();
		assertEquals(t, floats.type());
	}

	@Test
	public void testIsAssignableTo() {
		assertTrue(StoreType.of(String.class).isAssignableTo(Object.class));
		assertFalse(StoreType.of(Object.class).isAssignableTo(String.class));
		assertFalse(StoreType.of(int.class).isAssignableTo(long.class));
		assertFalse(StoreType.of(int.class).isAssignableTo(Integer.class));
		assertFalse(StoreType.of(int.class).isAssignableTo(int.class));
		assertTrue(StoreType.of(int.class).settingNullToValue(0).isAssignableTo(int.class));
		assertTrue(StoreType.of(int.class).settingNullDisallowed().isAssignableTo(int.class));
		assertFalse(StoreType.of(int.class).settingNullDisallowed().isAssignableTo(long.class));
	}
}
