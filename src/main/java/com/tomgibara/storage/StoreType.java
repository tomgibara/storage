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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import com.tomgibara.fundament.Mapping;
import com.tomgibara.storage.NullPrimitiveStore.BooleanStore;
import com.tomgibara.storage.NullPrimitiveStore.ByteStore;
import com.tomgibara.storage.NullPrimitiveStore.CharacterStore;
import com.tomgibara.storage.NullPrimitiveStore.DoubleStore;
import com.tomgibara.storage.NullPrimitiveStore.FloatStore;
import com.tomgibara.storage.NullPrimitiveStore.IntegerStore;
import com.tomgibara.storage.NullPrimitiveStore.LongStore;
import com.tomgibara.storage.NullPrimitiveStore.ShortStore;

/**
 * <p>
 * Instances of this class determine the underlying type of values in storage,
 * and control how null values are handled by stores. They serve as the primary
 * means by which {@link Storage} implementations are obtained; typically via
 * the {@link #storage()} method.
 *
 * <p>
 * Creating a {@link StoreType} instance begins by calling {@link #of(Class)}
 * with an explicit storage type, or {@link #generic()} if the type is not known
 * concretely (typically due to erasure). In addition to specifying the stored
 * value type, alternative instances can be obtained that control how nulls are
 * to be supported by the storage. Unless otherwise specified, a
 * {@link StoreType} will allow setting and getting null values.
 *
 * <p>
 * A {@link StoreType} also provides a number of convenient methods for creating
 * stores directly, without the use of an intervening {@link Storage} object.
 *
 * @param <V>
 *            the type of values to be stored
 *
 * @author Tom Gibara
 */

public final class StoreType<V> {

	// private statics

	// package statics

	static final StoreType<Object>    OBJECT     = new StoreType<>(Object.class,  true,  true,  null);

	static final StoreType<Byte>      BYTE        = new StoreType<>(byte.class,    true,  true,  null);
	static final StoreType<Short>     SHORT       = new StoreType<>(short.class,   true,  true,  null);
	static final StoreType<Integer>   INT         = new StoreType<>(int.class,     true,  true,  null);
	static final StoreType<Long>      LONG        = new StoreType<>(long.class,    true,  true,  null);
	static final StoreType<Boolean>   BOOLEAN     = new StoreType<>(boolean.class, true,  true,  null);
	static final StoreType<Character> CHAR        = new StoreType<>(char.class,    true,  true,  null);
	static final StoreType<Float>     FLOAT       = new StoreType<>(float.class,   true,  true,  null);
	static final StoreType<Double>    DOUBLE      = new StoreType<>(double.class,  true,  true,  null);

	static final StoreType<Byte>      BYTE_NN     = BYTE   .settingNullDisallowed();
	static final StoreType<Short>     SHORT_NN    = SHORT  .settingNullDisallowed();
	static final StoreType<Integer>   INT_NN      = INT    .settingNullDisallowed();
	static final StoreType<Long>      LONG_NN     = LONG   .settingNullDisallowed();
	static final StoreType<Boolean>   BOOLEAN_NN  = BOOLEAN.settingNullDisallowed();
	static final StoreType<Character> CHAR_NN     = CHAR   .settingNullDisallowed();
	static final StoreType<Float>     FLOAT_NN    = FLOAT  .settingNullDisallowed();
	static final StoreType<Double>    DOUBLE_NN   = DOUBLE .settingNullDisallowed();

	static final StoreType<Byte>      BYTE_WR     = new StoreType<>(Byte.class,      true,  true,  null);
	static final StoreType<Short>     SHORT_WR    = new StoreType<>(Short.class,     true,  true,  null);
	static final StoreType<Integer>   INT_WR      = new StoreType<>(Integer.class,   true,  true,  null);
	static final StoreType<Long>      LONG_WR     = new StoreType<>(Long.class,      true,  true,  null);
	static final StoreType<Boolean>   BOOLEAN_WR  = new StoreType<>(Boolean.class,   true,  true,  null);
	static final StoreType<Character> CHAR_WR     = new StoreType<>(Character.class, true,  true,  null);
	static final StoreType<Float>     FLOAT_WR    = new StoreType<>(Float.class,     true,  true,  null);
	static final StoreType<Double>    DOUBLE_WR   = new StoreType<>(Double.class,    true,  true,  null);

