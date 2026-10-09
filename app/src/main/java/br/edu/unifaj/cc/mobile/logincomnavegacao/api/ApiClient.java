package br.edu.unifaj.cc.mobile.logincomnavegacao.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fábrica do Retrofit.
 *
 * A base URL aponta para o backend Spring Boot. No emulador do Android,
 * {@code 10.0.2.2} é o {@code localhost} da máquina que roda o servidor; em um
 * aparelho físico, troque pelo IP da máquina na mesma rede (ex.: 192.168.x.y).
 */
public class ApiClient {

    private static final String BASE_URL = "http://100.116.15.89:8080/";

    private static Retrofit retrofit;

    public static ApiService getService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
