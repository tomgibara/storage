package com.tomgibara.storage;

import junit.framework.TestCase;

public class StoreTest extends TestCase {

	public void testTransformedBy() {
		
		Store<Integer> s = Stores.newStore(1,2,3);
		Store<Integer> t = s.transformedBy(i -> 2 * i);
		assertEquals(s.count(), t.count());
		for (int i = 0; i < s.count(); i++) {
			assertEquals(s.get(i) * 2, t.get(i).intValue());
		}
		assertTrue(s.isMutable());
		assertFalse(t.isMutable());
		try {
			t.transpose(0, 1);
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}
		s.set(0, null);
		assertNull(t.get(0));
	}
	
}
