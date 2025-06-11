package org.dnttr.zephyr.serializer.internal.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import org.dnttr.zephyr.serializer.annotations.Address;
import org.dnttr.zephyr.serializer.annotations.Serializable;
import org.dnttr.zephyr.serializer.exceptions.IllegalModifierException;
import org.dnttr.zephyr.serializer.exceptions.NotSerializableException;
import org.dnttr.zephyr.serializer.internal.descriptors.FieldDescriptor;
import org.dnttr.zephyr.serializer.internal.transformer.impl.DeserializationTransformer;
import org.dnttr.zephyr.serializer.internal.transformer.impl.SerializationTransformer;
import org.dnttr.zephyr.toolset.exceptions.InvalidLengthException;
import org.dnttr.zephyr.toolset.reflection.Reflection;
import org.dnttr.zephyr.toolset.types.Type;
import org.dnttr.zephyr.toolset.types.TypeMatch;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author dnttr
 */

public class CompositeObjectCodec {

    private final AtomicFieldCodec codec = new AtomicFieldCodec();
    private final DeserializationTransformer deserializationTransformer = new DeserializationTransformer();
    private final SerializationTransformer serializationTransformer = new SerializationTransformer();

    public Object read(Class<?> klass, ByteBuf buffer) throws Exception {
        try {
            if (buffer.readableBytes() < 4) {
                throw new InvalidLengthException("Buffer underflow. (expect at least 4, got " + buffer.readableBytes() + ")");
            }

            int size = buffer.readInt();

            if (size < 0) {
                throw new InvalidLengthException("The fields size cannot be negative.");
            }

            Constructor<?> constructor = Reflection.getConstructor(klass, 0);
            List<Field> fields = Reflection.getFields(klass, Address.class, true).toList();

            if (fields.size() != size) {
                throw new InvalidLengthException(size, fields.size(), false);
            }

            Map<String, Integer> paramPositions = getMap(constructor);
            HashMap<Field, Object> regularFields = new HashMap<>();

            Object[] constructorParams = new Object[constructor.getParameterCount()];

            for (int i = 0; i < size; i++) {
                if (buffer.readableBytes() < 4) {
                    throw new InvalidLengthException("Buffer underflow. (expect at least 4, got " + buffer.readableBytes() + ")");
                }

                int length = buffer.readInt();

                ByteBuf fieldData = buffer.readBytes(length);
                try {
                    FieldDescriptor descriptor = codec.read(fieldData);
                    String address = new String(descriptor.getAddress(), StandardCharsets.UTF_8);

                    Object value = deserializationTransformer.transform(
                            descriptor.getType(),
                            descriptor.isArray(),
                            descriptor.getValue()
                    );

                    Optional<Field> fieldOpt = fields.stream()
                            .filter(f -> f.getDeclaredAnnotation(Address.class).address().equalsIgnoreCase(address))
                            .findFirst();

                    if (fieldOpt.isPresent()) {
                        Field field = fieldOpt.get();

                        if (Modifier.isFinal(field.getModifiers())) {
                            Integer position = paramPositions.get(address.toLowerCase());

                            if (position != null) {
                                constructorParams[position] = value;
                            } else {
                                throw new IllegalStateException("Field " + field.getName() + " not found in constructor parameters"); //it is even possible? no idea
                            }
                        } else {
                            regularFields.put(field, value);
                        }
                    }
                } finally {
                    fieldData.release();
                }
            }

            Object instance = constructor.getParameterCount() == 0
                    ? Reflection.newInstance(constructor, new Object[0])
                    : Reflection.newInstance(constructor, constructorParams);

            if (instance == null) {
                throw new IllegalStateException("Failed to create instance of " + klass.getName());
            }

            setAll(regularFields, klass, instance);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize " + klass.getName(), e);
        }
    }

    private void setAll(Map<Field, Object> map, Class<?> klass, Object instance) {
        map.forEach((field, value) -> {
            field.setAccessible(true);

            try {
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to set field '" + field.getName() + "' in class '" + klass.getName() + "'", e);
            }
        });
    }

