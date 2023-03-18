package com.DnD5eTools.interfaces;

import static com.DnD5eTools.util.Util.getServerConnection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AbstractInterface {
    private static ObjectMapper mapper;
    private static OkHttpClient client;

    public static void init() {
        if (client != null) {
            return;
        }

        X509TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }
        };

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { TRUST_ALL_CERTS }, new java.security.SecureRandom());
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslContext.getSocketFactory(), TRUST_ALL_CERTS);

            mapper = new ObjectMapper();
            client = new OkHttpClient.Builder().connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS,
                            ConnectionSpec.COMPATIBLE_TLS))
                    .sslSocketFactory(sslContext.getSocketFactory(), TRUST_ALL_CERTS)
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T getSingleResult(Class<T> classType, String endpoint) {
        AtomicReference<T> result = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(getServerConnection().getUrl() + endpoint)
                        .build();

                Response response = client.newCall(request).execute();
                result.set(mapper.readValue(response.body().string(), classType));
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

    public static <T> List<T> getListResult(TypeReference<List<T>> typeReference, String endpoint) {
        AtomicReference<List<T>> result = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(getServerConnection().getUrl() + endpoint)
                        .build();

                Response response = client.newCall(request).execute();
                result.set(mapper.readValue(response.body().string(), typeReference));
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

                client.newCall(request).execute();
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

    /**
     * executes a PUT request with an optional payload and expects a single object result
     * @param endpoint
     * @param payload
     * @param <T>
     */
    public static <T> T putSingleResult(Class<T> classType, String endpoint, T payload) {
        AtomicReference<T> result = new AtomicReference<>();
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
                        .put(body)
                        .build();

                Response response = client.newCall(request).execute();
                result.set(mapper.readValue(response.body().string(), classType));
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
     * executes a PUT request with an optional payload and expects no result
     * @param endpoint
     * @param payload
     * @param <T>
     */
    public static <T> void putNoResult(String endpoint, T payload) {
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
                        .put(body)
                        .build();

                client.newCall(request).execute();
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
