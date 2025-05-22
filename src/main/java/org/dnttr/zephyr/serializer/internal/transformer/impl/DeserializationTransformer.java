package org.dnttr.zephyr.serializer.internal.transformer.impl;

import org.dnttr.zephyr.serializer.exceptions.UnsupportedTypeException;
import org.dnttr.zephyr.serializer.internal.transformer.ITransformer;
import org.dnttr.zephyr.toolset.exceptions.InvalidLengthException;
import org.dnttr.zephyr.toolset.types.Type;
import org.jetbrains.annotations.NotNull;

import static org.dnttr.zephyr.toolset.operators.BooleanOperator.getBooleanArrayFromBytes;
import static org.dnttr.zephyr.toolset.operators.CharOperator.getCharArrayFromBytes;
import static org.dnttr.zephyr.toolset.operators.DoubleOperator.getDoubleArrayFromBytes;
import static org.dnttr.zephyr.toolset.operators.DoubleOperator.getDoubleFromBytes;
import static org.dnttr.zephyr.toolset.operators.FloatOperator.getFloatArrayFromBytes;
import static org.dnttr.zephyr.toolset.operators.FloatOperator.getFloatFromBytes;
import static org.dnttr.zephyr.toolset.operators.IntegerOperator.getIntegerFromBytes;
import static org.dnttr.zephyr.toolset.operators.LongOperator.getLongArrayFromBytes;
import static org.dnttr.zephyr.toolset.operators.LongOperator.getLongFromBytes;
import static org.dnttr.zephyr.toolset.operators.ShortOperator.getShortArrayFromBytes;
import static org.dnttr.zephyr.toolset.operators.ShortOperator.getShortFromBytes;
import static org.dnttr.zephyr.toolset.operators.StringOperator.getStringArrayFromBytes;

/**
 * @author dnttr
 */

public final class DeserializationTransformer implements ITransformer<byte[], Object> {

    @Override
    public Object transform(@NotNull Type type, boolean isArray, byte[] input) throws Exception {
        if (input.length == 0) {
            throw new InvalidLengthException("The byte array is empty");
        }

        return isArray ? this.getArrayObject(type, input) : this.getSingularObject(type, input);
    }

    private Object getArrayObject(Type type, byte[] bytes) throws InvalidLengthException {
        return switch (type) {
            case BYTE -> bytes;
            case INT -> getIntegerFromBytes(bytes);
            case STRING -> getStringArrayFromBytes(bytes);
            case LONG -> getLongArrayFromBytes(bytes);
            case SHORT -> getShortArrayFromBytes(bytes);
            case FLOAT -> getFloatArrayFromBytes(bytes);
            case DOUBLE -> getDoubleArrayFromBytes(bytes);
            case CHAR -> getCharArrayFromBytes(bytes);
            case BOOLEAN -> getBooleanArrayFromBytes(bytes);
        };
    }

    private Object getSingularObject(Type type, byte[] bytes) throws InvalidLengthException {
        return switch (type) {
            case BYTE -> bytes[0];
            case INT -> getIntegerFromBytes(bytes);
            case STRING -> getStringArrayFromBytes(bytes)[0];
            case LONG -> getLongFromBytes(bytes);
            case SHORT -> getShortFromBytes(bytes);
            case FLOAT -> getFloatFromBytes(bytes);
            case DOUBLE -> getDoubleFromBytes(bytes);
            case CHAR -> getCharArrayFromBytes(bytes)[0];
            case BOOLEAN -> getBooleanArrayFromBytes(bytes)[0];
        };
    }
}