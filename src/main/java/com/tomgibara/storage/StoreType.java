package com.tomgibara.storage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import com.tomgibara.fundament.Mapping;

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

	//TODO this is pretty smelly
	@SuppressWarnings("unchecked")
	public static <V> StoreType<V> generic() {
		return (StoreType<V>) OBJECT;
	}

	public static <V> StoreType<V> of(Class<V> valueType) {
		if (valueType == Object.class) return generic();
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
	 * A nullity that permits nulls to be stored and retrieved. Both
	 * {@link #nullSettable()} and {@link #nullGettable()} return true.
	 *
	 * @param <V>
	 *            the generic type of the returned nullity
	 * @return a nullity that permits null values
	 */
	public StoreType<V> settingNullAllowed() {
		return nullSettable & nullGettable ? this : new StoreType<>(valueType, true, true, null);
	}

	/**
	 * A nullity that does not permit nulls to be stored or retrieved. Both
	 * {@link #nullSettable()} and {@link #nullGettable()} return false.
	 *
	 * @param <V>
	 *            the generic type of the returned nullity
	 * @return a nullity that prohibits null values
	 */
	public StoreType<V> settingNullDisallowed() {
		return !nullSettable && !nullGettable ? this : new StoreType<>(valueType, false, false, null);
	}

	/**
	 * <p>
	 * A nullity that substitutes nulls with the specified value. The returned
	 * nullity allows nulls to be set on a store ({@link #nullSettable()}
	 * returns true) but will not allow nulls to be returned from it (
	 * {@link #nullGettable()} returns false).
	 *
	 * <p>
	 * The exception to this is when a null value is supplied to the method
	 * (indicating that nulls should not be substituted). In this case, the
	 * method returns a nullity which is equivalent to that returned by
	 * {@link #settingNullAllowed()}.
	 *
	 * @param <V>
	 *            the type of any null-substituting value
	 * @param value
	 *            the value to be substituted for null
	 * @return a nullity that substitutes nulls
	 */
	public StoreType<V> settingNullToValue(V value) {
		return value == null ? settingNullAllowed() : new StoreType<>(valueType, true, false, value);
	}

	/**
	 * <p>
	 * Suggests a nullity that can substitute null for a given type. This method
	 * may be used to identify an appropriate null value for stores of common
	 * types.
	 *
	 * <p>
	 * It returns {@link #settingNullToValue(Object)} wrapping the following
	 * values for the types given.
	 *
	 * <dl>
	 * <dt>primitive numeric types
	 * <dd><code>0</code>
	 * <dt><code>boolean</code>
	 * <dd><code>false</code>
	 * <dt><code>char</code>
	 * <dd><code>'\0'</code>
	 * <dt>primitive wrapper types
	 * <dd><em>as per primitive types</em></dd>
	 * <dt>enumerations
	 * <dd>the enum constant with ordinal 0 (if it exists)
	 * <dt><code>java.lang.String</code>
	 * <dd>the empty string <code>""</code>
	 * </dl>
	 *
	 * <p>
	 * For all other types, the method returns {@link #settingNullAllowed()}.
	 * Future implementations may return null-replacing instances for a greater
	 * number of types.
	 *
	 * @param <V>
	 *            the generic type of the returned nullity
	 * @param type
	 *            a type of stored value
	 * @return an optional null value
	 */
	@SuppressWarnings("unchecked")
	public StoreType<V> settingNullToDefault() {

		if (valueType.isPrimitive()) {
			return (StoreType<V>) DEFAULTS[ Stores.hash(valueType) ];
		}

		if (valueType.isEnum()) {
			V[] constants = valueType.getEnumConstants();
			return constants.length == 0 ? settingNullAllowed() : settingNullToValue((V) constants[0]);
		}

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

		return settingNullAllowed();
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
	 * nulls into a store to which this nullity applies.
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
	
	public Storage<V> storage() {
		return storage == null ? storage = createStorage() : storage;
	}

	@SuppressWarnings("unchecked")
	public Storage<V> smallValueStorage(int range) {
		if (range <= 0) throw new IllegalArgumentException("non positive range");
		if (range == Integer.MAX_VALUE) throw new IllegalArgumentException("range too large");
		if (valueType != int.class) throw new IllegalStateException("requires int typed store");
		return (Storage<V>) SmallValueStore.newStorage(range, (StoreType<Integer>) this);
	}

	// stores

	public Store<V> emptyStore() {
		return new EmptyStore<>(this, false);
	}

	public Store<V> objectsAsStore(@SuppressWarnings("unchecked") V... objects) {
		if (objects == null) throw new IllegalArgumentException("null objects");
		Class<?> clss = objects.getClass().getComponentType();
		if (clss == valueType || valueType.isAssignableFrom(clss)) return new ImmutableArrayStore<>(objects, this);
		throw new IllegalArgumentException("object type not assignable to " + valueType.getName());
	}

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

	// TODO object methods

	// package scoped methods

	void checkNull() {
		if (!nullSettable) failNull();
	}

	V checkedValue(V value) {
		if (nullGettable || value != null) return value;
		if (!nullSettable) failNull();
		return nullValue;
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