    private @NotNull Map<String, Integer> getMap(Constructor<?> constructor) {
        Map<String, Integer> positions = new HashMap<>();
        Parameter[] parameters = constructor.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (!parameter.isAnnotationPresent(Address.class)) {
                throw new IllegalModifierException("Parameter " + parameter.getName() + " is not annotated with @Map");
            }

            Address addressAnnotation = parameter.getDeclaredAnnotation(Address.class);
            positions.put(addressAnnotation.address().toLowerCase(), i);
        }
        return positions;
    }

    public byte[] write(Class<?> klass, Object object) throws Exception {
        if (!Reflection.isConcreteClass(klass) || !klass.isAnnotationPresent(Serializable.class)) {
            throw new NotSerializableException("Class must be concrete and annotated with @Serializable");
        }

        ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();
        try {
            List<FieldDescriptor> descriptors = getDescriptors(klass, object);

            buffer.writeInt(descriptors.size());

            for (FieldDescriptor descriptor : descriptors) {
                byte[] fieldBytes = codec.write(descriptor);
                buffer.writeInt(fieldBytes.length);
                buffer.writeBytes(fieldBytes);
            }

            return ByteBufUtil.getBytes(buffer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize " + klass.getName(), e);
        } finally {
            buffer.release();
        }
    }

    private List<FieldDescriptor> getDescriptors(Class<?> klass, Object instance) throws Exception {
        List<Field> fields = Reflection.getFields(klass, Address.class, true)
                .filter(Reflection::isRegularField)
                .toList();

        Constructor<?> constructor = Reflection.getConstructor(klass, 0);
        List<Field> orderedFields = getFields(constructor, fields);

        List<FieldDescriptor> descriptors = new ArrayList<>();

        for (Field field : orderedFields) {
            field.setAccessible(true);

            int identity = TypeMatch.getModifier(field);
            Optional<Type> type = TypeMatch.getType(identity);

            if (type.isEmpty()) {
                throw new IllegalModifierException("Unsupported type modifier: " + identity);
            }

            Address addressAnnotation = field.getDeclaredAnnotation(Address.class);

            String address = addressAnnotation.address();
            byte[] addressBytes = address.getBytes(StandardCharsets.UTF_8);

            Object value = field.get(instance);
            boolean isArray = field.getType().isArray();
            boolean isNull = value == null;

            byte[] valueBytes = isNull
                    ? new byte[0]
                    : serializationTransformer.transform(type.get(), isArray, value);

            descriptors.add(new FieldDescriptor(identity, type.get(), addressBytes, valueBytes, isNull, isArray));
        }

        return descriptors;
    }

    private static @NotNull List<Field> getFields(Constructor<?> constructor, List<Field> fields) {
        List<String> constructorParamAddresses = Arrays.stream(constructor.getParameters())
                .filter(param -> param.isAnnotationPresent(Address.class))
                .map(param -> param.getDeclaredAnnotation(Address.class))
                .map(annotation -> annotation.address().toLowerCase()).toList();

        List<Field> orderedFields = new ArrayList<>(fields);

        orderedFields.sort((f1, f2) -> {
            String addr1 = f1.getDeclaredAnnotation(Address.class).address().toLowerCase();
            String addr2 = f2.getDeclaredAnnotation(Address.class).address().toLowerCase();

            boolean isParam1 = constructorParamAddresses.contains(addr1);
            boolean isParam2 = constructorParamAddresses.contains(addr2);

            if (isParam1 && !isParam2) return -1;
            if (!isParam1 && isParam2) return 1;
            if (isParam1) {
                return Integer.compare(
                        constructorParamAddresses.indexOf(addr1),
                        constructorParamAddresses.indexOf(addr2)
                );
            }
            return addr1.compareTo(addr2);
        });
        return orderedFields;
    }
}