package com.example.lab6;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lab6.Adapter.AdapterLinea1;
import com.example.lab6.databinding.FragmentLima1Binding;
import com.example.lab6.objetc.MovLinea1;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Lima1Fragment extends Fragment {
    private FragmentLima1Binding binding;
    private AdapterLinea1 adapter;
    private final List<MovLinea1> list = new ArrayList<>();
    private CollectionReference movRef;
    private final Calendar calendar = Calendar.getInstance();
    private Date filterStart = null;
    private Date filterEnd = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLima1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // RecyclerView setup
        binding.listaMovLinea1.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterLinea1(list, new AdapterLinea1.OnItemAction() {
            @Override
            public void onEdit(MovLinea1 m) { showEditDialog(m); }
            @Override
            public void onDelete(MovLinea1 m) { deleteMovement(m); }
        });
        binding.listaMovLinea1.setAdapter(adapter);

        // Firestore reference under usuarios/{uid}/movimientos-linea1
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            movRef = FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(user.getUid())
                    .collection("movimientos-linea1");
            loadMovements();
        }

        // Date filter start
        binding.etFilterStart.setOnClickListener(v -> showDatePicker(date -> {
            filterStart = date;
            binding.etFilterStart.setText(formatDate(date));
        }));

        // Date filter end
        binding.etFilterEnd.setOnClickListener(v -> showDatePicker(date -> {
            filterEnd = date;
            binding.etFilterEnd.setText(formatDate(date));
        }));

        // Apply filter
        binding.btnFilter.setOnClickListener(v -> applyFilter());

        // Add new movement
        binding.btnAdd.setOnClickListener(v -> showCreateDialog());

        return root;
    }

    // Interface for date picked callback
    private interface DatePicked { void onPicked(Date date); }

    private void showDatePicker(DatePicked callback) {
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    callback.onPicked(calendar.getTime());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // Load movements without filter
    private void loadMovements() {
        Query query = movRef.orderBy("date", Query.Direction.DESCENDING);
        executeQuery(query);
    }

    // Apply date range filter
    private void applyFilter() {
        Query query = movRef;
        if (filterStart != null) {
            query = query.whereGreaterThanOrEqualTo("date", new Timestamp(filterStart));
        }
        if (filterEnd != null) {
            Calendar c2 = Calendar.getInstance();
            c2.setTime(filterEnd);
            c2.add(Calendar.DATE, 1);
            query = query.whereLessThan("date", new Timestamp(c2.getTime()));
        }
        query = query.orderBy("date", Query.Direction.DESCENDING);
        executeQuery(query);
    }

    // Execute query and update UI
    private void executeQuery(Query query) {
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                list.clear();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    MovLinea1 m = MovLinea1.fromSnapshot(doc);
                    m.setCardId(df.format(m.getDate().toDate()));
                    list.add(m);
                }
                adapter.notifyDataSetChanged();
                binding.NoHayMovimientos.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    // Create with selected date
    private void createMovement(Date datePicked, String dateStr,
                                String stationIn, String stationOut, long travelSec) {
        MovLinea1 m = new MovLinea1(
                dateStr,
                new Timestamp(datePicked),
                stationIn,
                stationOut,
                travelSec
        );
        movRef.add(m)
                .addOnSuccessListener(doc ->
                        Toast.makeText(getContext(), "Movimiento agregado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(err ->
                        Toast.makeText(getContext(), "Error: " + err.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Update with selected date
    private void updateMovement(MovLinea1 m, Date datePicked) {
        m.setDate(new Timestamp(datePicked));
        movRef.document(m.getId())
                .set(m)
                .addOnSuccessListener(a ->
                        Toast.makeText(getContext(), "Movimiento actualizado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(err ->
                        Toast.makeText(getContext(), "Error: " + err.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Delete
    private void deleteMovement(MovLinea1 m) {
        movRef.document(m.getId())
                .delete()
                .addOnSuccessListener(a ->
                        Toast.makeText(getContext(), "Movimiento eliminado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(err ->
                        Toast.makeText(getContext(), "Error: " + err.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Show create dialog
    private void showCreateDialog() {
        View form = LayoutInflater.from(getContext()).inflate(R.layout.dialog_movement, null);
        TextInputEditText etFecha = form.findViewById(R.id.etFecha);
        TextInputEditText etStationIn = form.findViewById(R.id.etStationIn);
        TextInputEditText etStationOut = form.findViewById(R.id.etStationOut);
        TextInputEditText etTravelTime = form.findViewById(R.id.etTravelTime);

        final Date[] pickedDate = {null};
        etFecha.setOnClickListener(v -> showDatePicker(date -> {
            pickedDate[0] = date;
            etFecha.setText(formatDate(date));
        }));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Nuevo Movimiento")
                .setView(form)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    if (pickedDate[0] == null) {
                        Toast.makeText(getContext(), "Selecciona una fecha", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String dateStr = etFecha.getText().toString().trim();
                    String in = etStationIn.getText().toString().trim();
                    String out = etStationOut.getText().toString().trim();
                    String tStr = etTravelTime.getText().toString().trim();
                    if (TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(in) || TextUtils.isEmpty(out) || TextUtils.isEmpty(tStr)) {
                        Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long secs;
                    try { secs = Long.parseLong(tStr); } catch (NumberFormatException ex) {
                        Toast.makeText(getContext(), "Duración inválida", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createMovement(pickedDate[0], dateStr, in, out, secs);
                    dialog.dismiss();
                }));
        dialog.show();
    }

    // Show edit dialog
    private void showEditDialog(MovLinea1 m) {
        View form = LayoutInflater.from(getContext()).inflate(R.layout.dialog_movement, null);
        TextInputEditText etFecha = form.findViewById(R.id.etFecha);
        TextInputEditText etStationIn = form.findViewById(R.id.etStationIn);
        TextInputEditText etStationOut = form.findViewById(R.id.etStationOut);
        TextInputEditText etTravelTime = form.findViewById(R.id.etTravelTime);

        final Date[] pickedDate = {m.getDate().toDate()};
        etFecha.setText(formatDate(pickedDate[0]));
        etStationIn.setText(m.getStationIn());
        etStationOut.setText(m.getStationOut());
        etTravelTime.setText(String.valueOf(m.getTravelTimeSeconds()));

        etFecha.setOnClickListener(v -> showDatePicker(date -> {
            pickedDate[0] = date;
            etFecha.setText(formatDate(date));
        }));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Editar Movimiento")
                .setView(form)
                .setPositiveButton("Actualizar", null)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    if (pickedDate[0] == null) {
                        Toast.makeText(getContext(), "Selecciona una fecha", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String dateStr = etFecha.getText().toString().trim();
                    String in = etStationIn.getText().toString().trim();
                    String out = etStationOut.getText().toString().trim();
                    String tStr = etTravelTime.getText().toString().trim();
                    if (TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(in) || TextUtils.isEmpty(out) || TextUtils.isEmpty(tStr)) {
                        Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long secs;
                    try { secs = Long.parseLong(tStr); } catch (NumberFormatException ex) {
                        Toast.makeText(getContext(), "Duración inválida", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    m.setCardId(dateStr);
                    updateMovement(m, pickedDate[0]);
                    m.setStationIn(in);
                    m.setStationOut(out);
                    m.setTravelTimeSeconds(secs);
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }
}
