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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class StoreListTest {

	@Test
	public void testToArray() {
		StoreType<String> t = StoreType.of(String.class);
		String[] a = { "A", null, "B" };
		Store<String> s = t.objectsAsStore(a);
		{
			String[] b = new String[3];
			String[] c = s.asList().toArray(b);
			assertArrayEquals(a, c);
			assertSame(b, c);
		}

		{
			Object[] b = new Object[3];
			Object[] c = s.asList().toArray(b);
			assertArrayEquals(a, c);
			assertSame(b, c);
		}

		{
			String[] b = new String[2];
			String[] c = s.range(1, 3).asList().toArray(b);
			assertEquals(2, b.length);
			assertEquals(a[1], b[0]);
			assertEquals(a[2], b[1]);
			assertSame(b, c);
		}

		Store<String> ss = t.storage().newStoreOf(a);
		{
			Object[] b = new Object[3];
			Object[] c = ss.asList().toArray(b);
			assertArrayEquals(a, c);
			assertSame(b, c);
		}
	}

}
