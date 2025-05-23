package org.dnttr.zephyr.serializer.internal.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import lombok.AllArgsConstructor;
import org.dnttr.zephyr.serializer.exceptions.IllegalModifierException;
import org.dnttr.zephyr.serializer.internal.descriptors.FieldDescriptor;
import org.dnttr.zephyr.toolset.types.Type;
import org.dnttr.zephyr.toolset.types.TypeMatch;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static org.dnttr.zephyr.toolset.types.Type.*;

/**
 * @author dnttr
 */

@AllArgsConstructor
public class AtomicFieldCodec {

    public FieldDescriptor read(@NotNull ByteBuf buffer) throws Exception {
        try {
            int expectedMetadataLength = 2 * INT.getBytes() + SHORT.getBytes() + 2 * BOOLEAN.getBytes();

            if (buffer.isReadable(expectedMetadataLength)) {
                int addressLength = buffer.readInt();
                int objectLength = buffer.readInt();

                int identity = buffer.readShort();

                boolean isNull = buffer.readBoolean();
                boolean isArray = buffer.readBoolean();

                int expectedDataLength = addressLength + objectLength;

                boolean flag = expectedDataLength == buffer.readableBytes();

                if (buffer.isReadable(expectedDataLength) && flag) {
                    byte[] addressBytes = ByteBufUtil.getBytes(buffer.readBytes(addressLength));
                    byte[] objectBytes = ByteBufUtil.getBytes(buffer.readBytes(objectLength));

                    Optional<Type> type = TypeMatch.getType(identity);

                    if (type.isEmpty()) {
                        throw new IllegalModifierException(String.format("Type %s not found", identity));
                    }

                    return new FieldDescriptor(identity, type.get(), addressBytes, objectBytes, isNull, isArray);
                }
            }
        } catch (Exception ex) {
            throw new Exception("An exception occurred during reading the field descriptor.");
        }

        throw new Exception("An exception occurred during reading the field descriptor.");
    }

    public byte[] write(@NotNull FieldDescriptor descriptor) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();

        buffer.writeInt(descriptor.getAddress().length);
        buffer.writeInt(descriptor.getValue().length);

        buffer.writeShort(descriptor.getIdentity());

        buffer.writeBoolean(descriptor.isNull());
        buffer.writeBoolean(descriptor.isArray());

        buffer.writeBytes(descriptor.getAddress());
        buffer.writeBytes(descriptor.getValue());

        byte[] bytes = ByteBufUtil.getBytes(buffer);
        buffer.release();

        return bytes;
    }
}