	static final StoreType<Byte>      BYTE_DEF    = BYTE_WR   .settingNullToValue((byte)  0   );
	static final StoreType<Short>     SHORT_DEF   = SHORT_WR  .settingNullToValue((short) 0   );
	static final StoreType<Integer>   INT_DEF     = INT_WR    .settingNullToValue(        0   );
	static final StoreType<Long>      LONG_DEF    = LONG_WR   .settingNullToValue(        0L  );
	static final StoreType<Boolean>   BOOLEAN_DEF = BOOLEAN_WR.settingNullToValue(       false);
	static final StoreType<Character> CHAR_DEF    = CHAR_WR   .settingNullToValue(      '\0'  );
	static final StoreType<Float>     FLOAT_DEF   = FLOAT_WR  .settingNullToValue(        0.0f);
	static final StoreType<Double>    DOUBLE_DEF  = DOUBLE_WR .settingNullToValue(        0.0 );

	static final StoreType<String>     STRING_DEF   = new StoreType<>(String.class,     true, false, ""             );
	static final StoreType<BigInteger> BIGINT_DEF   = new StoreType<>(BigInteger.class, true, false, BigInteger.ZERO);
	static final StoreType<BigDecimal> DECIMAL_DEF  = new StoreType<>(BigDecimal.class, true, false, BigDecimal.ZERO);

	private static final StoreType<?>[] PRIMITIVES = new StoreType[16];
	static {
		PRIMITIVES[Stores.BYTE]    = BYTE;
		PRIMITIVES[Stores.SHORT]   = SHORT;
		PRIMITIVES[Stores.INT]     = INT;
		PRIMITIVES[Stores.LONG]    = LONG;
		PRIMITIVES[Stores.BOOLEAN] = BOOLEAN;
		PRIMITIVES[Stores.CHAR]    = CHAR;
		PRIMITIVES[Stores.FLOAT]   = FLOAT;
		PRIMITIVES[Stores.DOUBLE]  = DOUBLE;
	}

	private static final StoreType<?>[] DEFAULTS = new StoreType[16];
	static {
		DEFAULTS[Stores.BYTE]    = BYTE   .settingNullToValue((byte)  0   );
		DEFAULTS[Stores.SHORT]   = SHORT  .settingNullToValue((short) 0   );
		DEFAULTS[Stores.INT]     = INT    .settingNullToValue(        0   );
		DEFAULTS[Stores.LONG]    = LONG   .settingNullToValue(        0L  );
		DEFAULTS[Stores.BOOLEAN] = BOOLEAN.settingNullToValue(     false  );
		DEFAULTS[Stores.CHAR]    = CHAR   .settingNullToValue(      '\0'  );
		DEFAULTS[Stores.FLOAT]   = FLOAT  .settingNullToValue(        0.0f);
		DEFAULTS[Stores.DOUBLE]  = DOUBLE .settingNullToValue(        0.0 );
	}

	static void failNull() {
		throw new IllegalArgumentException("null value");
	}

	// public statics

	/**
	 * Specifies genericized storage backed by <code>Object</code> arrays.
	 * The type returned by this method supports getting and setting null values.
	 * Using {@link #of(Class)} with a precise type is to be preferred where
	 * possible.
	 *
	 * @param <V>
	 *            the ostensible value type
	 * @return a generic type
	 * @see #of(Class)
	 */
	//TODO this is pretty smelly
	@SuppressWarnings("unchecked")
	public static <V> StoreType<V> generic() {
		return (StoreType<V>) OBJECT;
	}

	/**
	 * Specifies that values are to be stored using the supplied type. Where the
	 * precise type of the stored values is unknown (typically due to type
	 * erasure) the {@link #generic()} method can be used.
	 *
	 * @param <V>
	 *            the value type
	 * @param valueType
	 *            the type of value stored
	 * @return a store type
	 * @see #generic()
	 */
	@SuppressWarnings("unchecked")
	public static <V> StoreType<V> of(Class<V> valueType) {
		if (valueType == null) throw new IllegalArgumentException("null valueType");
		if (valueType == Object.class) return generic();
		if (valueType.isPrimitive()) return (StoreType<V>) PRIMITIVES[Stores.hash(valueType)];
		return new StoreType<V>(valueType, true, true, null);
	}

	final Class<V> valueType;
	final boolean nullSettable;
	final boolean nullGettable;
	final V nullValue;
	// lazily generated
	private Storage<V> storage = null;

