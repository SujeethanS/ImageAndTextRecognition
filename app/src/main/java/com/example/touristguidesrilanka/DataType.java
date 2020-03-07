package com.example.touristguidesrilanka;

public enum DataType {
    FLOAT32(1),
    INT32(2),
    UINT8(3),
    INT64(4),
    STRING(5);

    private final int value;
    private static final DataType[] values = values();

    private DataType(int value) {
        this.value = value;
    }

    public int byteSize() {
        switch(this) {
            case FLOAT32:
                return 4;
            case INT32:
                return 4;
            case UINT8:
                return 1;
            case INT64:
                return 8;
            case STRING:
                return -1;
            default:
                throw new IllegalArgumentException("DataType error: DataType " + this + " is not supported yet");
        }
    }

    int c() {
        return this.value;
    }

    static DataType fromC(int c) {
        DataType[] var1 = values;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            DataType t = var1[var3];
            if (t.value == c) {
                return t;
            }
        }

        throw new IllegalArgumentException("DataType error: DataType " + c + " is not recognized in Java (version " + TensorFlowLite.runtimeVersion() + ")");
    }

    String toStringName() {
        switch(this) {
            case FLOAT32:
                return "float";
            case INT32:
                return "int";
            case UINT8:
                return "byte";
            case INT64:
                return "long";
            case STRING:
                return "string";
            default:
                throw new IllegalArgumentException("DataType error: DataType " + this + " is not supported yet");
        }
    }
}