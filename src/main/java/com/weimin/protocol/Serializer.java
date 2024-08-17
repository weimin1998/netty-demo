package com.weimin.protocol;

import com.google.gson.Gson;

import java.io.*;
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
                String json = new String(bytes, StandardCharsets.UTF_8);
                return new Gson().fromJson(json, clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                String json = new Gson().toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        }

    }
}
