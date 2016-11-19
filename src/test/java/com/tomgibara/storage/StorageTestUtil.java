/*
 * Copyright 2016 Tom Gibara
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

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import com.tomgibara.fundament.Mapping;
import com.tomgibara.fundament.Producer;

public class StorageTestUtil {

	private static final Store<Class<?>> allClasses;
	static {
		allClasses = Stores.objects(
			byte.class,
			short.class,
			int.class,
			long.class,
			boolean.class,
			char.class,
			float.class,
			double.class,
			Byte.class,
			Short.class,
			Integer.class,
			Long.class,
			Boolean.class,
			Character.class,
			Float.class,
			Double.class,
			String.class,
			Object.class
			).immutable();
	}

	private static final Class<Class<?>> t1 = (Class<Class<?>>)(Class) Class.class;
	private static final Class<StoreType<?>> t2 = (Class<StoreType<?>>)(Class)StoreType.class;
	public static final Store<StoreType<?>> nullTypes = allClasses.asTransformedBy(Mapping.fromFunction(t1, t2, c -> StoreType.of(c).settingNullAllowed()));
	public static final Store<StoreType<?>> nonNullTypes = allClasses.asTransformedBy(Mapping.fromFunction(t1, t2, c -> StoreType.of(c).settingNullDisallowed()));

	public static void forAllTypes(Consumer<StoreType<?>> test) {
		nullTypes.forEach(test);
		nonNullTypes.forEach(test);
	}

	public static <V> Function<StoreType<V>, Store<V>> randomStores(Random r) {
		return t -> {
			int size = r.nextInt(100);
			Producer<V> p = produce(t.valueType(), r);
			Store<V> store= t.storage().newStore(size, defaultValue(t));
			for (int i = 0; i < size; i++) {
				store.set(i, p.produce());
			}
			nullify(store, r);
			if (r.nextBoolean()) {
				int to = r.nextInt(store.size() + 1);
				int from = r.nextInt(to + 1);
				store = store.range(from, to);
			}
			return store;
		};
	}

	public static Producer<Store<Integer>> randomSmallValueStores(Random r, int range, boolean nullGettable) {
		StoreType<Integer> type = StoreType.of(int.class);
		StoreType<Integer> t = nullGettable ? type.settingNullAllowed() : type.settingNullDisallowed();
		return () -> {
			int size = r.nextInt(100);
			Producer<Integer> p = () -> Integer.valueOf(r.nextInt(range));
			Store<Integer> store = t.smallValueStorage(range).newStore(size, 0);
			for (int i = 0; i < size; i++) {
				store.set(i, p.produce());
			}
			nullify(store, r);
			if (r.nextBoolean()) {
				int to = r.nextInt(store.size() + 1);
				int from = r.nextInt(to + 1);
				store = store.range(from, to);
			}
			return store;
		};
	}

	private static <V> V defaultValue(StoreType<V> type) {
		V nv = type.settingNullToDefault().nullValue;
		if (nv != null) return nv;
		if (type.valueType() == Object.class) return (V) new Object();
		throw new RuntimeException("Unsupported type: " + type);
	}

	private static <V> Producer<V> produce(Class<V> clss, Random r) {
		if (clss == byte.class || clss == Byte.class) return () -> (V) Byte.valueOf((byte) r.nextInt());
		if (clss == short.class || clss == Short.class) return () -> (V) Short.valueOf((short) r.nextInt());
		if (clss == int.class || clss == Integer.class) return () -> (V) Integer.valueOf(r.nextInt());
		if (clss == long.class || clss == Long.class) return () -> (V) Long.valueOf(r.nextLong());
		if (clss == boolean.class || clss == Boolean.class) return () -> (V) Boolean.valueOf(r.nextBoolean());
		if (clss == char.class || clss == Character.class) return () -> (V) Character.valueOf((char) r.nextInt());
		if (clss == float.class || clss == Float.class) return () -> (V) Float.valueOf(r.nextFloat());
		if (clss == double.class || clss == Double.class) return () -> (V) Double.valueOf(r.nextDouble());
		if (clss == String.class) return () -> (V) Integer.toHexString(r.nextInt());
		if (clss == Object.class) return () -> (V) new Object();
		throw new RuntimeException("Unsupported type: " + clss.getName());
	}

	private static void nullify(Store<?> store, Random r) {
		if (store.type().nullGettable()) {
			for (int i = 0; i < store.size(); i++) {
				if (r.nextBoolean()) store.set(i, null);
			}
		}
	}
}
