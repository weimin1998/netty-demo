package com.weimin.protocol;

import com.google.gson.*;
import org.junit.Test;

import java.lang.reflect.Type;

public class TestGson {
    @Test
    public void test() {
        System.out.println(new Gson().toJson(String.class));
    }

    @Test
    public void test1() {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();

        System.out.println(gson.toJson(String.class));
    }

    @Test
    public void test2() {
        Gson gson = new GsonBuilder().registerTypeAdapter(Exception.class, new ExceptionCodec()).create();
        System.out.println(gson.toJson(new Exception("test")));
    }

    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

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
    static class ExceptionCodec implements JsonSerializer<Exception>, JsonDeserializer<Exception> {

        @Override
        public Exception deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String asString = jsonElement.getAsString();

            return new Exception(asString);
        }

        @Override
        public JsonElement serialize(Exception e, Type type, JsonSerializationContext jsonSerializationContext) {
            System.out.println(e.getMessage());
            return new JsonPrimitive(e.getMessage());
        }
    }

}
