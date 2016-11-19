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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NullConstantStoreTest {

	@Test
	public void testCopying() {
		for (int size = 0; size < 5; size++) {
			testCopying(StoreType.of(Integer.class), size);
			testCopying(StoreType.of(int.class), size);
		}
	}

	private void testCopying(StoreType<Integer> type, int size) {
		assertTrue(type.nullGettable);
		Store<Integer> store = type.constantStore(null, size);
		assertFalse(store.isMutable());
		assertEquals(size, store.size());
		assertEquals(0, store.count());
		Store<Integer> copy = store.copiedBy(type.storage());
		assertEquals(copy, store);
		assertEquals(copy.toString(), store.toString());
		assertEquals(copy.hashCode(), store.hashCode());
		assertEquals(copy.asList(), store.asList());
		assertEquals(copy, store.mutableCopy());
		for (int f = 0; f <= size; f++) {
			for (int t = f; t <= size; t++) {
				assertEquals(copy.range(f,t), store.range(f, t));
			}
		}
	}

}
