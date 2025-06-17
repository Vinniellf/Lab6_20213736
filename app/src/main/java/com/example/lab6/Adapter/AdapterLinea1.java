package com.example.lab6.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6.R;
import com.example.lab6.objetc.MovLinea1;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdapterLinea1 extends RecyclerView.Adapter<AdapterLinea1.ViewHolder> {
    public interface OnItemAction {
        void onEdit(MovLinea1 m);
        void onDelete(MovLinea1 m);
    }

    private List<MovLinea1> data;
    private OnItemAction listener;
    private SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public AdapterLinea1(List<MovLinea1> data, OnItemAction listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_linea1, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        MovLinea1 m = data.get(pos);
        // --- PROTECCIÓN CONTRA NULOS EN LA FECHA ---
        if (m.getDate() != null) {
            // Si getDate() devuelve un com.google.firebase.Timestamp
            h.tvDate.setText("Fecha: " + fmt.format(m.getDate().toDate()));
        } else {
            // Placeholder si no hay fecha
            h.tvDate.setText("Fecha: --/--/----");
        }
        h.tvStations.setText("De: " + m.getStationIn() + " → " + m.getStationOut());
        h.tvTravelTime.setText("Tiempo: " + m.getTravelTimeSeconds() + "m");

        h.itemView.setOnLongClickListener(v -> {
            // Opciones de editar o eliminar
            listener.onEdit(m);
            return true;
        });
        /*
        h.itemView.setOnClickListener(v -> {
            listener.onDelete(m);
        });*/

        h.btnDelete.setOnClickListener(v -> {
            listener.onDelete(m);
        });
        h.btnEdit.setOnClickListener(v -> {
            listener.onEdit(m);
        });

    }

    @Override public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvDate, tvStations, tvTravelTime;
        Button btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvDate);
            tvStations = v.findViewById(R.id.tvStations);
            tvTravelTime = v.findViewById(R.id.tvTravelTime);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}