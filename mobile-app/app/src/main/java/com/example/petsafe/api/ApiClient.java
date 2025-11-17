package com.example.petsafe.api;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String TAG = "ApiClient";

    // ========== CONFIGURAÇÃO DA URL DA API ==========
    // Escolha a URL apropriada para seu ambiente de teste:

    // 1. Para emulador Android (API rodando em localhost da máquina):
    private static final String EMULATOR_URL = "http://10.0.2.2:8080/api/v1/";

    // 2. Para dispositivo físico (API rodando em localhost - substitua pelo IP da sua máquina):
    // Use "ipconfig" (Windows) ou "ifconfig" (Linux/Mac) para descobrir seu IP local
    private static final String PHYSICAL_DEVICE_URL = "http://192.168.1.100:8080/api/v1/";

    // 3. VM da faculdade - PetSafe Backend
    private static final String PRODUCTION_URL = "http://177.44.248.27:8080/api/v1/";

    // ========== SELECIONE A URL ATIVA AQUI ==========
    // URL configurada para a VM da faculdade
    private static final String BASE_URL = PRODUCTION_URL;

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    /**
     * Retorna uma instância singleton do Retrofit configurada
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            Log.d(TAG, "Inicializando ApiClient com URL: " + BASE_URL);

            // Configurar logging interceptor para debug
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                Log.d(TAG, "HTTP: " + message)
            );
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configurar OkHttpClient com timeout e interceptor
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .retryOnConnectionFailure(true)
                    .build();

            // Criar instância do Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Log.d(TAG, "ApiClient inicializado com sucesso");
        }
        return retrofit;
    }

    /**
     * Retorna uma instância singleton do ApiService
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Reseta o cliente (útil para testes ou mudanças de configuração)
     */
    public static void resetClient() {
        retrofit = null;
        apiService = null;
    }

    /**
     * Define uma nova URL base
     */
    public static void setBaseUrl(String baseUrl) {
        resetClient();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
