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

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class StoreArraysTest {

	private static int[] ints(int... ints) { return ints; }

	@Test
	public void testCopyOfRange() {
		int[] src = {0,1,2,3,4};
		assertArrayEquals(ints(0,1,2,3,4), StoreArrays.ints.copyOfRange(src, 0, 5, 0));
		assertArrayEquals(ints(0,1,2,3), StoreArrays.ints.copyOfRange(src, 0, 4, 0));
		assertArrayEquals(ints(1,2,3), StoreArrays.ints.copyOfRange(src, 1, 4, 0));
		assertArrayEquals(ints(1,2,3,4), StoreArrays.ints.copyOfRange(src, 1, 5, 0));
		assertArrayEquals(ints(1,2,3,4,9), StoreArrays.ints.copyOfRange(src, 1, 6, 9));
		assertArrayEquals(ints(9,0,1,2,3,4,9), StoreArrays.ints.copyOfRange(src, -1, 6, 9));
		assertArrayEquals(ints(9,0,1,2,3), StoreArrays.ints.copyOfRange(src, -1, 4, 9));
		assertArrayEquals(ints(9,9,9,9), StoreArrays.ints.copyOfRange(src, 10, 14, 9));
		assertArrayEquals(ints(9,9,9,9), StoreArrays.ints.copyOfRange(src, -4, 0, 9));
	}


}
