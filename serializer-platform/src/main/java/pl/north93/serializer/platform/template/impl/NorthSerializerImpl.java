package pl.north93.serializer.platform.template.impl;

import java.lang.reflect.Type;

import pl.north93.serializer.platform.NorthSerializer;
import pl.north93.serializer.platform.context.DeserializationContext;
import pl.north93.serializer.platform.context.SerializationContext;
import pl.north93.serializer.platform.format.DeserializationConfiguration;
import pl.north93.serializer.platform.format.SerializationConfiguration;
import pl.north93.serializer.platform.format.SerializationFormat;
import pl.north93.serializer.platform.reflect.ClassResolver;
import pl.north93.serializer.platform.reflect.impl.DefaultClassResolver;
import pl.north93.serializer.platform.reflect.impl.ReflectionEngineImpl;
import pl.north93.serializer.platform.template.TemplateEngine;
import pl.north93.serializer.platform.template.field.CustomFieldInfo;
import pl.north93.serializer.platform.template.field.FieldInfo;

public class NorthSerializerImpl<OUT, IN> implements NorthSerializer<OUT, IN>
{
    private final TemplateEngine templateEngine;
    private final SerializationFormat<OUT, IN, ?, ?> serializationFormat;

    public NorthSerializerImpl(final SerializationFormat<OUT, IN, ?, ?> serializationFormat, final ClassResolver classResolver)
    {
        final ReflectionEngineImpl reflectionEngine = new ReflectionEngineImpl(classResolver);

        this.templateEngine = new TemplateEngineImpl(reflectionEngine, serializationFormat.getTypePredictor());
        this.serializationFormat = serializationFormat;
        this.serializationFormat.configure(this.templateEngine);
    }

    public NorthSerializerImpl(final SerializationFormat<OUT, IN, ?, ?> serializationFormat)
    {
        this(serializationFormat, new DefaultClassResolver());
    }

    @SuppressWarnings("unchecked")
    @Override
    public OUT serialize(final Type type, final Object object, final SerializationConfiguration<?> configuration)
    {
        final SerializationContext context = configuration.createContext(this.templateEngine);
        final var template = this.templateEngine.getTemplate(type);

        try
        {
            template.serialise(context, this.createRootField(type), object);
            return (OUT) context.finalizeAndGetResult();
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Exception thrown when serializing " + object, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(final Type type, final IN serialized, final DeserializationConfiguration<IN, ?> configuration)
    {
        final DeserializationContext context = configuration.createContext(this.templateEngine, serialized);
        final var template = this.templateEngine.getTemplate(type);

        try
        {
            return (T) template.deserialize(context, this.createRootField(type));
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Exception thrown when deserializing " + serialized, e);
        }
    }

    @Override
    public SerializationFormat<OUT, IN, ?, ?> getSerializationFormat()
    {
        return this.serializationFormat;
    }

    @Override
    public TemplateEngine getTemplateEngine()
    {
        return this.templateEngine;
    }

    // reprezentuje glówne pole przy wejsciu do pierwszej templatki
    private FieldInfo createRootField(final Type type)
    {
        return new CustomFieldInfo(null, type);
    }
}
