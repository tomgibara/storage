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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Month;
import java.util.Optional;

import org.junit.Test;

public class StoresTest {

	@Test
	public void testIsNullable() {
		assertFalse( Stores.objects(Optional.empty()).nullValue().isPresent() );
		assertTrue( Stores.objects(Optional.of(new Object())).nullValue().isPresent() );
		assertFalse( Stores.objectsAndNull(Optional.of(new Object())).nullValue().isPresent() );
	}

	@Test
	public void testPrimitiveNullability() {
		try {
			Stores.ints(1,2,3).set(0, null);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		Stores.intsAndNull(1,2,3).set(0, null);
	}

	@Test
	public void testPrimitiveIsNull() {
		assertFalse( Stores.ints(1,2,3).isNull(0) );
		Store<Integer> ints = Stores.intsAndNull(1,2,3);
		ints.set(0, null);
		assertTrue( ints.isNull(0) );
		assertFalse( ints.isNull(1) );
	}

	@Test
	public void testDefaultNullValue() {
		assertEquals(Optional.of(""), Stores.defaultNullValue(String.class));
		assertEquals(Optional.of(0), Stores.defaultNullValue(int.class));
		assertEquals(Optional.of(0), Stores.defaultNullValue(Integer.class));
		assertEquals(Optional.of(false), Stores.defaultNullValue(boolean.class));
		assertEquals(Optional.of(Month.JANUARY), Stores.defaultNullValue(Month.class));
	}
}
