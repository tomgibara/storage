package com.tomgibara.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import com.tomgibara.storage.StoreAccessors.StoreBytes;
import com.tomgibara.storage.StoreAccessors.StoreInts;
import com.tomgibara.storage.StoreAccessors.StoreLongs;
import com.tomgibara.storage.StoreAccessors.StoreShorts;

public class StoreAccessorsTest {

	@Test
	public void testObjectAccess() {
		Store<Object> store = StoreType.of(Object.class).storage().newStore(10);
		StoreBytes bytes = StoreAccessors.bytesFor(store);
		store.set(0, null);
		store.set(1, "notabyte");
		store.set(2, (byte) 7);
		assertFalse(bytes.isByte(0));
		assertFalse(bytes.isByte(1));
		assertTrue(bytes.isByte(2));

		try {
			bytes.getByte(0);
			fail();
		} catch (RuntimeException e) {
			/* expected */
		}

		try {
			bytes.getByte(1);
			fail();
		} catch (RuntimeException e) {
			/* expected */
		}

		assertEquals(7, bytes.getByte(2));

		bytes.setByte(0, (byte) 8);
		assertTrue(bytes.isByte(0));
		assertEquals(8, bytes.getByte(0));
	}

	@Test
	public void testNumberAccess() {
		Store<Object> store = StoreType.of(Object.class).storage().newStore(10);
		StoreInts ints = StoreAccessors.intsFor(store);
		store.set(0, null);
		store.set(1, (byte) 100);
		store.set(2, 1000000);
		store.set(3, 100000000000000L);

		assertFalse(ints.isInt(0));
		assertTrue(ints.isInt(1));
		assertTrue(ints.isInt(2));
		assertTrue(ints.isInt(3));

		assertEquals((int) 100, ints.getInt(1));
		assertEquals((int) 1000000, ints.getInt(2));
		assertEquals((int) 100000000000000L, ints.getInt(3));

		ints.setInt(1, 100);
		assertEquals((int) 100, ints.getInt(1));
		assertEquals(Integer.class, store.get(1).getClass());
	}

	@Test
	public void testIncompatibleAccess() {
		Store<String> store = StoreType.of(String.class).storage().newStore(10);
		try {
			StoreAccessors.intsFor(store);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}

	@Test
	public void testIntegerAccess() {
		Store<Integer> store = StoreType.of(Integer.class).storage().newStore(10);
		StoreShorts shorts = StoreAccessors.shortsFor(store);
		StoreLongs longs = StoreAccessors.longsFor(store);

		store.set(1, null);
		store.set(1, 32765);

		assertEquals((short) 32765, shorts.getShort(1));
		shorts.setShort(1, (short) 100);
		assertEquals(100, shorts.getShort(1));
		assertEquals(Integer.class, store.get(1).getClass());

		assertEquals((long) 100, longs.getLong(1));
		longs.setLong(2, 100000000000000L);
		assertEquals((int) 100000000000000L, store.get(2).intValue());
	}

	@Test
	public void testIntAccess() {
		Store<Integer> store = StoreType.of(int.class).settingNullDisallowed().storage().newStore(10, 0);
		StoreInts ints = StoreAccessors.intsFor(store);
		assertSame(store, ints);

		store.set(0, 100);
		assertEquals(100, ints.getInt(0));
		ints.setInt(1, 101);
		assertEquals(101, store.get(1).intValue());

		try {
			StoreAccessors.intsFor( store.immutable() ).setInt(2, 1);
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}
	}

	@Test
	public void testSmallIntAccess() {
		for (int range = 1; range < 6; range++) {
			testSmallIntAccess(range, false);
			testSmallIntAccess(range, true);
		}
	}

	private void testSmallIntAccess(int range, boolean nullAllowed) {
		StoreType<Integer> type = StoreType.of(int.class);
		type = nullAllowed ? type.settingNullAllowed() : type.settingNullDisallowed();
		Store<Integer> store = type.smallValueStorage(range).newStore(10, 0);
		StoreInts ints = StoreAccessors.intsFor(store);
		assertSame(store, ints);

		ints.setInt(0, range - 1);
		assertEquals(range - 1, store.get(0).intValue());

		store.set(1, range - 1);
		assertEquals(range - 1, ints.getInt(1));

		try {
			ints.setInt(0, range);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}

		try {
			ints.setInt(0, -1);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}

		Store<Integer> imm = store.immutableView();
		StoreInts immInts = StoreAccessors.intsFor(imm);
		assertSame(imm, immInts);

		try {
			immInts.setInt(0, 0);
			fail();
		} catch (IllegalStateException e) {
			/* expected */
		}

		if (nullAllowed) {
			store.set(2, null);
			assertFalse(ints.isInt(2));
			try {
				ints.getInt(2);
				fail();
			} catch (RuntimeException e) {
				/* expected */
			}
		}
	}
}
