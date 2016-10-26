package com.tomgibara.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

	@Test
	public void testConstructors() {
		assertTrue(StoreNullity.settingNullAllowed().nullSettable());
		assertTrue(StoreNullity.settingNullAllowed().nullGettable());
		assertFalse(StoreNullity.settingNullDisallowed().nullSettable());
		assertFalse(StoreNullity.settingNullDisallowed().nullGettable());
		assertTrue(StoreNullity.settingNullToValue("").nullSettable());
		assertFalse(StoreNullity.settingNullToValue("").nullGettable());
		
	}
}
