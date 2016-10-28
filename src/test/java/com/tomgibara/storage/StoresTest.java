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

import static com.tomgibara.storage.StoreNullity.settingNullAllowed;
import static com.tomgibara.storage.StoreNullity.settingNullToValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Month;

import org.junit.Test;

public class StoresTest {

	@Test
	public void testEmpty() {
		assertFalse(Stores.empty().isMutable());
		assertFalse(Stores.empty(String.class).isMutable());
	}

	@Test
	public void testPrimitiveNullability() {
		{ // not supporting null
			Store<Integer> ints = Stores.intsWithNullity(settingNullToValue(0),1,2,3);
			ints.set(0, null);
			assertEquals(0, ints.get(0).intValue());
		}
		{ // supporting null
			Store<Integer> ints = Stores.intsWithNullity(settingNullAllowed(),1,2,3);
			ints.set(0, null);
			assertNull(ints.get(0));
		}
	}

	@Test
	public void testPrimitiveIsNull() {
		assertFalse( Stores.ints(1,2,3).isNull(0) );
		Store<Integer> ints = Stores.intsWithNullity(settingNullAllowed(),1,2,3);
		ints.set(0, null);
		assertTrue( ints.isNull(0) );
		assertFalse( ints.isNull(1) );
	}

	@Test
	public void testDefaultNullValue() {
		assertEquals("", StoreNullity.defaultForType(String.class).nullValue());
		assertEquals(0, StoreNullity.defaultForType(int.class).nullValue().intValue());
		assertEquals(0, StoreNullity.defaultForType(Integer.class).nullValue().intValue());
		assertEquals(false, StoreNullity.defaultForType(boolean.class).nullValue().booleanValue());
		assertEquals(Month.JANUARY, StoreNullity.defaultForType(Month.class).nullValue());
	}

	@Test
	public void testInitialCount() {
		assertEquals(4, Stores.ints(0,1,2,3).count());
		assertEquals(4, Stores.intsWithNullity(settingNullAllowed(),0,1,2,3).count());
		assertEquals(0, Stores.ints().count());
		assertEquals(0, Stores.intsWithNullity(settingNullAllowed()).count());
	}
}
