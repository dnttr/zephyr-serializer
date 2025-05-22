package org.dnttr.zephyr.serializer.internal.descriptors;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.dnttr.zephyr.toolset.types.Type;

/**
 * @author dnttr
 */

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class FieldDescriptor {

    private final int identity;

    @EqualsAndHashCode.Exclude
    private final Type type;

    private final byte[] address;
    private final byte[] value;

    private boolean isNull;
    private boolean isArray;
}
