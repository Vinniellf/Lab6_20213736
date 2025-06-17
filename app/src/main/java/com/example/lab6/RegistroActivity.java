package com.example.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputLayout tilNombres, tilApellidos, tilLima1, tilLimaPass, tilCorreo, tilNuevaContrasena, tilConfirmarContrasena;
    private TextInputEditText etNombres, etApellidos, etLima1, etLimaPass, etCorreo, etNuevaContrasena, etConfirmarContrasena;
    private MaterialButton btnContinuar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicializar vistas
        btnBack = findViewById(R.id.btnBack);
        tilNombres = findViewById(R.id.tilNombres);
        tilApellidos = findViewById(R.id.tilApellidos);
        tilLima1 = findViewById(R.id.tilLima1);
        tilLimaPass = findViewById(R.id.tilLimaPass);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilNuevaContrasena = findViewById(R.id.tilNuevaContrasena);
        tilConfirmarContrasena = findViewById(R.id.tilConfirmarContrasena);

        etNombres = findViewById(R.id.etNombres);
        etApellidos = findViewById(R.id.etApellidos);
        etLima1 = findViewById(R.id.etLima1);
        etLimaPass = findViewById(R.id.etLimaPass);
        etCorreo = findViewById(R.id.etCorreo);
        etNuevaContrasena = findViewById(R.id.etNuevaContrasena);
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);

        btnContinuar = findViewById(R.id.btnContinuar);

        // Instancias de Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());
        btnContinuar.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        // Limpiar errores previos
        tilNombres.setError(null);
        tilApellidos.setError(null);
        tilLima1.setError(null);
        tilLimaPass.setError(null);
        tilCorreo.setError(null);
        tilNuevaContrasena.setError(null);
        tilConfirmarContrasena.setError(null);

        // Obtener valores
        String nombres = etNombres.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String lima1 = etLima1.getText().toString().trim();
        String limaPass = etLimaPass.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String password = etNuevaContrasena.getText().toString();
        String confirmPassword = etConfirmarContrasena.getText().toString();

        // Validaciones
        if (nombres.isEmpty()) {
            tilNombres.setError("Ingresa tus nombres");
            etNombres.requestFocus();
            return;
        }
        if (apellidos.isEmpty()) {
            tilApellidos.setError("Ingresa tus apellidos");
            etApellidos.requestFocus();
            return;
        }
        if (lima1.isEmpty()) {
            tilLima1.setError("Ingresa el ID de tu tarjeta Lima 1");
            etLima1.requestFocus();
            return;
        }
        if (limaPass.isEmpty()) {
            tilLimaPass.setError("Ingresa el ID de tu tarjeta Lima Pass");
            etLimaPass.requestFocus();
            return;
        }
        if (correo.isEmpty()) {
            tilCorreo.setError("Ingresa tu correo electrónico");
            etCorreo.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Ingresa un correo válido");
            etCorreo.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            tilNuevaContrasena.setError("Ingresa una contraseña");
            etNuevaContrasena.requestFocus();
            return;
        }
        if (password.length() < 6) {
            tilNuevaContrasena.setError("La contraseña debe tener al menos 6 caracteres");
            etNuevaContrasena.requestFocus();
            return;
        }
        if (confirmPassword.isEmpty()) {
            tilConfirmarContrasena.setError("Confirma tu contraseña");
            etConfirmarContrasena.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmarContrasena.setError("Las contraseñas no coinciden");
            etConfirmarContrasena.requestFocus();
            return;
        }

        btnContinuar.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, task -> {
                    btnContinuar.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Enviar correo de verificación
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(verifTask -> {
                                        if (verifTask.isSuccessful()) {
                                            // Guardar datos adicionales en Firestore
                                            String uid = firebaseUser.getUid();
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("nombres", nombres);
                                            userData.put("apellidos", apellidos);
                                            userData.put("idLima1", lima1);
                                            userData.put("idLimaPass", limaPass);
                                            userData.put("email", correo);

                                            db.collection("usuarios")
                                                    .document(uid)
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(this,
                                                                "Registro exitoso. Revisa tu correo para verificar la cuenta.",
                                                                Toast.LENGTH_LONG).show();
                                                        mAuth.signOut();
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(this,
                                                            "Error al guardar datos: " + e.getMessage(),
                                                            Toast.LENGTH_LONG).show());
                                        } else {
                                            Toast.makeText(this,
                                                    "No se pudo enviar el correo de verificación.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Error de registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
