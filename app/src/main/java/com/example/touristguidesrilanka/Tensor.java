package com.example.touristguidesrilanka;

import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

public final class Tensor {
    private long nativeHandle;
    private final DataType dtype;
    private int[] shapeCopy;

    static Tensor fromIndex(long nativeInterpreterHandle, int tensorIndex) {
        return new Tensor(create(nativeInterpreterHandle, tensorIndex));
    }

    void close() {
        delete(this.nativeHandle);
        this.nativeHandle = 0L;
    }

    public DataType dataType() {
        return this.dtype;
    }

    public int numDimensions() {
        return this.shapeCopy.length;
    }

    public int numBytes() {
        return numBytes(this.nativeHandle);
    }

    public int numElements() {
        return computeNumElements(this.shapeCopy);
    }

    public int[] shape() {
        return this.shapeCopy;
    }

    public int index() {
        return index(this.nativeHandle);
    }

    public String name() {
        return name(this.nativeHandle);
    }

    void setTo(Object src) {
        if (src == null) {
            if (!hasDelegateBufferHandle(this.nativeHandle)) {
                throw new IllegalArgumentException("Null inputs are allowed only if the Tensor is bound to a buffer handle.");
            }
        } else {
            this.throwIfDataIsIncompatible(src);
            if (isBuffer(src)) {
                this.setTo((Buffer)src);
            } else {
                writeMultiDimensionalArray(this.nativeHandle, src);
            }

        }
    }

    private void setTo(Buffer src) {
        if (src instanceof ByteBuffer) {
            ByteBuffer srcBuffer = (ByteBuffer)src;
            if (srcBuffer.isDirect() && srcBuffer.order() == ByteOrder.nativeOrder()) {
                writeDirectBuffer(this.nativeHandle, src);
            } else {
                this.buffer().put(srcBuffer);
            }
        } else if (src instanceof LongBuffer) {
            LongBuffer srcBuffer = (LongBuffer)src;
            if (srcBuffer.isDirect() && srcBuffer.order() == ByteOrder.nativeOrder()) {
                writeDirectBuffer(this.nativeHandle, src);
            } else {
                this.buffer().asLongBuffer().put(srcBuffer);
            }
        } else if (src instanceof FloatBuffer) {
            FloatBuffer srcBuffer = (FloatBuffer)src;
            if (srcBuffer.isDirect() && srcBuffer.order() == ByteOrder.nativeOrder()) {
                writeDirectBuffer(this.nativeHandle, src);
            } else {
                this.buffer().asFloatBuffer().put(srcBuffer);
            }
        } else {
            if (!(src instanceof IntBuffer)) {
                throw new IllegalArgumentException("Unexpected input buffer type: " + src);
            }

            IntBuffer srcBuffer = (IntBuffer)src;
            if (srcBuffer.isDirect() && srcBuffer.order() == ByteOrder.nativeOrder()) {
                writeDirectBuffer(this.nativeHandle, src);
            } else {
                this.buffer().asIntBuffer().put(srcBuffer);
            }
        }

    }

    Object copyTo(Object dst) {
        if (dst == null) {
            if (hasDelegateBufferHandle(this.nativeHandle)) {
                return dst;
            } else {
                throw new IllegalArgumentException("Null outputs are allowed only if the Tensor is bound to a buffer handle.");
            }
        } else {
            this.throwIfDataIsIncompatible(dst);
            if (isBuffer(dst)) {
                this.copyTo((Buffer)dst);
            } else {
                readMultiDimensionalArray(this.nativeHandle, dst);
            }

            return dst;
        }
    }

    private void copyTo(Buffer dst) {
        if (dst instanceof ByteBuffer) {
            ((ByteBuffer)dst).put(this.buffer());
        } else if (dst instanceof FloatBuffer) {
            ((FloatBuffer)dst).put(this.buffer().asFloatBuffer());
        } else if (dst instanceof LongBuffer) {
            ((LongBuffer)dst).put(this.buffer().asLongBuffer());
        } else {
            if (!(dst instanceof IntBuffer)) {
                throw new IllegalArgumentException("Unexpected output buffer type: " + dst);
            }

            ((IntBuffer)dst).put(this.buffer().asIntBuffer());
        }

    }

    int[] getInputShapeIfDifferent(Object input) {
        if (input == null) {
            return null;
        } else if (isBuffer(input)) {
            return null;
        } else {
            this.throwIfTypeIsIncompatible(input);
            int[] inputShape = computeShapeOf(input);
            return Arrays.equals(this.shapeCopy, inputShape) ? null : inputShape;
        }
    }

    void refreshShape() {
        this.shapeCopy = shape(this.nativeHandle);
    }

