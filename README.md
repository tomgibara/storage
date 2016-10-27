Storage
=======

Provides array-like storage that can serve as the basis for building more
complex collection types in Java.

Overview
--------

### Abstractions

The abstractions provide by this small library are:

* `Store`         integer-indexed storage of data
* `Storage`       creates stores of required capacity
* `StoreNullity`  controls null handling in stores

Additionally, the following helper classes are also available

* `AstractStore` a convenient base-class for creating new implementations
* `Stores` a set of static methods that wrap arrays into stores.

All classes are found in the `com.tomgibara.storage` package, with full
documentation available via the javadocs packaged with the release. These can
be browsed online at
[javadoc.io](http://www.javadoc.io/doc/com.tomgibara.storage/storage).

### Features

In addition to setting and getting values at an index, the `Store` abstraction
provides, among other things, the following features:

* Every store has a fixed `size()` that is its capacity, in addition to which
  it exposes a `count()` of the number of non-null values it contains.
* Stores may vary in their support for null values, this is indicated by their
  `nullity()`.
* The indices of the non-null values in a store are exposed as a bit store using
  the method `population()`.
* Stores are `Iterable` and iteration is over the non-null values only.
* Stores can be treated as lists via their `asList()` method.
* All stores provide mutability control via the interface:
  `com.tomgibara.fundament.Mutability`
* Stores provide a convenient `resizedCopy()` method for convenient resizing
  when used as a backing-store.

### Implementations

The storage types currently provided by this package are:

* Genericized storage - backed by object arrays
* Typed storage - backed by typed arrays, including support for primitive types
  and enumerations
* Weak/soft storage - generic storage of object held by weak/strong references
* Small value storage - efficiently stores small int values using bit packing

Examples
--------

### Creating new stores

```java
/* Creating generic array storage backed by Object arrays. */
Store<T> ex1 = Storage.<T>generic().newStore(size);

/* Creating storage backed by primitive arrays. */
Store<Integer> ex2 = Storage.typed(int.class).newStore(size);

/* Creating storage backed by String arrays
   in which nulls are replaced by empty string. */
Store<String> ex3 = Storage.typed(String.class, StoreNullity.settingNullToValue("")).newStore(size);

/* Creating storage backed by weak references. */
Store<T> ex4 = Storage.<T>weak().newStore(size);

/* Creating storage backed by soft references. */
Store<T> ex5 = Storage.<T>soft().newStore(size);

/* Creating storage for ints in the range [0,4]. */
Store<Integer> ex6 = Storage.smallValues(5, nullsAllowed).newStore(size);
```

### Wrapping existing arrays

```java
/* Wrapping a primitive array as a store. */
Store<Integer> ex7 = Stores.ints(2,3,5,7,11);

/* Wrapping a primitive array as a store,
   adding support for null values. */
Store<Double> ex8 = Stores.doublesWithNullity(StoreNullity.settingNullAllowed(), 1.0,2.0,3.0);

/* Wrapping an object array permitting null values. */
Store<Object> ex9 = Stores.objects(new Object[size]);

/* Wrapping an object array, not permitting null values. */
Store<String> exa = Stores.objectsWithNullity(StoreNullity.settingNullDisallowed(), "Zippy", "Bungle", "George");
```

### Basic store functions

```java
ex1.set(i, value);        // set a value
ex1.get(i);               // get a value
ex1.isNull(i);            // check if a value is null
ex1.transpose(i, j);      // swap two values
ex1.clear();              // clear all values
ex1.fill(value);          // change all values
ex1.iterator();           // iterate over all non-null values
ex1.forEach(t -> {});     // act over all non-null values
ex1.resizedCopy(newSize); // create a resized copy
ex1.compact();            // gather all non-null values
```

### Additional store functions

```java
/* A count of the number non-null elements in the store. */
int count = ex2.count();

/* The positions of all non-null values in the store, as a bit store. */
BitStore population = ex3.population();

/* An immutable view over the store. */
Store<T> view = ex4.immutableView();

/* Copy a store into alternative storage. */ 
Store<T> copy = ex5.copiedBy(storage);

/* A store as a list. */
List<Integer> list = ex6.asList();

/* Transforming a store with a function */
Store<Integer> store = ex7.asTransformedBy(i -> 2*i);
```


```java
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
```


Usage
-----

The storage library will be available from the Maven central repository:

> Group ID:    `com.tomgibara.storage`
> Artifact ID: `storage`
> Version:     `1.0.0`

The Maven dependency to be:

    <dependency>
      <groupId>com.tomgibara.storage</groupId>
      <artifactId>storage</artifactId>
      <version>1.0.0</version>
    </dependency>

Release History
---------------

*not yet released*
