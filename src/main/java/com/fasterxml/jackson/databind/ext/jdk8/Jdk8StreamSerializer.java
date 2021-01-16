package com.fasterxml.jackson.databind.ext.jdk8;

import java.util.stream.Stream;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Common typed stream serializer
 *
 */
public class Jdk8StreamSerializer extends StdSerializer<Stream<?>>
{
    /**
     * Stream elements type (matching T)
     */
    private final JavaType elemType;
    
    /**
     * element specific serializer, if any
     */
    private transient final JsonSerializer<Object> elemSerializer;

    /**
     * Constructor
     *
     * @param streamType Stream type
     * @param elemType   Stream elements type (matching T)
     */
    public Jdk8StreamSerializer(JavaType streamType, JavaType elemType) {
        this(streamType, elemType, null);
    }

    /**
     * Constructor with custom serializer
     *
     * @param streamType     Stream type
     * @param elemType       Stream elements type (matching T)
     * @param elemSerializer Custom serializer to use for element type
     */
    public Jdk8StreamSerializer(JavaType streamType, JavaType elemType, JsonSerializer<Object> elemSerializer) {
        super(streamType);
        this.elemType = elemType;
        this.elemSerializer = elemSerializer;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
    {
        if (!elemType.hasRawClass(Object.class)
                && (provider.isEnabled(MapperFeature.USE_STATIC_TYPING) || elemType.isFinal())) {
            return new Jdk8StreamSerializer(
                    provider.getTypeFactory().constructParametricType(Stream.class, elemType),
                    elemType,
                    provider.findContentValueSerializer(elemType, property));
        }
        return this;
    }

    @Override
    public void serialize(Stream<?> stream, JsonGenerator g, SerializerProvider provider)
            throws JacksonException
    {
        try (Stream<?> s = stream) {
            g.writeStartArray(s);
            
            s.forEach(elem -> {
                if (elemSerializer == null) {
                    provider.writeValue(g, elem);
                } else {
                    elemSerializer.serialize(elem, g, provider);
                }
            });
            g.writeEndArray();
        }
    }
}
