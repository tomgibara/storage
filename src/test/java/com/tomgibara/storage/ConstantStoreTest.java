package com.tomgibara.storage;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConstantStoreTest {

	@Test
	public void testCopying() {
		testCopying(StoreType.of(Integer.class));
		testCopying(StoreType.of(int.class));
		testCopying(StoreType.of(int.class).settingNullToValue(1));
		testCopying(StoreType.of(int.class).settingNullDisallowed());
	}

	private void testCopying(StoreType<Integer> type) {
		Store<Integer> store = type.constantStore(1, 5);
		assertEquals(5, store.size());
		assertEquals(5, store.count());
		for (int i = 0; i < 5; i++) {
			assertEquals(1, store.get(0).intValue());
		}
		Store<Integer> copy = type.storage().newCopyOf(store);
		assertEquals(copy, store);
		assertEquals(store, copy);
		assertEquals(copy.toString(), store.toString());
		assertEquals(copy.hashCode(), store.hashCode());

		assertEquals(copy.resizedCopy(0), store.resizedCopy(0));
		assertEquals(copy.resizedCopy(5), store.resizedCopy(5));
		if (store.type().nullSettable()) assertEquals(copy.resizedCopy(10), store.resizedCopy(10));

		assertEquals(copy.range(0, 2), store.range(0, 2));
		assertEquals(copy.range(1, 2), store.range(1, 2));
		assertEquals(copy.range(2, 2), store.range(2, 2));

		Store<Integer> a = store.copiedBy(type.storage().mutable());
		Store<Integer> b = store.copiedBy(type.storage().immutable());
		assertEquals(store, a);
		assertEquals(store, b);
	}
}
