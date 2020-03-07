package com.example.touristguidesrilanka;

import android.support.annotation.NonNull;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements AutoCloseable {
    NativeInterpreterWrapper wrapper;

    public Interpreter(@NonNull File modelFile) {
        this((File)modelFile, (Interpreter.Options)null);
    }

    /** @deprecated */
    @Deprecated
    public Interpreter(@NonNull File modelFile, int numThreads) {
        this(modelFile, (new Interpreter.Options()).setNumThreads(numThreads));
    }

    public Interpreter(@NonNull File modelFile, Interpreter.Options options) {
        this.wrapper = new NativeInterpreterWrapper(modelFile.getAbsolutePath(), options);
    }

    public Interpreter(@NonNull ByteBuffer byteBuffer) {
        this((ByteBuffer)byteBuffer, (Interpreter.Options)null);
    }

    /** @deprecated */
    @Deprecated
    public Interpreter(@NonNull ByteBuffer byteBuffer, int numThreads) {
        this(byteBuffer, (new Interpreter.Options()).setNumThreads(numThreads));
    }

    /** @deprecated */
    @Deprecated
    public Interpreter(@NonNull MappedByteBuffer mappedByteBuffer) {
        this((ByteBuffer)mappedByteBuffer, (Interpreter.Options)null);
    }

    public Interpreter(@NonNull ByteBuffer byteBuffer, Interpreter.Options options) {
        this.wrapper = new NativeInterpreterWrapper(byteBuffer, options);
    }

    public void run(Object input, Object output) {
        Object[] inputs = new Object[]{input};
        Map<Integer, Object> outputs = new HashMap();
        outputs.put(0, output);
        this.runForMultipleInputsOutputs(inputs, outputs);
    }

    public void runForMultipleInputsOutputs(Object[] inputs, @NonNull Map<Integer, Object> outputs) {
        this.checkNotClosed();
        this.wrapper.run(inputs, outputs);
    }

    public void resizeInput(int idx, int[] dims) {
        this.checkNotClosed();
        this.wrapper.resizeInput(idx, dims);
    }

    public int getInputTensorCount() {
        this.checkNotClosed();
        return this.wrapper.getInputTensorCount();
    }

    public int getInputIndex(String opName) {
        this.checkNotClosed();
        return this.wrapper.getInputIndex(opName);
    }

    public Tensor getInputTensor(int inputIndex) {
        this.checkNotClosed();
        return this.wrapper.getInputTensor(inputIndex);
    }

    public int getOutputTensorCount() {
        this.checkNotClosed();
        return this.wrapper.getOutputTensorCount();
    }

    public int getOutputIndex(String opName) {
        this.checkNotClosed();
        return this.wrapper.getOutputIndex(opName);
    }

    public Tensor getOutputTensor(int outputIndex) {
        this.checkNotClosed();
        return this.wrapper.getOutputTensor(outputIndex);
    }

    public Long getLastNativeInferenceDurationNanoseconds() {
        this.checkNotClosed();
        return this.wrapper.getLastNativeInferenceDurationNanoseconds();
    }

    /** @deprecated */
    @Deprecated
    public void setUseNNAPI(boolean useNNAPI) {
        this.checkNotClosed();
        this.wrapper.setUseNNAPI(useNNAPI);
    }

    /** @deprecated */
    @Deprecated
    public void setNumThreads(int numThreads) {
        this.checkNotClosed();
        this.wrapper.setNumThreads(numThreads);
    }

    public void modifyGraphWithDelegate(Delegate delegate) {
        this.checkNotClosed();
        this.wrapper.modifyGraphWithDelegate(delegate);
    }

    public void resetVariableTensors() {
        this.checkNotClosed();
        this.wrapper.resetVariableTensors();
    }

    public void close() {
        if (this.wrapper != null) {
            this.wrapper.close();
            this.wrapper = null;
        }

    }

    protected void finalize() throws Throwable {
        try {
            this.close();
        } finally {
            super.finalize();
        }

    }

    private void checkNotClosed() {
        if (this.wrapper == null) {
            throw new IllegalStateException("Internal error: The Interpreter has already been closed.");
        }
    }

    public static class Options {
        int numThreads = -1;
        Boolean useNNAPI;
        Boolean allowFp16PrecisionForFp32;
        Boolean allowBufferHandleOutput;
        final List<Delegate> delegates = new ArrayList();

        public Options() {
        }

        public Interpreter.Options setNumThreads(int numThreads) {
            this.numThreads = numThreads;
            return this;
        }

        public Interpreter.Options setUseNNAPI(boolean useNNAPI) {
            this.useNNAPI = useNNAPI;
            return this;
        }

        public Interpreter.Options setAllowFp16PrecisionForFp32(boolean allow) {
            this.allowFp16PrecisionForFp32 = allow;
            return this;
        }

        public Interpreter.Options addDelegate(Delegate delegate) {
            this.delegates.add(delegate);
            return this;
        }

        public Interpreter.Options setAllowBufferHandleOutput(boolean allow) {
            this.allowBufferHandleOutput = allow;
            return this;
        }
    }
}