	private StoreType(Class<V> valueType, boolean nullSettable, boolean nullGettable, V nullValue) {
		this.valueType = valueType;
		this.nullSettable = nullSettable;
		this.nullGettable = nullGettable;
		this.nullValue = nullValue;
	}

	// builders

	/**
	 * A type that shares the same {@link #valueType()} but which permits nulls
	 * to be stored and retrieved; both {@link #nullSettable()} and
	 * {@link #nullGettable()} return true.
	 *
	 * @return a store type that permits null values
	 */
	public StoreType<V> settingNullAllowed() {
		return nullSettable & nullGettable ? this : new StoreType<>(valueType, true, true, null);
	}

	/**
	 * A type that shares the same {@link #valueType()} but which does not
	 * permit nulls to be stored or retrieved; both {@link #nullSettable()} and
	 * {@link #nullGettable()} return false.
	 *
	 * @return a store type that prohibits null values
	 */
	public StoreType<V> settingNullDisallowed() {
		return !nullSettable && !nullGettable ? this : new StoreType<>(valueType, false, false, null);
	}

	/**
	 * <p>
	 * A type that shares the same {@link #valueType()} but which substitutes
	 * nulls with the specified value. The returned type allows nulls to be set
	 * on a store ({@link #nullSettable()} returns true) but does not allow
	 * nulls to be returned from it ( {@link #nullGettable()} returns false).
	 *
	 * <p>
	 * The exception to this is when a null value is supplied to the method
	 * (indicating that nulls should not be substituted). In this case, the
	 * method returns a type which is equivalent to that returned by calling
	 * {@link #settingNullAllowed()} on this type.
	 *
	 * @param value
	 *            the value to be substituted for null
	 * @return a store type that substitutes nulls
	 */
	public StoreType<V> settingNullToValue(V value) {
		return value == null ? settingNullAllowed() : new StoreType<>(valueType, true, false, value);
	}

	/**
	 * <p>
	 * Suggests a store type that substitutes null for a given defined range of
	 * value types. This method may be used to easily configure a conventional
	 * null substitution value for stores of common types.
	 *
	 * <p>
	 * It returns {@link #settingNullToValue(Object)} with the following values
	 * for the types given.
	 *
	 * <dl>
	 * <dt>primitive numeric types
	 * <dd><code>0</code>
	 *
	 * <dt><code>boolean</code>
	 * <dd><code>false</code>
	 *
	 * <dt><code>char</code>
	 * <dd><code>'\0'</code>
	 *
	 * <dt>primitive wrapper types
	 * <dd><em>as per primitive types</em></dd>
	 *
	 * <dt>enumerations
	 * <dd>the enum constant with ordinal 0 (if it exists)
	 *
	 * <dt><code>java.lang.String</code>
	 * <dd>the empty string <code>""</code>
	 *
	 * <dt><code>java.math.BigInteger</code>
	 * <dd><code>BigInteger.ZERO</code>
	 *
	 * <dt><code>java.math.BigDecimal</code>
	 * <dd><code>BigDecimal.ZERO</code>
	 * </dl>
	 *
	 * <p>
	 * For all other types, the method returns {@link #settingNullDisallowed()}.
	 * Future implementations may return null-replacing instances for a greater
	 * number of types.
	 *
	 * @return a type that defaults null values, or that disallows them
	 */
	@SuppressWarnings("unchecked")
	public StoreType<V> settingNullToDefault() {

		if (valueType.isPrimitive()) {
			return (StoreType<V>) DEFAULTS[ Stores.hash(valueType) ];
		}

		if (valueType.isEnum()) {
			V[] constants = valueType.getEnumConstants();
			return constants.length == 0 ? settingNullDisallowed() : settingNullToValue((V) constants[0]);
		}

		if (!valueType.isInterface()) {
			switch (valueType.getName()) {
			case "java.lang.Boolean"    : return (StoreType<V>) BOOLEAN_DEF;
			case "java.lang.Character"  : return (StoreType<V>) CHAR_DEF;
			case "java.lang.Float"      : return (StoreType<V>) FLOAT_DEF;
			case "java.lang.Double"     : return (StoreType<V>) DOUBLE_DEF;
			case "java.lang.Byte"       : return (StoreType<V>) BYTE_DEF;
			case "java.lang.Short"      : return (StoreType<V>) SHORT_DEF;
			case "java.lang.Integer"    : return (StoreType<V>) INT_DEF;
			case "java.lang.Long"       : return (StoreType<V>) LONG_DEF;

			case "java.lang.String"     : return (StoreType<V>) STRING_DEF;
			case "java.math.BigInteger" : return (StoreType<V>) BIGINT_DEF;
			case "java.math.BigDecimal" : return (StoreType<V>) DECIMAL_DEF;
			}
		}

		return settingNullDisallowed();
	}

