package com.example.lab6;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.linea1) {
                loadFragment(new Lima1Fragment());
            } else if (id == R.id.limaPass) {
                loadFragment(new LimaPassFragment());
            } else if (id == R.id.resumen) {
                loadFragment(new ResumenFragment());
            } else if (id == R.id.perfil) {
                loadFragment(new PerfilFragment());
            }
            return true;
        });

        if (savedInstanceState == null) {
            loadFragment(new Lima1Fragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.adminhotel_container_view, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}