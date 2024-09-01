package com.weimin.protocol;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

// 序列化
public interface Serializer {
    // 反序列化
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    // 序列化
    <T> byte[] serialize(T object);

    enum Algorithm implements Serializer {
        // jdk的序列化方式
        Java {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    return (T) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(object);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        },

        Json {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Class.class, new ClassCodec())
                        .registerTypeAdapter(Exception.class, new ExceptionCodec())
                        .create();
                String json = new String(bytes, StandardCharsets.UTF_8);
                return gson.fromJson(json, clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Class.class, new ClassCodec())
                        .registerTypeAdapter(Exception.class, new ExceptionCodec())
                        .create();
                String json = gson.toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        }

    }

    class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String asString = jsonElement.getAsString();

            try {
                return Class.forName(asString);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Class aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(aClass.getName());
        }
    }

    class ExceptionCodec implements JsonSerializer<Exception>, JsonDeserializer<Exception> {

        @Override
        public Exception deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String asString = jsonElement.getAsString();
            return new Exception(asString);
        }

        @Override
        public JsonElement serialize(Exception e, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(e.getClass() + ": " + e.getMessage());
        }
    }
}
