Storage
=======

Provides array-like storage that can serve as the basis for building more
complex collection types in Java.

Overview
--------

### Abstractions

The abstractions provide by this library are:

* `Store`      integer-indexed storage of data
* `Storage`    creates stores of required capacity
* `StoreType`  controls the types of values that can be stored

Additionally, the following helper classes are also available

* `AstractStore` a convenient base-class for creating new implementations
* `Stores` a set of static methods that directly wrap arrays into stores.

All classes are found in the `com.tomgibara.storage` package, with full
documentation available via the javadocs packaged with the release. These can
be browsed online at
[javadoc.io](http://www.javadoc.io/doc/com.tomgibara.storage/storage).

### Benefits

Most applications will benefit from using this library's `Store` type as a
low-level data-structure to replace arrays when constructing higher-level
data-structures. It is possible that using stores over arrays will incur a
performance overhead, but this can be set aside some of the benefits:

* Differences between primitive and object based arrays are hidden behind a
  unified abstraction.
* Substantially reduced memory usage for primitive backed storage; even
  when that storage is configured to support null values, and is thus
  indistinguishable from object array backed storage.
* Greater type-safety since storage is generally be backed by typed arrays, with
  this type made explicit via the `StoreType`.
* Explicit mutability control provides for immutable views of stores in a way
  that is impossible with Java arrays.

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

### Idiom

The general pattern of use is in three steps:

1. Create a `StoreType` to specify the stored value type.
2. Optionally configure how null values should be supported.
3. Obtain the `Storage` for the type (often caching a reference to it).
4. Create new `Store` instances as needed.

```java
/* 1 */ StoreType.of(String.class)
/* 2 */ .settingNullToValue("")
/* 3 */ .storage()
/* 4 */ .newStore(size);
```

Examples
--------

### Creating new stores

```java
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
```

### Wrapping existing arrays

```java
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
```

### Basic store functions

```java
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
```

### Additional store functions

```java
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

/* A 'slice' of a store */
ex7.range(from, to);
```


Usage
-----

The storage library is available from the Maven central repository:

> Group ID:    `com.tomgibara.storage`
> Artifact ID: `storage`
> Version:     `1.0.0`

The Maven dependency being:

    <dependency>
      <groupId>com.tomgibara.storage</groupId>
      <artifactId>storage</artifactId>
      <version>1.0.0</version>
    </dependency>

Release History
---------------

**2016.11.19** Version 1.0.0

 Initial release
