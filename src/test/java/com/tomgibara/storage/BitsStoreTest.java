/*
 * Copyright 2017 Tom Gibara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.tomgibara.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.UnaryOperator;

import org.junit.Test;

import com.tomgibara.bits.BitStore;
import com.tomgibara.bits.Bits;
import com.tomgibara.fundament.Bijection;

public class BitsStoreTest {

	@Test
	public void testTransformedBy() {
		BitStore bits = Bits.store(2);
		Store<Boolean> s = Stores.bits(bits);

		UnaryOperator<Boolean> id = b -> b;
		Store<Boolean> tid = s.asTransformedBy(id);
		assertEquals(s, tid);

		UnaryOperator<Boolean> xor = b -> !b;
		Store<Boolean> txor = s.asTransformedBy(xor);
		assertTrue(txor.get(0));
		assertTrue(txor.get(1));
		txor.set(0, false);
		assertTrue(bits.getBit(0));
		txor.set(0, true);
		assertFalse(bits.getBit(0));

		UnaryOperator<Boolean> ones = b -> true;
		Store<Boolean> tones = s.asTransformedBy(ones);
		tones.forEach(b -> assertTrue(b));
		try {
			tones.set(0, false);
			fail("set on non-bijective transform");
		} catch (IllegalStateException e) {
			// expected
		}

		Bijection<Boolean, Integer> a = Bijection.fromFunctions(boolean.class, int.class, b -> b ? 1 : 0, i -> i == 1);
		Store<Integer> ta = s.asTransformedBy(a);
		assertEquals(Stores.ints(0,0), ta);
		ta.set(0, 1);
		assertEquals(Stores.ints(1,0), ta);
		assertEquals(Bits.toStore("01"), bits);
		ta.set(0, 0);

	}
}
