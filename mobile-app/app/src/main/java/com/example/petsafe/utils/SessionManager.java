package com.example.petsafe.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.petsafe.models.User;
import com.google.gson.Gson;

public class SessionManager {

    private static final String PREF_NAME = "PetSafeSession";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_USER = "user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    /**
     * Salva os dados da sessão do usuário
     */
    public void createLoginSession(String accessToken, String refreshToken, String tokenType, User user) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_TOKEN_TYPE, tokenType);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Verifica se o usuário está logado
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Retorna o access token
     */
    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Retorna o refresh token
     */
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Retorna o token type (geralmente "Bearer")
     */
    public String getTokenType() {
        return sharedPreferences.getString(KEY_TOKEN_TYPE, "Bearer");
    }

    /**
     * Retorna o usuário logado
     */
    public User getUser() {
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }

    /**
     * Atualiza o access token
     */
    public void updateAccessToken(String accessToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.apply();
    }

    /**
     * Atualiza os dados do usuário
     */
    public void updateUser(User user) {
        editor.putString(KEY_USER, gson.toJson(user));
        editor.apply();
    }

    /**
     * Limpa a sessão (logout)
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Retorna o header de autorização completo
     */
    public String getAuthorizationHeader() {
        String token = getAccessToken();
        if (token != null) {
            return getTokenType() + " " + token;
        }
        return null;
    }
}