	// accessors

	public Class<V> valueType() {
		return valueType;
	}

	/**
	 * Whether null values may be set on on stores. Some stores can store null
	 * values and some stores substitute nulls with a default value, but some
	 * stores will raise an <code>IllegalArgumentException</code> if any attempt
	 * is made to introduce a null value into the store. A false value returned
	 * from this method indicates the latter.
	 *
	 * @return true if null values may be set on the store, false if not
	 * @see Store#set(int, Object)
	 */
	public boolean nullSettable() {
		return nullSettable;
	}

	/**
	 * Whether null values may be returned from the store. Some stores can store
	 * null values and some stores substitute nulls with a default value and
	 * other stores will raise an exception if any attempt is made to introduce
	 * a null value into the store. A true value returned from this method
	 * indicates the former.
	 *
	 * @return true if it is possible to get null values from the store, false
	 *         if not
	 * @see Store#get(int)
	 */
	public boolean nullGettable() {
		return nullGettable;
	}

	/**
	 * <p>
	 * Returns the value passed to {@link #settingNullToValue(Object)}, or null.
	 * This value will replace null in any operation that attempts to introduce
	 * nulls into a store of this type.
	 *
	 * <p>
	 * Note that any non-null value returned by this method will by substituted
	 * for null in calls to {@link Store#set(int, Object)} and
	 * {@link Store#clear()} but occurrences of the value in the store will not
	 * be reported as null.
	 *
	 * @return the value substituted for null, or null if no substitution is
	 *         performed
	 * @see #settingNullToValue(Object)
	 */
	public V nullValue() {
		return nullValue;
	}

	// storage

	/**
	 * <p>
	 * Storage backed by typed arrays, or by <code>Object</code> arrays in the
	 * case of a generic type. The storage returned by this method supports
	 * setting and getting null values as per the this type.
	 *
	 * <p>
	 * The use of primitive value types will result in storage backed by arrays
	 * of primitives. Such stores provide greater type safety than those created
	 * by genericized storage. In many contexts they will also provide a very
	 * significant reduction in the memory required to store values.
	 *
	 * <p>
	 * Specific support is also provided for enumeration types. These are stored
	 * as small value integers yielding a commensurate reduction in memory usage
	 * at the possible expense of slower operation times. To bypass this, use a
	 * {@link #generic()} type.
	 *
	 * <p>
	 * Note that storage returned by this method is always mutable. To obtain
	 * storage that directly constructs immutable stores, make a further call
	 * to {@link Storage#immutable()}
	 *
	 * @return the basis of creating new stores
	 * @see #generic()
	 * @see #of(Class)
	 */
	public Storage<V> storage() {
		return storage == null ? storage = createStorage() : storage;
	}

	/**
	 * <p>
	 * Storage that packs bounded non-negative integer values into minimal bit
	 * sizes. Such storage may be useful in situations where a very large number
	 * of small integer values need to be stored without occupying more memory
	 * than is needed. The range must be less than
	 * <code>Integer.MAX_VALUE</code>
	 *
	 * <p>
	 * Generally values in a range are packed linearly using the least number of
	 * bits needed to represent them individually. However, in the present
	 * implementation, ternary values ([0,1,2] or [null, 0,1]) and quinary
	 * values ([0,1,2,3,4] or [null, 0, 1, 2, 3]) are treated specially to avoid
	 * underutilized memory. Ternary storage requires 8 bits for every 5 values
	 * and quinary storage requires 7 bits for every 3 values. As a result, the
	 * performance of ternary and quinary storage may degraded in some
	 * applications. In any such case, it is possible to use a larger range to
	 * switch to a regular linear bit-packing strategy.
	 *
	 * <p>
	 * This method may only be called on a type for which the
	 * {@link #valueType()} is <code>int.class</code>. In all other cases an
	 * exception will be thrown.
	 *
	 * @param range
	 *            defines the range <code>[0..range)</code> that small values
	 *            may take in this store
	 * @return small value storage
	 * @throws IllegalStateException
	 *             if the value type of this type is not <code>int.class</code>
	 */
	@SuppressWarnings("unchecked")
	public Storage<V> smallValueStorage(int range) throws IllegalStateException {
		if (range <= 0) throw new IllegalArgumentException("non positive range");
		if (range == Integer.MAX_VALUE) throw new IllegalArgumentException("range too large");
		if (valueType != int.class) throw new IllegalStateException("requires int typed store");
		return (Storage<V>) SmallValueStore.newStorage(range, (StoreType<Integer>) this);
	}

