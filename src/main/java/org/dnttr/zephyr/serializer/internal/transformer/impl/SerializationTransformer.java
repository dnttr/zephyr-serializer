package org.dnttr.zephyr.serializer.internal.transformer.impl;

import org.dnttr.zephyr.serializer.exceptions.UnsupportedTypeException;
import org.dnttr.zephyr.serializer.internal.transformer.ITransformer;
import org.dnttr.zephyr.toolset.operators.ByteOperator;
import org.dnttr.zephyr.toolset.types.Type;

import static org.dnttr.zephyr.toolset.operators.BooleanOperator.getBytesFromBooleanArray;
import static org.dnttr.zephyr.toolset.operators.CharOperator.getBytesFromCharArray;
import static org.dnttr.zephyr.toolset.operators.DoubleOperator.getBytesFromDouble;
import static org.dnttr.zephyr.toolset.operators.DoubleOperator.getBytesFromDoubleArray;
import static org.dnttr.zephyr.toolset.operators.FloatOperator.getBytesFromFloat;
import static org.dnttr.zephyr.toolset.operators.FloatOperator.getBytesFromFloatArray;
import static org.dnttr.zephyr.toolset.operators.IntegerOperator.getBytesFromInteger;
import static org.dnttr.zephyr.toolset.operators.IntegerOperator.getBytesFromIntegerArray;
import static org.dnttr.zephyr.toolset.operators.LongOperator.getBytesFromLong;
import static org.dnttr.zephyr.toolset.operators.LongOperator.getBytesFromLongArray;
import static org.dnttr.zephyr.toolset.operators.ShortOperator.getBytesFromShort;
import static org.dnttr.zephyr.toolset.operators.ShortOperator.getBytesFromShortArray;
import static org.dnttr.zephyr.toolset.operators.StringOperator.getBytesFromStringArray;

/**
 * @author dnttr
 */

public final class SerializationTransformer implements ITransformer<Object, byte[]> {

    @Override
    public byte[] transform(Type type, boolean isArray, Object input) throws Exception {
        if (type == null) {
            throw new UnsupportedTypeException("Provided type is unsupported/null");
        }

        return isArray ?  this.getArrayObject(type, input) : this.getSingularObject(type, input);
    }

    private byte[] getArrayObject(Type type, Object object) {
        return switch (type) {
            case BYTE -> (byte[]) (object);
            case INT -> getBytesFromIntegerArray((int[]) object);
            case STRING -> getBytesFromStringArray((String[]) object);
            case LONG -> getBytesFromLongArray((long[]) object);
            case SHORT -> getBytesFromShortArray((short[]) object);
            case FLOAT -> getBytesFromFloatArray((float[]) object);
            case DOUBLE -> getBytesFromDoubleArray((double[]) object);
            case CHAR -> getBytesFromCharArray((char[]) object);
            case BOOLEAN -> getBytesFromBooleanArray((boolean[]) object);
        };
    }

    private byte[] getSingularObject(Type type, Object object) {
        return switch (type) {
            case BYTE -> ByteOperator.getSingleByteArray((Byte) object);
            case INT -> getBytesFromInteger((Integer) object);
            case STRING -> getBytesFromStringArray((String) object);
            case LONG -> getBytesFromLong((Long) object);
            case SHORT -> getBytesFromShort((Short) object);
            case FLOAT -> getBytesFromFloat((Float) object);
            case DOUBLE -> getBytesFromDouble((Double) object);
            case CHAR -> getBytesFromCharArray((Character) object);
            case BOOLEAN -> getBytesFromBooleanArray((Boolean) object);
        };
    }
}