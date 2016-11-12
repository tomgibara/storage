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