	// stores

	/**
	 * <p>
	 * An immutable empty store.
	 *
	 * <p>
	 * This method provides a convenient way to fabricate an empty store as
	 * needed.
	 *
	 * @return an immutable empty store
	 */
	public Store<V> emptyStore() {
		return new EmptyStore<>(this, false);
	}

	/**
	 * Wraps the supplied object as a singleton store, that is, a store of size
	 * 1. A null object may only be supplied to this method if
	 * {@link #nullSettable()} is true for this type.
	 *
	 * @param object
	 *            the object to be wrapped in a store
	 * @return a store containing the object
	 */

	@SuppressWarnings("unchecked")
	public Store<V> objectAsStore(V object) {
		// first deal with null value
		if (object == null) {
			if (nullGettable) return new NullSingletonStore<>(this);
			if (nullSettable) return new SingletonStore.TypedStore<>(this, nullValue);
			throw new IllegalArgumentException("null not supported for this type");
		}

		// if this type isn't a straightforward primitive type, we need the custom type reported
		if (!valueType.isPrimitive() || nullSettable) return new SingletonStore.TypedStore<>(this, object);

		// otherwise we can fallback on the very cheapest primitive wrappers
		switch(Stores.hash(valueType)) {
			case Stores.BYTE:    return (Store<V>) new SingletonStore.ByteStore    ((Byte)      object);
			case Stores.FLOAT:   return (Store<V>) new SingletonStore.FloatStore   ((Float)     object);
			case Stores.CHAR:    return (Store<V>) new SingletonStore.CharStore    ((Character) object);
			case Stores.SHORT:   return (Store<V>) new SingletonStore.ShortStore   ((Short)     object);
			case Stores.LONG:    return (Store<V>) new SingletonStore.LongStore    ((Long)      object);
			case Stores.INT:     return (Store<V>) new SingletonStore.IntStore     ((Integer)   object);
			case Stores.DOUBLE:  return (Store<V>) new SingletonStore.DoubleStore  ((Double)    object);
			case Stores.BOOLEAN: return (Store<V>) new SingletonStore.BooleanStore ((Boolean)   object);
			default: throw new IllegalArgumentException(valueType.getName());
		}
	}

	/**
	 * <p>
	 * Exposes the supplied objects as an immutable store. No copy of the
	 * supplied array is made, but the returned store cannot be modified by the
	 * caller, and thus the caller cannot modify it.
	 *
	 * <p>
	 * If {@link #nullGettable()} on this type returns false, the supplied array
	 * is not permitted to contain nulls.
	 *
	 * @param objects
	 *            the objects from which a store should be fabricated.
	 * @return the objects as a store
	 */
	public Store<V> objectsAsStore(@SuppressWarnings("unchecked") V... objects) {
		if (objects == null) throw new IllegalArgumentException("null objects");
		Class<?> clss = objects.getClass().getComponentType();
		if (clss == valueType || valueType.isAssignableFrom(clss)) return new ImmutableArrayStore<>(objects, this);
		throw new IllegalArgumentException("object type not assignable to " + valueType.getName());
	}

