package org.dnttr.zephyr.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.dnttr.zephyr.serializer.annotations.Serializable;
import org.dnttr.zephyr.serializer.exceptions.NotSerializableException;
import org.dnttr.zephyr.serializer.internal.codec.CompositeObjectCodec;
import org.dnttr.zephyr.toolset.exceptions.InvalidLengthException;

/**
 * @author dnttr
 */

public class Serializer {

    private final CompositeObjectCodec codec = new CompositeObjectCodec();

    public ByteBuf serialize(Class<?> klass, Object instance) throws Exception {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();

        byte[] bytes = this.codec.write(klass, instance);
        buffer.writeBytes(bytes);

        return buffer;
    }

    public Object deserialize(Class<?> klass, ByteBuf buffer) throws Exception {
        if (klass == null) {
            throw new NullPointerException("Class cannot be null.");
        }

        if (!klass.isAnnotationPresent(Serializable.class)) {
            throw new NotSerializableException("Annotation not present");
        }

        if (buffer.readableBytes() <= 0) {
            throw new InvalidLengthException("Length of buffer is invalid");
        }

        return this.codec.read(klass, buffer);
    }
}