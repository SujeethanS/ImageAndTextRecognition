package com.example.touristguidesrilanka;

public final class TensorFlowLite {
    private static final String LIBNAME = "tensorflowlite_jni";
    private static final Throwable LOAD_LIBRARY_EXCEPTION;
    private static volatile boolean isInit = false;

    private TensorFlowLite() {
    }

    /** @deprecated */
    @Deprecated
    public static String version() {
        return schemaVersion();
    }

    public static String runtimeVersion() {
        init();
        return nativeRuntimeVersion();
    }

    public static String schemaVersion() {
        init();
        return nativeSchemaVersion();
    }

    public static void init() {
        if (!isInit) {
            try {
                nativeRuntimeVersion();
                isInit = true;
            } catch (UnsatisfiedLinkError var2) {
                Throwable exceptionToLog = LOAD_LIBRARY_EXCEPTION != null ? LOAD_LIBRARY_EXCEPTION : var2;
                throw new UnsatisfiedLinkError("Failed to load native TensorFlow Lite methods. Check that the correct native libraries are present, and, if using a custom native library, have been properly loaded via System.loadLibrary():\n  " + exceptionToLog);
            }
        }
    }

    public static native String nativeRuntimeVersion();

    public static native String nativeSchemaVersion();

    static {
        UnsatisfiedLinkError loadLibraryException = null;

        try {
            System.loadLibrary("tensorflowlite_jni");
        } catch (UnsatisfiedLinkError var2) {
            loadLibraryException = var2;
        }

        LOAD_LIBRARY_EXCEPTION = loadLibraryException;
    }
}
