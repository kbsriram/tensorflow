package org.tensorflow;

/**
 * A BaseType<T> keeps track of the base data type of a tensor in a way that allows the Java type system
 * to use it for type checking. There is a separate BaseType<T> object for each of the possible data types,
 * corresponding to the cases of the enum DataType.
 * 
 * @param <T>
 */
public class BaseType<T> {
	private BaseType(T[] dummy_array, DataType dtype) {
		this.info = dummy_array;
	}
	private T[] info; // must be length 1 containing default value of T
	private DataType dtype; // must match T
	
	/** Get the default value of this base type (0 or null) */
	public T defaultValue() {
		return info[0];
	}
	
	/** Get the default value of this base type as a scalar tensor. */
	public Tensor<T> defaultScalar() {
		return Tensor.create(defaultValue(), this);
	}
	
	/** Convert to the equivalent DataType. */
	public DataType dataType() {
		return dtype;
	}
	
	// XXX not sure whether to name these to match Java or TF conventions. Currently a compromise that
	// XXX maybe leaves everyone unhappy.
	public static BaseType<Float> Float = new BaseType<Float>(new Float[1], DataType.FLOAT);
	public static BaseType<Integer> Int = new BaseType<Integer>(new Integer[1], DataType.INT32);
	public static BaseType<Long> Long = new BaseType<Long>(new Long[1], DataType.INT64);
	public static BaseType<Double> Double = new BaseType<Double>(new Double[1], DataType.DOUBLE);
	public static BaseType<Boolean> Bool = new BaseType<Boolean>(new Boolean[1], DataType.BOOL);
	public static BaseType<String> String = new BaseType<String>(new String[1], DataType.STRING);
	public static BaseType<Byte> UInt8 = new BaseType<Byte>(new Byte[1], DataType.UINT8);	
}
