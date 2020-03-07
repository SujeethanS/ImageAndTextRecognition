package com.example.touristguidesrilanka;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NativeInterpreterWrapper implements AutoCloseable {
    private static final int ERROR_BUFFER_SIZE = 512;
    private long errorHandle;
    private long interpreterHandle;
    private long modelHandle;
    private long inferenceDurationNanoseconds;
    private ByteBuffer modelByteBuffer;
    private Map<String, Integer> inputsIndexes;
    private Map<String, Integer> outputsIndexes;
    private Tensor[] inputTensors;
    private Tensor[] outputTensors;
    private boolean isMemoryAllocated;
    private final List<Delegate> delegates;
    private final List<AutoCloseable> ownedDelegates;

    NativeInterpreterWrapper(String modelPath) {
        this((String)modelPath, (Interpreter.Options)null);
    }

    NativeInterpreterWrapper(ByteBuffer byteBuffer) {
        this((ByteBuffer)byteBuffer, (Interpreter.Options)null);
    }

    NativeInterpreterWrapper(String modelPath, Interpreter.Options options) {
        this.inferenceDurationNanoseconds = -1L;
        this.isMemoryAllocated = false;
        this.delegates = new ArrayList();
        this.ownedDelegates = new ArrayList();
        TensorFlowLite.init();
        long errorHandle = createErrorReporter(512);
        long modelHandle = createModel(modelPath, errorHandle);
        this.init(errorHandle, modelHandle, options);
    }

    NativeInterpreterWrapper(ByteBuffer buffer, Interpreter.Options options) {
        this.inferenceDurationNanoseconds = -1L;
        this.isMemoryAllocated = false;
        this.delegates = new ArrayList();
        this.ownedDelegates = new ArrayList();
        TensorFlowLite.init();
        if (buffer != null && (buffer instanceof MappedByteBuffer || buffer.isDirect() && buffer.order() == ByteOrder.nativeOrder())) {
            this.modelByteBuffer = buffer;
            long errorHandle = createErrorReporter(512);
            long modelHandle = createModelWithBuffer(this.modelByteBuffer, errorHandle);
            this.init(errorHandle, modelHandle, options);
        } else {
            throw new IllegalArgumentException("Model ByteBuffer should be either a MappedByteBuffer of the model file, or a direct ByteBuffer using ByteOrder.nativeOrder() which contains bytes of model content.");
        }
    }

    private void init(long errorHandle, long modelHandle, Interpreter.Options options) {
        if (options == null) {
            options = new Interpreter.Options();
        }

        this.errorHandle = errorHandle;
        this.modelHandle = modelHandle;
        this.interpreterHandle = createInterpreter(modelHandle, errorHandle, options.numThreads);
        this.inputTensors = new Tensor[getInputCount(this.interpreterHandle)];
        this.outputTensors = new Tensor[getOutputCount(this.interpreterHandle)];
        if (options.allowFp16PrecisionForFp32 != null) {
            allowFp16PrecisionForFp32(this.interpreterHandle, options.allowFp16PrecisionForFp32);
        }

        if (options.allowBufferHandleOutput != null) {
            allowBufferHandleOutput(this.interpreterHandle, options.allowBufferHandleOutput);
        }

        this.applyDelegates(options);
        allocateTensors(this.interpreterHandle, errorHandle);
        this.isMemoryAllocated = true;
    }

    public void close() {
        int i;
        for(i = 0; i < this.inputTensors.length; ++i) {
            if (this.inputTensors[i] != null) {
                this.inputTensors[i].close();
                this.inputTensors[i] = null;
            }
        }

        for(i = 0; i < this.outputTensors.length; ++i) {
            if (this.outputTensors[i] != null) {
                this.outputTensors[i].close();
                this.outputTensors[i] = null;
            }
        }

        delete(this.errorHandle, this.modelHandle, this.interpreterHandle);
        this.errorHandle = 0L;
        this.modelHandle = 0L;
        this.interpreterHandle = 0L;
        this.modelByteBuffer = null;
        this.inputsIndexes = null;
        this.outputsIndexes = null;
        this.isMemoryAllocated = false;
        this.delegates.clear();
        Iterator var5 = this.ownedDelegates.iterator();

        while(var5.hasNext()) {
            AutoCloseable ownedDelegate = (AutoCloseable)var5.next();

            try {
                ownedDelegate.close();
            } catch (Exception var4) {
                System.err.println("Failed to close flex delegate: " + var4);
            }
        }

        this.ownedDelegates.clear();
    }

    void run(Object[] inputs, Map<Integer, Object> outputs) {
        this.inferenceDurationNanoseconds = -1L;
        if (inputs != null && inputs.length != 0) {
            if (outputs != null && !outputs.isEmpty()) {
                for(int i = 0; i < inputs.length; ++i) {
                    Tensor tensor = this.getInputTensor(i);
                    int[] newShape = tensor.getInputShapeIfDifferent(inputs[i]);
                    if (newShape != null) {
                        this.resizeInput(i, newShape);
                    }
                }

                boolean needsAllocation = !this.isMemoryAllocated;
                if (needsAllocation) {
                    allocateTensors(this.interpreterHandle, this.errorHandle);
                    this.isMemoryAllocated = true;
                }

                for(int i = 0; i < inputs.length; ++i) {
                    this.getInputTensor(i).setTo(inputs[i]);
                }

                long inferenceStartNanos = System.nanoTime();
                run(this.interpreterHandle, this.errorHandle);
                long inferenceDurationNanoseconds = System.nanoTime() - inferenceStartNanos;
                if (needsAllocation) {
                    for(int i = 0; i < this.outputTensors.length; ++i) {
                        if (this.outputTensors[i] != null) {
                            this.outputTensors[i].refreshShape();
                        }
                    }
                }

                Iterator var13 = outputs.entrySet().iterator();

                while(var13.hasNext()) {
                    Map.Entry<Integer, Object> output = (Map.Entry)var13.next();
                    this.getOutputTensor((Integer)output.getKey()).copyTo(output.getValue());
                }

                this.inferenceDurationNanoseconds = inferenceDurationNanoseconds;
            } else {
                throw new IllegalArgumentException("Input error: Outputs should not be null or empty.");
            }
        } else {
            throw new IllegalArgumentException("Input error: Inputs should not be null or empty.");
        }
    }

    private static native void run(long var0, long var2);

    void resizeInput(int idx, int[] dims) {
        if (resizeInput(this.interpreterHandle, this.errorHandle, idx, dims)) {
            this.isMemoryAllocated = false;
            if (this.inputTensors[idx] != null) {
                this.inputTensors[idx].refreshShape();
            }
        }

    }

    private static native boolean resizeInput(long var0, long var2, int var4, int[] var5);

    void setUseNNAPI(boolean useNNAPI) {
        useNNAPI(this.interpreterHandle, useNNAPI);
    }

    void setNumThreads(int numThreads) {
        numThreads(this.interpreterHandle, numThreads);
    }

    void modifyGraphWithDelegate(Delegate delegate) {
        applyDelegate(this.interpreterHandle, this.errorHandle, delegate.getNativeHandle());
        this.delegates.add(delegate);
    }

    void resetVariableTensors() {
        resetVariableTensors(this.interpreterHandle, this.errorHandle);
    }

    int getInputIndex(String name) {
        if (this.inputsIndexes == null) {
            String[] names = getInputNames(this.interpreterHandle);
            this.inputsIndexes = new HashMap();
            if (names != null) {
                for(int i = 0; i < names.length; ++i) {
                    this.inputsIndexes.put(names[i], i);
                }
            }
        }

        if (this.inputsIndexes.containsKey(name)) {
            return (Integer)this.inputsIndexes.get(name);
        } else {
            throw new IllegalArgumentException(String.format("Input error: '%s' is not a valid name for any input. Names of inputs and their indexes are %s", name, this.inputsIndexes.toString()));
        }
    }

    int getOutputIndex(String name) {
        if (this.outputsIndexes == null) {
            String[] names = getOutputNames(this.interpreterHandle);
            this.outputsIndexes = new HashMap();
            if (names != null) {
                for(int i = 0; i < names.length; ++i) {
                    this.outputsIndexes.put(names[i], i);
                }
            }
        }

        if (this.outputsIndexes.containsKey(name)) {
            return (Integer)this.outputsIndexes.get(name);
        } else {
            throw new IllegalArgumentException(String.format("Input error: '%s' is not a valid name for any output. Names of outputs and their indexes are %s", name, this.outputsIndexes.toString()));
        }
    }

    Long getLastNativeInferenceDurationNanoseconds() {
        return this.inferenceDurationNanoseconds < 0L ? null : this.inferenceDurationNanoseconds;
    }

    int getOutputQuantizationZeroPoint(int index) {
        return getOutputQuantizationZeroPoint(this.interpreterHandle, index);
    }

    float getOutputQuantizationScale(int index) {
        return getOutputQuantizationScale(this.interpreterHandle, index);
    }

    int getInputTensorCount() {
        return this.inputTensors.length;
    }

    Tensor getInputTensor(int index) {
        if (index >= 0 && index < this.inputTensors.length) {
            Tensor inputTensor = this.inputTensors[index];
            if (inputTensor == null) {
                inputTensor = this.inputTensors[index] = Tensor.fromIndex(this.interpreterHandle, getInputTensorIndex(this.interpreterHandle, index));
            }

            return inputTensor;
        } else {
            throw new IllegalArgumentException("Invalid input Tensor index: " + index);
        }
    }

    int getOutputTensorCount() {
        return this.outputTensors.length;
    }

    Tensor getOutputTensor(int index) {
        if (index >= 0 && index < this.outputTensors.length) {
            Tensor outputTensor = this.outputTensors[index];
            if (outputTensor == null) {
                outputTensor = this.outputTensors[index] = Tensor.fromIndex(this.interpreterHandle, getOutputTensorIndex(this.interpreterHandle, index));
            }

            return outputTensor;
        } else {
            throw new IllegalArgumentException("Invalid output Tensor index: " + index);
        }
    }

    private void applyDelegates(Interpreter.Options options) {
        boolean originalGraphHasUnresolvedFlexOp = hasUnresolvedFlexOp(this.interpreterHandle);
        if (originalGraphHasUnresolvedFlexOp) {
            Delegate optionalFlexDelegate = maybeCreateFlexDelegate(options.delegates);
            if (optionalFlexDelegate != null) {
                this.ownedDelegates.add((AutoCloseable)optionalFlexDelegate);
                applyDelegate(this.interpreterHandle, this.errorHandle, optionalFlexDelegate.getNativeHandle());
            }
        }

        try {
            Iterator var6 = options.delegates.iterator();

            while(var6.hasNext()) {
                Delegate delegate = (Delegate)var6.next();
                applyDelegate(this.interpreterHandle, this.errorHandle, delegate.getNativeHandle());
                this.delegates.add(delegate);
            }

            if (options.useNNAPI != null && options.useNNAPI) {
                NnApiDelegate optionalNnApiDelegate = new NnApiDelegate();
                this.ownedDelegates.add(optionalNnApiDelegate);
                applyDelegate(this.interpreterHandle, this.errorHandle, optionalNnApiDelegate.getNativeHandle());
            }
        } catch (IllegalArgumentException var5) {
            boolean shouldSuppressException = originalGraphHasUnresolvedFlexOp && !hasUnresolvedFlexOp(this.interpreterHandle);
            if (!shouldSuppressException) {
                throw var5;
            }

            System.err.println("Ignoring failed delegate application: " + var5);
        }

    }

    private static Delegate maybeCreateFlexDelegate(List<Delegate> delegates) {
        try {
            Class<?> clazz = Class.forName("org.tensorflow.lite.flex.FlexDelegate");
            Iterator var2 = delegates.iterator();

            Delegate delegate;
            do {
                if (!var2.hasNext()) {
                    return (Delegate)clazz.getConstructor().newInstance();
                }

                delegate = (Delegate)var2.next();
            } while(!clazz.isInstance(delegate));

            return null;
        } catch (Exception var4) {
            return null;
        }
    }

    private static native int getOutputDataType(long var0, int var2);

    private static native int getOutputQuantizationZeroPoint(long var0, int var2);

    private static native float getOutputQuantizationScale(long var0, int var2);

    private static native long allocateTensors(long var0, long var2);

    private static native boolean hasUnresolvedFlexOp(long var0);

    private static native int getInputTensorIndex(long var0, int var2);

    private static native int getOutputTensorIndex(long var0, int var2);

    private static native int getInputCount(long var0);

    private static native int getOutputCount(long var0);

    private static native String[] getInputNames(long var0);

    private static native String[] getOutputNames(long var0);

    private static native void useNNAPI(long var0, boolean var2);

    private static native void numThreads(long var0, int var2);

    private static native void allowFp16PrecisionForFp32(long var0, boolean var2);

    private static native void allowBufferHandleOutput(long var0, boolean var2);

    private static native long createErrorReporter(int var0);

    private static native long createModel(String var0, long var1);

    private static native long createModelWithBuffer(ByteBuffer var0, long var1);

    private static native long createInterpreter(long var0, long var2, int var4);

    private static native void applyDelegate(long var0, long var2, long var4);

    private static native void resetVariableTensors(long var0, long var2);

    private static native void delete(long var0, long var2, long var4);
}
