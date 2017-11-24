package com.tomgibara.storage;

public final class StoreAccessors {

	public static StoreBytes bytesFor(Store<?> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		if (store instanceof StoreBytes) return (StoreBytes) store;
		return new NumberAccessor.Bytes(store);
	}

	public static StoreShorts shortsFor(Store<?> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		if (store instanceof StoreShorts) return (StoreShorts) store;
		return new NumberAccessor.Shorts(store);
	}

	public static StoreInts intsFor(Store<?> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		if (store instanceof StoreInts) return (StoreInts) store;
		return new NumberAccessor.Ints(store);
	}

	public static StoreLongs longsFor(Store<?> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		if (store instanceof StoreLongs) return (StoreLongs) store;
		return new NumberAccessor.Longs(store);
	}

	public static StoreFloats floatsFor(Store<?> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		if (store instanceof StoreFloats) return (StoreFloats) store;
		return new NumberAccessor.Floats(store);
	}

	public static StoreDoubles doublesFor(Store<?> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		if (store instanceof StoreDoubles) return (StoreDoubles) store;
		return new NumberAccessor.Doubles(store);
	}

	public static StoreBooleans booleansFor(Store<?> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		if (store instanceof StoreBooleans) return (StoreBooleans) store;
		return new BooleanAccessor(store);
	}

	public static StoreChars charsFor(Store<?> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		if (store instanceof StoreChars) return (StoreChars) store;
		return new CharAccessor(store);
	}

	interface StoreBytes {
		boolean isByte(int index);
		byte getByte(int index);
		void setByte(int index, byte value);
	}

	interface StoreShorts {
		boolean isShort(int index);
		short getShort(int index);
		void setShort(int index, short value);
	}

	interface StoreInts {
		boolean isInt(int index);
		int getInt(int index);
		void setInt(int index, int value);
	}

	interface StoreLongs {
		boolean isLong(int index);
		long getLong(int index);
		void setLong(int index, long value);
	}

	interface StoreBooleans {
		boolean isBoolean(int index);
		boolean getBoolean(int index);
		void setBoolean(int index, boolean value);
	}

	interface StoreChars {
		boolean isChar(int index);
		char getChar(int index);
		void setChar(int index, char value);
	}

	interface StoreFloats {
		boolean isFloat(int index);
		float getFloat(int index);
		void setFloat(int index, float value);
	}

	interface StoreDoubles {
		boolean isDouble(int index);
		double getDouble(int index);
		void setDouble(int index, double value);
	}

	private StoreAccessors() {}

	private static final class BooleanAccessor implements StoreBooleans {

		private final Store<?> store;

		BooleanAccessor(Store<?> store) {
			Class<?> type = store.type().valueType;
			if (type != Object.class && type != Boolean.class) throw new IllegalArgumentException("store type not compatible with accessor");
			this.store = store;
		}

		@Override
		public boolean isBoolean(int index) {
			return store.get(index) instanceof Boolean;
		}

		@Override
		public boolean getBoolean(int index) {
			Object value = store.get(index);
			if (!(value instanceof Boolean)) throw new RuntimeException("value at index is not a boolean");
			return ((Boolean) value).booleanValue();
		}

		/* Ugly, but prechecked during construction */
		@SuppressWarnings("unchecked")
		@Override
		public void setBoolean(int index, boolean value) {
			((Store<Object>)store).set(index, value);
		}

	}

	private static final class CharAccessor implements StoreChars {

		private final Store<?> store;

		CharAccessor(Store<?> store) {
			Class<?> type = store.type().valueType;
			if (type != Object.class && type != Character.class) throw new IllegalArgumentException("store type not compatible with accessor");
			this.store = store;
		}

		@Override
		public boolean isChar(int index) {
			return store.get(index) instanceof Character;
		}

		@Override
		public char getChar(int index) {
			Object value = store.get(index);
			if (!(value instanceof Character)) throw new RuntimeException("value at index is not a character");
			return ((Character) value).charValue();
		}

		/* Ugly, but prechecked during construction */
		@SuppressWarnings("unchecked")
		@Override
		public void setChar(int index, char value) {
			((Store<Object>)store).set(index, value);
		}

	}
}