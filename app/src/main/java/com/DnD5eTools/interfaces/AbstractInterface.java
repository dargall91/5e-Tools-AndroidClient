package com.DnD5eTools.interfaces;

import static com.DnD5eTools.util.Util.getServerConnection;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AbstractInterface {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();

    public static <T> T getSingleResult(Class<T> classType, String endpoint) {
        AtomicReference<T> result = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getServerConnection().getUrl() + endpoint);
                result.set(mapper.readValue(url, classType));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result.get();
    }

    public static <T> T[] getArrayResult(Class<T[]> classType, String endpoint) {
        AtomicReference<T[]> result = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getServerConnection().getUrl() + endpoint);
                result.set(mapper.readValue(url, classType));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result.get();
    }

    /**
     * executes a POST request with an optional payload
     * @param endpoint
     * @param payload
     * @param <T>
     */
    public static <T> void postNoResult(String endpoint, T payload) {
        Thread thread = new Thread(() -> {
            try {
                RequestBody body;
                if (payload != null) {
                    String payloadString = mapper.writer().writeValueAsString(payload);
                     body = RequestBody.create(payloadString, MediaType.parse("application/json"));
                } else {
                    body = RequestBody.create("", null);
                }
                Request request = new Request.Builder()
                        .url(getServerConnection().getUrl() + endpoint)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void deleteNoResult(String endpoint) {
        Thread thread = new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(getServerConnection().getUrl() + endpoint)
                        .delete()
                        .build();

                Response response = client.newCall(request).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
