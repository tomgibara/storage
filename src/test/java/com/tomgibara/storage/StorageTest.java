package com.tomgibara.storage;

import java.util.Collections;

import junit.framework.TestCase;

public class StorageTest extends TestCase {

	public void testSmallValueStorage() {
		
		{
			Storage<Integer> t = Storage.smallValues(4, false);
			Store<Integer> s = t.newStore(100);
			s.set(0, 0);
			s.set(1, 1);
			s.set(2, 2);
			s.set(3, 3);
			
			for (int i = 0; i < 4; i++) {
				assertEquals(i, s.get(i).intValue());
			}
			
			s.fill(0);
			assertEquals(t.newStore(100), s);
			
			s.fill(2);
			assertEquals(Collections.nCopies(100, 2), s.asList());
		}

		{
			Storage<Integer> t = Storage.smallValues(1, false);
			Store<Integer> s = t.newStore(10);
			assertEquals(Collections.nCopies(10, 0), s.asList());
			assertTrue(s.population().ones().isAll());
		}

		{
			Storage<Integer> t = Storage.smallValues(1, true);
			Store<Integer> s = t.newStore(10);
			s.set(0, 0);
			s.set(3, 0);
			s.set(6, 0);
			assertEquals(0, s.get(3).intValue());
			assertNull(s.get(1));
			assertNull(s.get(2));
			assertEquals("0001001001", s.population().toString());
		}

		{
			Storage<Integer> t = Storage.smallValues(3, false);
			Store<Integer> s = t.newStore(23);
			s.set(1, 1);
			s.set(2, 2);
			s.set(3, 0);
			s.set(4, 1);
			s.set(5, 2);
			for (int i = 0; i < 10; i++) {
				int v = i % 3;
				s.set(i, v);
				assertEquals(v, s.get(i).intValue());
			}
			for (int i = 0; i < 10; i++) {
				assertEquals(i % 3, s.get(i).intValue());
			}
			s.fill(0);
			assertEquals(Storage.smallValues(1, false).newStore(23), s);
			s.fill(1);
			assertEquals(Collections.nCopies(23, 1), s.asList());
		}
		
		{
			Storage<Integer> t = Storage.smallValues(5, false);
			Store<Integer> s = t.newStore(16);
			assertEquals(Collections.nCopies(16, 0), s.asList());
			for (int i = 0; i < 16; i++) {
				int v = i % 5;
				s.set(i, v);
				assertEquals(v, s.get(i).intValue());
			}
			for (int i = 0; i < 16; i++) {
				assertEquals(i % 5, s.get(i).intValue());
			}
		}
	}
}