    static DataType dataTypeOf(Object o) {
        if (o != null) {
            Class c;
            for(c = o.getClass(); c.isArray(); c = c.getComponentType()) {
            }

            if (!Float.TYPE.equals(c) && !(o instanceof FloatBuffer)) {
                if (!Integer.TYPE.equals(c) && !(o instanceof IntBuffer)) {
                    if (Byte.TYPE.equals(c)) {
                        return DataType.UINT8;
                    } else if (!Long.TYPE.equals(c) && !(o instanceof LongBuffer)) {
                        if (String.class.equals(c)) {
                            return DataType.STRING;
                        } else {
                            throw new IllegalArgumentException("DataType error: cannot resolve DataType of " + o.getClass().getName());
                        }
                    } else {
                        return DataType.INT64;
                    }
                } else {
                    return DataType.INT32;
                }
            } else {
                return DataType.FLOAT32;
            }
        } else {
            throw new IllegalArgumentException("DataType error: cannot resolve DataType of " + o.getClass().getName());
        }
    }

    static int[] computeShapeOf(Object o) {
        int size = computeNumDimensions(o);
        int[] dimensions = new int[size];
        fillShape(o, 0, dimensions);
        return dimensions;
    }

    static int computeNumElements(int[] shape) {
        int n = 1;

        for(int i = 0; i < shape.length; ++i) {
            n *= shape[i];
        }

        return n;
    }

    static int computeNumDimensions(Object o) {
        if (o != null && o.getClass().isArray()) {
            if (Array.getLength(o) == 0) {
                throw new IllegalArgumentException("Array lengths cannot be 0.");
            } else {
                return 1 + computeNumDimensions(Array.get(o, 0));
            }
        } else {
            return 0;
        }
    }

    static void fillShape(Object o, int dim, int[] shape) {
        if (shape != null && dim != shape.length) {
            int len = Array.getLength(o);
            if (shape[dim] == 0) {
                shape[dim] = len;
            } else if (shape[dim] != len) {
                throw new IllegalArgumentException(String.format("Mismatched lengths (%d and %d) in dimension %d", shape[dim], len, dim));
            }

            for(int i = 0; i < len; ++i) {
                fillShape(Array.get(o, i), dim + 1, shape);
            }

        }
    }

    private void throwIfDataIsIncompatible(Object o) {
        this.throwIfTypeIsIncompatible(o);
        this.throwIfShapeIsIncompatible(o);
    }

    private void throwIfTypeIsIncompatible(Object o) {
        if (!isByteBuffer(o)) {
            DataType oType = dataTypeOf(o);
            if (oType != this.dtype) {
                throw new IllegalArgumentException(String.format("Cannot convert between a TensorFlowLite tensor with type %s and a Java object of type %s (which is compatible with the TensorFlowLite type %s).", this.dtype, o.getClass().getName(), oType));
            }
        }
    }

    private void throwIfShapeIsIncompatible(Object o) {
        if (isBuffer(o)) {
            Buffer oBuffer = (Buffer)o;
            int bytes = this.numBytes();
            int oBytes = isByteBuffer(o) ? oBuffer.capacity() : oBuffer.capacity() * this.dtype.byteSize();
            if (bytes != oBytes) {
                throw new IllegalArgumentException(String.format("Cannot convert between a TensorFlowLite buffer with %d bytes and a Java Buffer with %d bytes.", bytes, oBytes));
            }
        } else {
            int[] oShape = computeShapeOf(o);
            if (!Arrays.equals(oShape, this.shapeCopy)) {
                throw new IllegalArgumentException(String.format("Cannot copy between a TensorFlowLite tensor with shape %s and a Java object with shape %s.", Arrays.toString(this.shapeCopy), Arrays.toString(oShape)));
            }
        }
    }

    private static boolean isBuffer(Object o) {
        return o instanceof Buffer;
    }

    private static boolean isByteBuffer(Object o) {
        return o instanceof ByteBuffer;
    }

    private Tensor(long nativeHandle) {
        this.nativeHandle = nativeHandle;
        this.dtype = DataType.fromC(dtype(nativeHandle));
        this.shapeCopy = shape(nativeHandle);
    }

    private ByteBuffer buffer() {
        return buffer(this.nativeHandle).order(ByteOrder.nativeOrder());
    }

    private static native long create(long var0, int var2);

    private static native void delete(long var0);

    private static native ByteBuffer buffer(long var0);

    private static native void writeDirectBuffer(long var0, Buffer var2);

    private static native int dtype(long var0);

    private static native int[] shape(long var0);

    private static native int numBytes(long var0);

    private static native boolean hasDelegateBufferHandle(long var0);

    private static native void readMultiDimensionalArray(long var0, Object var2);

    private static native void writeMultiDimensionalArray(long var0, Object var2);

    private static native int index(long var0);

    private static native String name(long var0);
}