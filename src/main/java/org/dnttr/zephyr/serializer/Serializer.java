package org.dnttr.zephyr.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.dnttr.zephyr.serializer.annotations.Serializable;
import org.dnttr.zephyr.serializer.exceptions.NotSerializableException;
import org.dnttr.zephyr.serializer.internal.codec.CompositeObjectCodec;
import org.dnttr.zephyr.toolset.exceptions.InvalidLengthException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * @author dnttr
 */

public class Serializer {

    private static final CompositeObjectCodec codec = new CompositeObjectCodec();

    public static ByteBuf serializeToBuffer(Class<?> klass, Object instance) throws Exception {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();

        byte[] bytes = codec.write(klass, instance);
        buffer.writeBytes(bytes);

        return buffer;
    }

    public static byte[] serializeToArray(Class<?> klass, Object instance) throws Exception {
        return codec.write(klass, instance);
    }

    /*
     * Might not be necessary to have this method, but it is here for completeness.
     * It allows deserialization from a byte array directly.
     * It is not fully tested and therefore might not be reliable.
     */
    @ApiStatus.Experimental
    public static Object deserializeUsingArray(@NotNull Class<?> klass, byte[] bytes) throws Exception {
        if (!klass.isAnnotationPresent(Serializable.class)) {
            throw new NotSerializableException("Annotation not present");
        }

        ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();
        buffer.writeBytes(bytes);

        if (buffer.readableBytes() <= 0) {
            throw new InvalidLengthException("Length of buffer is invalid");
        }

        Object object = codec.read(klass, buffer);
        buffer.release();

        return object;
    }

    public static Object deserializeUsingBuffer(@NotNull Class<?> klass, @NotNull ByteBuf buffer) throws Exception {
        if (!klass.isAnnotationPresent(Serializable.class)) {
            throw new NotSerializableException("Annotation not present");
        }

        if (buffer.readableBytes() <= 0) {
            throw new InvalidLengthException("Length of buffer is invalid");
        }

        return codec.read(klass, buffer);
    }
}