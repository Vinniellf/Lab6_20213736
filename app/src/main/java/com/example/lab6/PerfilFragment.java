package com.example.lab6;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.lab6.databinding.FragmentPerfilBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PerfilFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseUser usuarioActual;

    public PerfilFragment() {
        // Constructor vacío requerido
    }
    private FragmentPerfilBinding binding;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        TextInputEditText etNombre = binding.etNombre;
        TextInputEditText etApellido = binding.etApellido;
        TextInputEditText etCorreo = binding.etCorreo;
        TextInputEditText etIDLinea1 = binding.etIDLinea1;
        TextInputEditText etIDLimaPass = binding.etIDLimaPass;

        // Cargar datos del usuario desde Firestore
        Log.d("SplashDebug", "usuario: " + usuarioActual);

        if (usuarioActual != null) {
            db.collection("usuarios").document(usuarioActual.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            etNombre.setText(document.getString("nombres"));
                            etApellido.setText(document.getString("apellidos"));
                            etCorreo.setText(usuarioActual.getEmail());
                            etIDLinea1.setText(document.getString("idLima1"));
                            etIDLimaPass.setText(document.getString("idLimaPass"));
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    );
        }



        binding.btnCerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // 🔐 Cierra sesión
            Toast.makeText(getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();

            //  Redirige a LoginActivity
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });


    }

}