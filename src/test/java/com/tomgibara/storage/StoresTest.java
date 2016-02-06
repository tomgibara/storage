package com.tomgibara.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class StoresTest {

	@Test
	public void testIsNullable() {
		assertTrue( Stores.objects(true).isNullAllowed() );
		assertFalse( Stores.objects(false).isNullAllowed() );
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
}
