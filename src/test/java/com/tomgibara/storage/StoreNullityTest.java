package com.tomgibara.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StoreNullityTest {

	enum EmptyEnum { }

	enum NonEmptyEnum { DOG, CAT; }

	@Test
	public void testEnumDefault() {
		assertEquals(NonEmptyEnum.DOG, StoreNullity.defaultForType(NonEmptyEnum.class).nullValue());
		assertTrue(StoreNullity.defaultForType(EmptyEnum.class).nullSettable());
	}
}
