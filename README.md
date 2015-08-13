Storage
=======

Provides array-like storage that can serve as the basis for building more
complex collection types in Java.

Overview
--------

The abstractions provide by this small library are:

* `Mutability` provides a standard interface for controlling mutability
* `Store`      integer-indexed storage of non-null values
* `Storage`    creates stores of required capacity

All three interfaces are found in the `com.tomgibara.storage` package with full
documentation is available via the javadocs packaged with the release.

The storage types provided by this package are:

* Genericized storage - backed by object arrays
* Typed storage - backed by typed arrays, including support for primitive types
* Weak/soft storage - generic storage of object held by weak/strong references

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
