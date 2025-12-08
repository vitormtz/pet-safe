package com.example.petsafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.petsafe.api.ApiClient;
import com.example.petsafe.api.ApiService;
import com.example.petsafe.models.LoginRequest;
import com.example.petsafe.models.LoginResponse;
import com.example.petsafe.utils.FcmTokenManager;
import com.example.petsafe.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword;
    private TextView tvRegisterLink;
    private TextView tvErrorMessage;
    private View cvErrorMessage;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instalar splash screen antes do super.onCreate()
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Verificar se já está logado
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_welcome);

        // Inicializar API Service
        apiService = ApiClient.getApiService();

        initializeViews();
        setupListeners();
        setupBackPressedHandler();
    }

    private void initializeViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        cvErrorMessage = findViewById(R.id.cvErrorMessage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        tvForgotPassword.setOnClickListener(v -> {
            // TODO: Implementar recuperação de senha
        });

        tvRegisterLink.setOnClickListener(v -> {
            // Navegar para tela de cadastro
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        // Limpar erros anteriores
        tilEmail.setError(null);
        tilPassword.setError(null);
        hideErrorMessage();

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validações
        if (!validateEmail(email)) {
            return;
        }

        if (!validatePassword(password)) {
            return;
        }

        // Realizar login
        performLogin(email, password);
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_email));
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_password));
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performLogin(String email, String password) {
        showLoading(true);

        // Criar request de login
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Fazer chamada à API
        Call<LoginResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Login bem-sucedido
                    LoginResponse loginResponse = response.body();

                    // Salvar sessão
                    sessionManager.createLoginSession(
                            loginResponse.getAccessToken(),
                            loginResponse.getRefreshToken(),
                            loginResponse.getTokenType(),
                            loginResponse.getUser()
                    );

                    // Request notification permission and register FCM token
                    FcmTokenManager.requestNotificationPermissionAndRegisterToken(WelcomeActivity.this);

                    // Navegar para MainActivity
                    navigateToMain();
                } else {
                    // Login falhou
                    handleLoginError(response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Erro ao fazer login", t);

                // Mostrar erro de conexão na mensagem geral
                showErrorMessage(getString(R.string.error_network));
            }
        });
    }

    private void handleLoginError(int statusCode) {
        String errorMessage;
        switch (statusCode) {
            case 401:
                // Credenciais inválidas
                errorMessage = getString(R.string.error_login_failed);
                break;
            case 500:
                // Erro no servidor
                errorMessage = "Erro no servidor. Tente novamente mais tarde.";
                break;
            default:
                // Outro erro
                errorMessage = "Erro ao fazer login. Código: " + statusCode;
                break;
        }
        showErrorMessage(errorMessage);
    }

    private void showErrorMessage(String message) {
        tvErrorMessage.setText(message);
        cvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void hideErrorMessage() {
        cvErrorMessage.setVisibility(View.GONE);
    }

    private void navigateToMain() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    @Override
    public void onBackPressed() {
        // Desabilitar botão voltar na tela de boas-vindas
        // O usuário não deve voltar da tela inicial
        super.onBackPressed();
        finishAffinity();
    }

    // Migrar para OnBackPressedDispatcher (AndroidX compatível)
    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Desabilitar botão voltar na tela de boas-vindas
                // O usuário não deve voltar da tela inicial
                finishAffinity();
            }
        });
    }
}
