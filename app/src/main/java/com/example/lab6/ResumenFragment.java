package com.example.lab6;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.lab6.databinding.FragmentResumenBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResumenFragment extends Fragment {
    private FragmentResumenBinding binding;
    private final Calendar calendar = Calendar.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResumenBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Selector fechas
        binding.etFrom.setFocusable(false);
        binding.etFrom.setOnClickListener(v -> pickDate(binding.etFrom));
        binding.etTo.setFocusable(false);
        binding.etTo.setOnClickListener(v -> pickDate(binding.etTo));

        // Botón aplicar filtro
        binding.btnApply.setOnClickListener(v -> {
            String inicio = binding.etFrom.getText().toString();
            String fin = binding.etTo.getText().toString();
            cargarDatos(inicio.isEmpty() ? null : inicio,
                    fin.isEmpty() ? null : fin);
        });

        // Carga inicial sin filtro
        cargarDatos(null, null);
        return root;
    }

    private void pickDate(final com.google.android.material.textfield.TextInputEditText et) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(year, month, day);
                    String formatted = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(sel.getTime());
                    et.setText(formatted);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dp.show();
    }

    private void cargarDatos(String inicio, String fin) {
        BarChart barChart = binding.barChart;
        PieChart pieChart = binding.pieChart;
        barChart.clear(); pieChart.clear();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String,Integer> countL1 = new HashMap<>();
        Map<String,Integer> countLP = new HashMap<>();
        int[] countTrain = {0}, countBus = {0};

        Query q1 = db.collection("usuarios").document(uid).collection("movimientos-linea1");
        if (inicio != null && fin != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dStart = sdf.parse(inicio);
                Date dEnd = sdf.parse(fin);
                Calendar c2 = Calendar.getInstance();
                c2.setTime(dEnd);
                c2.add(Calendar.DATE,1);
                Date dEndPlus = c2.getTime();
                q1 = q1.whereGreaterThanOrEqualTo("date", dStart)
                        .whereLessThan("date", dEndPlus);
            } catch(ParseException e) { e.printStackTrace(); }
        }
        q1.get().addOnSuccessListener(snap1 -> {
            SimpleDateFormat monthFmt = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
            for(QueryDocumentSnapshot doc: snap1) {
                Date dt = doc.getTimestamp("date").toDate();
                String mes = monthFmt.format(dt);
                countL1.put(mes, countL1.getOrDefault(mes,0)+1);
                countTrain[0]++;
            }
            Query q2 = db.collection("usuarios").document(uid).collection("movimientos-limaPass");
            if (inicio != null && fin != null) {
                try{
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date dStart = sdf.parse(inicio);
                    Date dEnd = sdf.parse(fin);
                    Calendar c2 = Calendar.getInstance();
                    c2.setTime(dEnd);
                    c2.add(Calendar.DATE,1);
                    Date dEndPlus = c2.getTime();
                    q2 = q2.whereGreaterThanOrEqualTo("date", dStart)
                            .whereLessThan("date", dEndPlus);
                } catch(ParseException e){ e.printStackTrace(); }
            }
            q2.get().addOnSuccessListener(snap2 -> {
                SimpleDateFormat monthFmt2 = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                for(QueryDocumentSnapshot doc2: snap2) {
                    Date dt2 = doc2.getTimestamp("date").toDate();
                    String mes = monthFmt2.format(dt2);
                    countLP.put(mes, countLP.getOrDefault(mes,0)+1);
                    countBus[0]++;
                }
                mostrarGraficoBarras(barChart, countL1, countLP);
                mostrarGraficoTorta(pieChart, countTrain[0], countBus[0]);
            }).addOnFailureListener(e -> Toast.makeText(getContext(),"Error LP: "+e.getMessage(),Toast.LENGTH_LONG).show());
        }).addOnFailureListener(e -> Toast.makeText(getContext(),"Error L1: "+e.getMessage(),Toast.LENGTH_LONG).show());
    }

    private void mostrarGraficoBarras(BarChart chart, Map<String,Integer> l1, Map<String,Integer> lp) {
        List<String> meses = new ArrayList<>(l1.keySet());
        for(String m: lp.keySet()) if(!meses.contains(m)) meses.add(m);
        Collections.sort(meses);
        List<BarEntry> e1 = new ArrayList<>(), e2 = new ArrayList<>();
        for(int i=0;i<meses.size();i++){
            String mes=meses.get(i);
            e1.add(new BarEntry(i, l1.getOrDefault(mes,0)));
            e2.add(new BarEntry(i, lp.getOrDefault(mes,0)));
        }
        BarDataSet ds1=new BarDataSet(e1,"Línea 1");
        BarDataSet ds2=new BarDataSet(e2,"Lima Pass");
        ds1.setColor(0xFFE53935); ds2.setColor(0xFF1E88E5);
        float groupSpace=0.3f, barSpace=0.05f;
        float barWidth=(1f-groupSpace)/2f-barSpace;
        BarData data=new BarData(ds1,ds2);
        data.setBarWidth(barWidth);
        chart.setData(data);
        chart.getXAxis().setAxisMinimum(0f);
        chart.getXAxis().setAxisMaximum(data.getGroupWidth(groupSpace,barSpace)*meses.size());
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setValueFormatter(new ValueFormatter(){
            @Override public String getFormattedValue(float value){int idx=(int)value;return idx>=0&&idx<meses.size()?meses.get(idx):"";}
        });
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getAxisRight().setEnabled(false);
        chart.setFitBars(true);
        chart.groupBars(0f,groupSpace,barSpace);
        chart.invalidate();
    }

    private void mostrarGraficoTorta(PieChart chart, int tren, int bus) {
        List<PieEntry> entries=new ArrayList<>();
        if(tren>0) entries.add(new PieEntry(tren,"Tren"));
        if(bus>0) entries.add(new PieEntry(bus,"Bus"));
        PieDataSet ds=new PieDataSet(entries,"Uso de Tarjetas");
        ds.setColors(new int[]{0xFFE53935,0xFF1E88E5});
        PieData data=new PieData(ds);
        chart.setData(data);
        chart.setUsePercentValues(true);
        Description desc=new Description(); desc.setText(""); chart.setDescription(desc);
        chart.invalidate();
    }
}
