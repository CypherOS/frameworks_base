package com.google.protobuf.nano;

public abstract class ExtendableMessageNano<M extends ExtendableMessageNano<M>> extends MessageNano {
    protected FieldArray unknownFieldData;

    public M clone() throws CloneNotSupportedException {
        ExtendableMessageNano cloned = (ExtendableMessageNano) super.clone();
        InternalNano.cloneUnknownFieldData(this, cloned);
        return cloned;
    }
}
