package com.tomgibara.storage.sample;

import java.util.List;

import org.junit.Test;

import com.tomgibara.bits.BitStore;
import com.tomgibara.storage.Storage;
import com.tomgibara.storage.Store;
import com.tomgibara.storage.StoreType;
import com.tomgibara.storage.Stores;

public class SamplesTest {

	class T {}

	int size = 100;
	int newSize = 200;
	Storage<T> storage = StoreType.<T>generic().storage();
	int i = 0;
	int j = 1;
	T value = new T();
	Object obj = new Object();

	@SuppressWarnings("unused")
	@Test
	public void testSamples() {

		// CREATING NEW STORES

		/* Creating generic array storage backed by Object arrays. */
		Store<T> ex1 = StoreType.<T>generic().storage().newStore(size);

		/* Creating storage backed by primitive arrays. */
		Store<Integer> ex2 = StoreType.of(int.class).storage().newStore(size);

		/* Creating storage backed by String arrays
		   in which nulls are replaced by empty string. */
		Store<String> ex3 = StoreType.of(String.class).settingNullToValue("").storage().newStore(size);

		/* Creating storage backed by weak references. */
		Store<T> ex4 = Storage.<T>weak().newStore(size);

		/* Creating storage backed by soft references. */
		Store<T> ex5 = Storage.<T>soft().newStore(size);

		/* Creating storage for ints in the range [0,4]. */
		Store<Integer> ex6 = StoreType.of(int.class).smallValueStorage(5).newStore(size, 0);


		// WRAPPING EXISTING ARRAYS

		/* Wrapping a primitive array as a store. */
		Store<Integer> ex7 = Stores.ints(2,3,5,7,11);

		/* Wrapping an object array permitting null values. */
		Store<Object> ex8 = Stores.objects(new Object[size]);

		/* Wrapping varargs as an immutable store. */
		Store<String> ex9 = StoreType.of(String.class)
				.objectsAsStore("A", "B", "C");
		
		/* Wrapping a primitive array as a store allowing nulls. */
		Store<Double> exa = StoreType.of(double.class)
				.arrayAsStore(new double[] {1.0,2.0,3.0});

		/* Wrapping an object array, not permitting null values. */
		Store<String> exb = StoreType.of(String.class).settingNullDisallowed()
				.arrayAsStore(new String[] {"Zippy", "Bungle", "George"});


		// BASIC STORE FUNCTIONS

		ex1.set(i, value);        // set a value
		ex1.get(i);               // get a value
		ex1.isNull(i);            // check if a value is null
		ex1.transpose(i, j);      // swap two values
		ex1.clear();              // clear all values
		ex1.fill(value);          // change all values
		ex1.count();              // number of non-null values
		ex1.iterator();           // iterate over all non-null values
		ex1.forEach(t -> {});     // act over all non-null values
		ex1.resizedCopy(newSize); // create a resized copy
		ex1.compact();            // gather all non-null values
		ex1.isSettable(obj);      // check whether a value may be set


		// ADDITIONAL STORE FUNCTIONS

		/* The positions of all non-null values in the store, as a bit store. */
		BitStore population = ex2.population();

		/* An immutable view over the store. */
		Store<String> view = ex3.immutableView();

		/* Copy a store into alternative storage. */
		Store<T> copy = ex4.copiedBy(storage);

		/* A store as a list. */
		List<T> list = ex5.asList();

		/* Transforming a store with a function */
		Store<Integer> store = ex6.asTransformedBy(i -> 2*i);


		// IDIOMATIC USAGE

		/* 1 */ StoreType.of(String.class)
		/* 2 */ .settingNullToValue("")
		/* 3 */ .storage()
		/* 4 */ .newStore(size);
	}

}
