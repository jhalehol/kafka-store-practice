package org.dataart.sales.procesor.serdes;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class CustomDeserializer<T> implements Deserializer<T> {

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private Class<T> targetClass;
    private Type reflectionType;

    public CustomDeserializer(final Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    public CustomDeserializer(final Type reflectionType) {
        this.reflectionType = reflectionType;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public T deserialize(String topic, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        final Type type = targetClass != null ? targetClass : reflectionType;
        return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), type);
    }

    @Override
    public void close() {}
}
