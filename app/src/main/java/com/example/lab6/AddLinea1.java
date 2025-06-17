package com.example.lab6;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6.databinding.ActivityAddLinea1Binding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AddLinea1 extends AppCompatActivity {

    private TextInputEditText etEstacionEntrada;
    private TextInputEditText etEstacionSalida;
    private TextInputEditText etFecha;
    private TextInputEditText etDuracion;
    private Button btnGuardar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ActivityAddLinea1Binding binding;

    private Calendar calendario = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddLinea1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Referencias a vistas
        etEstacionEntrada = findViewById(R.id.etEstacionEntrada);
        etEstacionSalida  = findViewById(R.id.etEstacionSalida);
        etFecha           = findViewById(R.id.etFecha);
        etDuracion        = findViewById(R.id.etDuracion);
        btnGuardar        = findViewById(R.id.btnGuardarMovimiento);

        // Firebase
        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnGuardar.setOnClickListener(v -> guardarMovimiento());

        binding.etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorFecha();
            }
        });
    }

    private void guardarMovimiento() {
        String entrada = etEstacionEntrada.getText().toString().trim();
        String salida  = etEstacionSalida.getText().toString().trim();
        String fechaStr= etFecha.getText().toString().trim();
        String durStr  = etDuracion.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(entrada) ||
                TextUtils.isEmpty(salida) ||
                TextUtils.isEmpty(fechaStr) ||
                TextUtils.isEmpty(durStr)) {
            Toast.makeText(this,
                    "Por favor completa todos los campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Parseo de fecha
        Date fecha;
        try {
            fecha = new SimpleDateFormat("dd/MM/yyyy",
                    java.util.Locale.getDefault())
                    .parse(fechaStr);
        } catch (ParseException e) {
            Toast.makeText(this,
                    "Formato de fecha inválido",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int duracionMin;
        try {
            duracionMin = Integer.parseInt(durStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Duración inválida",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Datos a guardar
        Map<String,Object> movimiento = new HashMap<>();
        movimiento.put("cardId",          entrada);
        movimiento.put("date",            fecha);
        movimiento.put("stationIn",       entrada);
        movimiento.put("stationOut",      salida);
        movimiento.put("travelTimeSeconds", duracionMin);
        // Usuario actual
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this,
                    "Debes iniciar sesión primero",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Referencia a subcolección: users/{uid}/movimientos-linea1
        CollectionReference movRef = db
                .collection("usuarios")
                .document(user.getUid())
                .collection("movimientos-linea1");

        // Guardar
        movRef.add(movimiento)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this,
                            "Movimiento guardado",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(this,
                            "Error al guardar: " + err.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void mostrarSelectorFecha() {
        final Calendar c2 = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendario.set(Calendar.YEAR, year);
            calendario.set(Calendar.MONTH, month);
            calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            c2.set(Calendar.YEAR, year);
            c2.set(Calendar.MONTH, month);
            c2.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            binding.etFecha.setText(android.text.format.DateFormat.format("dd/MM/yyyy", c2));

        };

        new DatePickerDialog(AddLinea1.this, dateSetListener,
                calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show();
    }
}
