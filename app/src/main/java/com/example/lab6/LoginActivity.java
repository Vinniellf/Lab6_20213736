package com.example.lab6;

// Asegúrate de añadir en tu module-level build.gradle:
// implementation 'com.facebook.android:facebook-login:15.0.1'

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilCorreo, tilContrasena;
    private TextInputEditText etCorreo, etContrasena;
    private MaterialButton btnLogin, btnGoogle, btnFacebook;
    private CheckBox checkRecordar;
    private TextView tvOlvidoContrasena, tvRegistrate;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences prefs;
    private CallbackManager callbackManager;

    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Antes de inicializar el SDK de Facebook, agrega el Client Token:
        // Define en res/values/strings.xml:
        // <string name="facebook_client_token">TU_CLIENT_TOKEN_DE_FACEBOOK</string>
        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_login_and_register);

        // Vistas
        tilCorreo = findViewById(R.id.tilCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        checkRecordar = findViewById(R.id.checkRecordar);
        tvOlvidoContrasena = findViewById(R.id.tvOlvidoContrasena);
        tvRegistrate = findViewById(R.id.tvRegistrate);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // "Recordarme"
        prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("remember", false)) {
            etCorreo.setText(prefs.getString("email", ""));
            etContrasena.setText(prefs.getString("password", ""));
            checkRecordar.setChecked(true);
        }

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Facebook Login
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new com.facebook.FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult result) {
                        handleFacebookAccessToken(result.getAccessToken());
                    }
                    @Override
                    public void onCancel() { }
                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(LoginActivity.this,
                                "Facebook login failed: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
        btnFacebook.setOnClickListener(v ->
                LoginManager.getInstance()
                        .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"))
        );

        // Listeners
        btnLogin.setOnClickListener(v -> loginUser());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        tvRegistrate.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        tvOlvidoContrasena.setOnClickListener(v -> resetPassword());
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            navigateToMain();
                        } else {
                            Toast.makeText(this,
                                    "Verifica tu correo para continuar.",
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(this,
                                "Auth failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUser() {
        tilCorreo.setError(null);
        tilContrasena.setError(null);
        String correo = etCorreo.getText().toString().trim();
        String password = etContrasena.getText().toString();
        if (correo.isEmpty()) { tilCorreo.setError("Ingresa tu correo"); etCorreo.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { tilCorreo.setError("Email inválido"); etCorreo.requestFocus(); return; }
        if (password.isEmpty()) { tilContrasena.setError("Ingresa tu contraseña"); etContrasena.requestFocus(); return; }
        if (password.length() < 6) { tilContrasena.setError("Al menos 6 caracteres"); etContrasena.requestFocus(); return; }
        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            savePreferences(correo, password);
                            navigateToMain();
                        } else {
                            Toast.makeText(this,
                                    "Debes verificar tu correo.",
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword() {
        String correo = etCorreo.getText().toString().trim();
        if (correo.isEmpty()) { tilCorreo.setError("Ingresa tu correo para restablecer"); etCorreo.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { tilCorreo.setError("Email inválido"); etCorreo.requestFocus(); return; }
        mAuth.sendPasswordResetEmail(correo)
                .addOnSuccessListener(aVoid -> Toast.makeText(this,
                        "Revisa tu correo para restablecer contraseña",
                        Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, authTask -> {
                            if (authTask.isSuccessful()) navigateToMain();
                        });
            } catch (ApiException e) {
                Toast.makeText(this,
                        "Google sign in fallido: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void navigateToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void savePreferences(String email, String password) {
        if (checkRecordar.isChecked()) {
            prefs.edit()
                    .putBoolean("remember", true)
                    .putString("email", email)
                    .putString("password", password)
                    .apply();
        } else {
            prefs.edit().clear().apply();
        }
    }
}
