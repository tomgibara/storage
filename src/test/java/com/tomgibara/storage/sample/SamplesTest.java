package com.tomgibara.storage.sample;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.tomgibara.bits.BitStore;
import com.tomgibara.storage.Storage;
import com.tomgibara.storage.Store;
import com.tomgibara.storage.Stores;

public class SamplesTest {

	class T {}

	int size = 100;
	int newSize = 200;
	boolean nullsAllowed = false;
	int i = 0;
	int j = 1;
	T value = new T();

	@SuppressWarnings("unused")
	@Test
	public void testSamples() {

		// CREATING NEW STORES

		/* Creating generic array storage backed by Object arrays. */
		Store<T> ex1 = Storage.<T>generic().newStore(size);

		/* Creating storage backed by primitive arrays. */
		Store<Integer> ex2 = Storage.typed(int.class).newStore(size);

		/* Creating storage backed by String arrays
		   in which nulls are replaced by empty string. */
		Store<String> ex3 = Storage.typed(String.class, "").newStore(size);

		/* Creating storage backed by weak references. */
		Store<T> ex4 = Storage.<T>weak().newStore(size);

		/* Creating storage backed by soft references. */
		Store<T> ex5 = Storage.<T>soft().newStore(size);

		/* Creating storage for ints in the range [0,4]. */
		Store<Integer> ex6 = Storage.smallValues(5, nullsAllowed).newStore(size);


		// WRAPPING EXISTING ARRAYS

		/* Wrapping a primitive array as a store. */
		Store<Integer> ex7 = Stores.ints(2,3,5,7,11);

		/* Wrapping a primitive array as a store,
		   adding support for null values. */
		Store<Double> ex8 = Stores.doublesAndNull(1.0,2.0,3.0);

		/* Wrapping an object array permitting null values. */
		Store<Object> ex9 = Stores.objects(Optional.empty(), new Object[size]);

		/* Wrapping an object array, not permitting null values. */
		Store<String> exa = Stores.objects(Optional.of(""), "Zippy", "Bungle", "George");


		// BASIC STORE FUNCTIONS

		ex1.set(i, value);    // set a value
		ex1.get(i);           // get a value
		ex1.isNull(i);        // check if a value is null
		ex1.transpose(i, j);  // swap two values
		ex1.clear();          // clear all values
		ex1.fill(value);      // change all values
		ex1.iterator();       // iterate over all non-null values
		ex1.forEach(t -> {}); // act over all non-null values
		ex1.compact();       // gather all non-null values


		// ADDITIONAL STORE FUNCTIONS

		/* A count of the number non-null elements in the store. */
		int count = ex2.count();

		/* The positions of all non-null values in the store, as a bit store. */
		BitStore population = ex3.population();

		/* An immutable view over the store. */
		Store<T> view = ex4.immutableView();

		/* A resized copy of an existing store. */
		Store<T> copy = ex5.resizedCopy(newSize);

		/* A store as a list. */
		List<Integer> list = ex6.asList();

		/* Transforming a store with a function */
		Store<Integer> store = ex7.asTransformedBy(i -> 2*i);

	}

}
