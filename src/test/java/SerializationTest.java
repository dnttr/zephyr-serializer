import io.netty.buffer.ByteBuf;
import org.dnttr.zephyr.serializer.Serializer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author dnttr
 */

public class SerializationTest {

    @Test
    public void test() {
        try {
            SerializableObject object = new SerializableObject();

            ByteBuf serializedData = Serializer.serializeToBuffer(SerializableObject.class, object);
            assert serializedData.readableBytes() > 0;

            SerializableObject deserializedObject = (SerializableObject) Serializer.deserializeUsingBuffer(SerializableObject.class, serializedData);

            Field[] fields = SerializableObject.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object val1 = field.get(object);
                Object val2 = field.get(deserializedObject);

                boolean areEqual = isEqual(val1, val2);

                if (!Modifier.isTransient(field.getModifiers())) {
                    assertTrue(areEqual, "Field '" + field.getName() + "' values don't match: " + val1 + " vs " + val2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isEqual(Object val1, Object val2) {
        boolean areEqual;
        if (val1 != null && val1.getClass().isArray()) {
            areEqual = switch (val1) {
                case byte[] bytes -> Arrays.equals(bytes, (byte[]) val2);
                case short[] shorts -> Arrays.equals(shorts, (short[]) val2);
                case int[] ints -> Arrays.equals(ints, (int[]) val2);
                case long[] longs -> Arrays.equals(longs, (long[]) val2);
                case float[] floats -> Arrays.equals(floats, (float[]) val2);
                case double[] doubles -> Arrays.equals(doubles, (double[]) val2);
                case boolean[] booleans -> Arrays.equals(booleans, (boolean[]) val2);
                case char[] chars -> Arrays.equals(chars, (char[]) val2);
                default -> Arrays.deepEquals((Object[]) val1, (Object[]) val2);
            };
        } else {
            areEqual = Objects.equals(val1, val2);
        }
        return areEqual;
    }
}
