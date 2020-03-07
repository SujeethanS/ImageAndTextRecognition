package com.example.touristguidesrilanka;

public class NnApiDelegate implements Delegate, AutoCloseable {
    private static final long INVALID_DELEGATE_HANDLE = 0L;
    private long delegateHandle;

    public NnApiDelegate(NnApiDelegate.Options options) {
        TensorFlowLite.init();
        this.delegateHandle = createDelegate(options.executionPreference, options.accelerator_name, options.cache_dir, options.model_token);
    }

    public NnApiDelegate() {
        this(new NnApiDelegate.Options());
    }

    public long getNativeHandle() {
        return this.delegateHandle;
    }

    public void close() {
        if (this.delegateHandle != 0L) {
            deleteDelegate(this.delegateHandle);
            this.delegateHandle = 0L;
        }

    }

    private static native long createDelegate(int var0, String var1, String var2, String var3);

    private static native void deleteDelegate(long var0);

    public static final class Options {
        public static final int EXECUTION_PREFERENCE_UNDEFINED = -1;
        public static final int EXECUTION_PREFERENCE_LOW_POWER = 0;
        public static final int EXECUTION_PREFERENCE_FAST_SINGLE_ANSWER = 1;
        public static final int EXECUTION_PREFERENCE_SUSTAINED_SPEED = 2;
        int executionPreference = -1;
        String accelerator_name = null;
        String cache_dir = null;
        String model_token = null;

        public Options() {
        }

        public NnApiDelegate.Options setExecutionPreference(int preference) {
            this.executionPreference = preference;
            return this;
        }

        public NnApiDelegate.Options setAcceleratorName(String name) {
            this.accelerator_name = name;
            return this;
        }

        public NnApiDelegate.Options setCacheDir(String name) {
            this.cache_dir = name;
            return this;
        }

        public NnApiDelegate.Options setModelToken(String name) {
            this.model_token = name;
            return this;
        }
    }
}

