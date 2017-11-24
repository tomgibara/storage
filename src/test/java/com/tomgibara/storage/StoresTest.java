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

import org.junit.Assert;
import org.junit.Test;

public class StoresTest {

	@Test
	public void testPrimitiveIsNull() {
		assertFalse( Stores.ints(1,2,3).isNull(0) );
	}

	@Test
	public void testPrimitiveMutability() {
		Store<Integer> store = Stores.ints(1,2,3).immutable();
		assertFalse(store.isMutable());

		try {
			store.set(0, 999);
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}

		try {
			store.fill(999);
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}

	}

	@Test
	public void testInitialCount() {
		assertEquals(4, Stores.ints(0,1,2,3).count());
		assertEquals(0, Stores.ints().count());
	}

	@Test
	public void testVarargs() {
		Store<Object> store = Stores.objects("Me", "Myself", "I");
		store.set(0, new Object());
	}

	@Test
	public void testSingleton() {
		Store<Integer> a = Stores.singleInt(4);
		assertEquals(1, a.size());
		assertEquals(4, a.get(0).intValue());
		assertTrue(a.population().getBit(0));
		assertFalse(a.isMutable());
		assertEquals(1, a.asList().size());
		assertEquals(4, a.asList().get(0).intValue());
		Store<Integer> b = a.asTransformedBy(i -> i + 1);
		assertEquals(1, b.size());
		assertEquals(5, b.get(0).intValue());
		Store<Object> c = Stores.singleObject(null);
		assertEquals(1, c.size());
		assertEquals(null, c.get(0));
		assertTrue(c.isNull(0));
		Store<String> d = Stores.singleObject("Hello");
		assertEquals(1, d.size());
		assertEquals("Hello", d.get(0));
		assertFalse(d.isNull(0));
	}
}
