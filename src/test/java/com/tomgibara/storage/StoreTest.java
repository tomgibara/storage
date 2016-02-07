package com.tomgibara.storage;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Test;

import com.tomgibara.fundament.Bijection;

public class StoreTest {

	@Test
	public void testTransformedBy() {
		
		Store<Integer> s = Stores.intsAndNull(1,2,3);
		assertTrue(s.isNullAllowed());
		Store<Integer> t = s.asTransformedBy(i -> 2 * i);
		assertEquals(s.count(), t.count());
		for (int i = 0; i < s.count(); i++) {
			assertEquals(s.get(i) * 2, t.get(i).intValue());
		}
		
		Iterator<Integer> it = t.iterator();
		assertTrue(it.hasNext());
		assertEquals(2, it.next().intValue());
		assertTrue(it.hasNext());
		assertEquals(4, it.next().intValue());
		assertTrue(it.hasNext());
		assertEquals(6, it.next().intValue());
		assertFalse(it.hasNext());
		
		assertTrue(s.isMutable());
		assertTrue(t.isMutable());
		t.set(0, null);
		assertNull(s.get(0));
		
		t = s.immutableView().asTransformedBy(i -> 2 * i);
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

	@Test
	public void testTransformedByBijection() {
		Store<Integer> s = Stores.intsAndNull(1,2,3);
		Bijection<Integer, String> fn = new Bijection<Integer, String>() {
			@Override public String apply(Integer t) { return t.toString(); }
			@Override public Integer disapply(String r) { return Integer.parseInt(r); }
		};
		Store<String> t = s.asTransformedBy(String.class, fn);
		assertEquals("1", t.get(0));
		t.set(0, "1000");
		assertEquals("1000", t.get(0));
		assertEquals(1000, s.get(0).intValue());
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
	
	@Test
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

	@Test
	public void testObjectMethods() {
		Store<Integer> s = Stores.ints(1, 2, 3);
		Store<Integer> t = Stores.intsAndNull(1, 2, 3);
		Store<Object> u = Stores.objects(true, new Object[] {1,2,3});
		Store<Integer> v = Stores.ints(1, 2, 4);

		assertTrue(s.equals(s));
		assertTrue(s.equals(t));
		assertTrue(s.equals(u));
		assertFalse(s.equals(v));
		
		assertEquals(s.hashCode(), t.hashCode());
		assertEquals(s.hashCode(), u.hashCode());

		assertEquals(s.toString(), t.toString());
		assertEquals(s.toString(), u.toString());
	}

	@Test
	public void testIterator() {
		Store<Integer> s = Stores.intsAndNull(0,2,4,6,8);
		s.set(1, null);
		s.set(3, null);
		Iterator<Integer> i = s.iterator();
		assertTrue(i.hasNext());
		assertEquals(0, i.next().intValue());
		assertTrue(i.hasNext());
		assertEquals(4, i.next().intValue());
		assertTrue(i.hasNext());
		assertEquals(8, i.next().intValue());
		i.remove();
		assertFalse(i.hasNext());
	}
}