	/**
	 * <p>
	 * Exposes an array as a store, as per this type. The store returned is
	 * mutable and backed by the supplied array so that changes in the store
	 * will be reflected in the array. The component type of the supplied array
	 * must equal (ie. precisely match) {@link #valueType()}.
	 *
	 * <p>
	 * Note that the array is supplied as an object since primitive arrays are
	 * also supported.
	 *
	 * <p>
	 * This method provides a means by which arrays can be operated on as stores
	 * and entails no duplication of the array values.
	 *
	 * @param array
	 *            an array consistent with this type
	 * @return a mutable store backed the supplied array
	 */
	@SuppressWarnings("unchecked")
	public Store<V> arrayAsStore(Object array) {
		if (array == null) throw new IllegalArgumentException("null array");
		Class<?> clss = array.getClass().getComponentType();
		if (clss == null) throw new IllegalArgumentException("not an array");
		if (clss != valueType) throw new IllegalArgumentException("array component type does not equal " + valueType.getName());
		if (clss.isPrimitive()) {
			return nullGettable ? NullPrimitiveStore.newStore((Class<V>)clss, array) : PrimitiveStore.newStore(this, array);
		} else {
			return nullGettable ? new NullArrayStore<>((V[]) array) : new ArrayStore<>((V[]) array, this);
		}
	}

	/**
	 * Returns an immutable store consisting of <em>size</em> copies of
	 * <em>value</em>. The advantage of using this method is that memory does
	 * not need to be allocated for each instance of the value.
	 *
	 * @param value
	 *            the value to be stored, possibly null
	 * @param size
	 *            the number of copies stored, possibly zero
	 * @return a store consisting of multiple copies of a single value
	 */
	public Store<V> constantStore(V value, int size) {
		if (size < 0) throw new IllegalArgumentException("negative size");
		value = checkedValue(value);
		return value == null ?
				new NullConstantStore<V>(this, size) :
				new ConstantStore<V>(this, value, size);
	}

	// object methods

	@Override
	public int hashCode() {
		return
				Boolean.hashCode(nullSettable) + 31 * (
				Boolean.hashCode(nullGettable) + 31 * (
				valueType.hashCode()           + 31 * (
				Objects.hash(nullValue))));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StoreType)) return false;
		StoreType<?> that = (StoreType<?>) obj;
		if (this.nullSettable != that.nullSettable) return false;
		if (this.nullGettable != that.nullGettable) return false;
		if (!this.valueType.equals(that.valueType)) return false;
		return Objects.equals(this.nullValue, that.nullValue);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(valueType.getName());
		if (nullGettable) {
			sb.append(" (null allowed)");
		} else if (nullSettable) {
			sb.append(" (null set to ").append(nullValue).append(")");
		} else {
			sb.append(" (null disallowed)");
		}
		return sb.toString();
	}

	// package scoped methods

	void checkNull() {
		if (!nullSettable) failNull();
	}

	V checkedValue(V value) {
		if (nullGettable || value != null) return value;
		if (!nullSettable) failNull();
		return nullValue;
	}

	boolean checkValue(Object value) {
		if (value == null) return nullSettable;
		if (!valueType.isPrimitive()) return valueType.isInstance(value);
		return Stores.primitiveClassFor(value.getClass()) == valueType;
	}

	void checkValues(V[] values) {
		if (nullGettable) return;
		if (nullSettable) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] == null) values[i] = nullValue;
			}
			return;
		}
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) failNull();
		}
	}

	int countNonNulls(V[] values) {
		if (!nullGettable) return values.length;
		int count = 0;
		for (V v : values) if (v != null) count++;
		return count;
	}

	V[] resizedCopyOf(V[] values, int newSize) {
		if (nullGettable) {
			return Arrays.copyOf(values, newSize);
		}
		if (!nullSettable) {
			if (newSize > values.length) throw new IllegalArgumentException("null disallowed");
			return Arrays.copyOf(values, newSize);
		}
		int oldSize = values.length;
		values = Arrays.copyOf(values, newSize);
		if (newSize > oldSize) Arrays.fill(values, oldSize, newSize, nullValue);
		return values;
	}

	@SuppressWarnings("unchecked")
	<W> StoreType<W> map(Mapping<V, W> fn) {
		if (nullSettable == nullGettable) return (StoreType<W>) this;
		return new StoreType<>(fn.rangeType(), nullSettable, nullGettable, nullValue == null ? null : fn.apply(nullValue));
	}

	// private helper methods

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Storage<V> createStorage() {
		if (nullGettable) {
			if (valueType.isEnum()) return new NullEnumStorage(this);
			if (valueType.isPrimitive()) return NullPrimitiveStore.newStorage(this);
			return NullArrayStore.mutableStorage(this);
		} else {
			if (valueType.isEnum()) return new EnumStorage(this);
			if (valueType.isPrimitive()) return PrimitiveStore.newStorage(this);
			return ArrayStore.mutableStorage(this);
		}
	}

}
