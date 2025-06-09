package org.dnttr.zephyr.serializer.internal.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import org.dnttr.zephyr.serializer.annotations.Map;
import org.dnttr.zephyr.serializer.annotations.Serializable;
import org.dnttr.zephyr.serializer.exceptions.DuplicateAddressException;
import org.dnttr.zephyr.serializer.exceptions.FieldNotPresentException;
import org.dnttr.zephyr.serializer.exceptions.IllegalModifierException;
import org.dnttr.zephyr.serializer.internal.descriptors.FieldDescriptor;
import org.dnttr.zephyr.serializer.internal.transformer.impl.DeserializationTransformer;
import org.dnttr.zephyr.serializer.internal.transformer.impl.SerializationTransformer;
import org.dnttr.zephyr.toolset.exceptions.InvalidLengthException;
import org.dnttr.zephyr.toolset.reflection.Reflection;
import org.dnttr.zephyr.toolset.types.Type;
import org.dnttr.zephyr.toolset.types.TypeMatch;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author dnttr
 */

public class CompositeObjectCodec {

    private final AtomicFieldCodec codec = new AtomicFieldCodec();

    private final DeserializationTransformer deserializationTransformer = new DeserializationTransformer();
    private final SerializationTransformer serializationTransformer = new SerializationTransformer();

    public Object read(Class<?> klass, ByteBuf buffer) throws Exception {
        var addresses = new ArrayList<>();

        if (buffer.readableBytes() < 4) {
            throw new InvalidLengthException("Buffer underflow. (expect at least 4, got " + buffer.readableBytes() + ")");
        }

        int size = buffer.readInt();

        if (size < 0) {
            throw new InvalidLengthException("The fields size cannot be negative.");
        }

        var constructor = Reflection.getConstructor(klass, 0);

        int parameterCount = constructor.getParameterCount();

        if (parameterCount > 0) {
            throw new InvalidLengthException(0, parameterCount);
        }

        var fields = Reflection.getFields(klass, Map.class, true).toList();
        if (fields.size() != size) {
            throw new InvalidLengthException(size, fields.size(), false);
        }

        Object instance = Reflection.newInstance(constructor, new Object[0]);

        if (instance == null) {
            throw new IllegalStateException("The instance cannot be null.");
        }

        for (int i = 0; i < size; i++) {
            if (buffer.readableBytes() < 4) {
                throw new InvalidLengthException("Buffer underflow. (expect at least 4, got " + buffer.readableBytes() + ")");
            }

            int length = buffer.readInt();

            ByteBuf data = buffer.readBytes(length);
            FieldDescriptor descriptor = this.codec.read(data);

            Object object = this.deserializationTransformer.transform(descriptor.getType(), descriptor.isArray(), descriptor.getValue());
            String address = new String(descriptor.getAddress(), StandardCharsets.UTF_8);

            if (!addresses.contains(address)) {
                addresses.add(address);
            } else {
                throw new DuplicateAddressException("Duplicate keys found." + address);
            }

            var optionalField = fields.stream().filter(f -> f.getDeclaredAnnotation(Map.class).address().equalsIgnoreCase(address)).findFirst();

            if (optionalField.isPresent()) {
                Field field = optionalField.get();

                field.setAccessible(true);
                field.set(instance, object);
            } else {
                throw new FieldNotPresentException("Field not present." + address);
            }
        }

        return instance;
    }

    public byte[] write(Class<?> klass, Object object) throws Exception {
        if (!Reflection.isConcreteClass(klass) && klass.isAnnotationPresent(Serializable.class)) {
            return null;
        }

        List<FieldDescriptor> descriptors = this.getDescriptors(klass, object);
        ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();

        buffer.writeInt(descriptors.size());

        descriptors.stream().map(this.codec::write).forEachOrdered(bytes -> {
            int length = bytes.length;

            buffer.writeInt(length);
            buffer.writeBytes(bytes);
        });

        byte[] bytes = ByteBufUtil.getBytes(buffer);
        buffer.release();

        return bytes;
    }

    private List<FieldDescriptor> getDescriptors(Class<?> klass, Object instance) throws Exception {
        var fields = Reflection.getFields(klass, Map.class, true).filter(Reflection::isRegularField);

        List<FieldDescriptor> descriptors = new ArrayList<>();

        for (Field field : fields.toList()) {
            int identity = TypeMatch.getModifier(field);
            Optional<Type> type = TypeMatch.getType(identity);

            if (type.isEmpty()) {
                throw new IllegalModifierException(String.format("Type %s not found", identity));
            }

            String address = field.getDeclaredAnnotation(Map.class).address();
            byte[] addressBytes = address.getBytes(StandardCharsets.UTF_8);

            Object object;
            object = field.get(instance);

            boolean isArray = field.getType().isArray();
            boolean isNull = object == null;
            byte[] objectBytes = new byte[0];

            if (!isNull) {
                objectBytes = this.serializationTransformer.transform(type.orElse(null), isArray, object);
            }

            var descriptor = new FieldDescriptor(identity, type.get(), addressBytes, objectBytes, isNull, isArray);
            descriptors.add(descriptor);
        }

        return descriptors;
    }
}
