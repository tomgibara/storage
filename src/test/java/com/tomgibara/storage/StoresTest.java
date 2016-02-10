package com.tomgibara.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
}
