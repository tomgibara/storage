package com.tomgibara.storage;

import static java.util.Arrays.asList;
import junit.framework.TestCase;

public class StoreTest extends TestCase {

	public void testTransformedBy() {
		
		Store<Integer> s = Stores.intsAndNull(1,2,3);
		assertTrue(s.isNullAllowed());
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
	
	public void testNullableResizedCopy() {
		Store<Integer> s = Stores.intsAndNull(1,2,3);
		Store<Integer> t = s.resizedCopy(5);
		assertTrue(t.isMutable());
		assertTrue(t.isNullAllowed());
		assertEquals(asList(1,2,3,null,null), t.asList());
		Store<Integer> u = s.immutableCopy();
		t.set(0, 0);
		t.set(3, 0);
		t.set(4, 0);
		assertEquals(asList(0,2,3,0,0), t.asList());
		assertEquals(u.asList(), s.asList());
		assertEquals(asList(1,2), s.resizedCopy(2).asList());
	}
	
	public void testResizedCopy() {
		Store<Integer> s = Stores.ints(1,2,3);
		Store<Integer> t = s.resizedCopy(5);
		assertTrue(t.isMutable());
		assertFalse(t.isNullAllowed());
		assertEquals(asList(1,2,3,0,0), t.asList());
		Store<Integer> u = s.immutableCopy();
		t.set(0, 4);
		t.set(3, 4);
		t.set(4, 4);
		assertEquals(asList(4,2,3,4,4), t.asList());
		assertEquals(u.asList(), s.asList());
		assertEquals(asList(1,2), s.resizedCopy(2).asList());
	}
	
}
