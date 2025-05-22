package org.dnttr.zephyr.serializer.internal.transformer;

import org.dnttr.zephyr.toolset.types.Type;

/**
 * @author dnttr
 */

public interface ITransformer<I, O> {


    O transform(Type type, boolean isArray, I input) throws Exception;
}
