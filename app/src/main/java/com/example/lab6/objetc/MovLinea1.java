package com.example.lab6.objetc;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class MovLinea1 {
    private String id;                // document ID en Firestore
    private String cardId;            // ID de la tarjeta
    private Timestamp date;           // Fecha del movimiento
    private String stationIn;         // Estación de entrada
    private String stationOut;        // Estación de salida
    private long travelTimeSeconds;   // Tiempo de viaje en segundos

    public MovLinea1() { /* Constructor vacío para Firestore */ }

    public MovLinea1(String cardId, Timestamp date,
                    String stationIn, String stationOut, long travelTimeSeconds) {
        this.date = date;
        this.stationIn = stationIn;
        this.stationOut = stationOut;
        this.travelTimeSeconds = travelTimeSeconds;
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
    public String getStationIn() { return stationIn; }
    public void setStationIn(String stationIn) { this.stationIn = stationIn; }
    public String getStationOut() { return stationOut; }
    public void setStationOut(String stationOut) { this.stationOut = stationOut; }
    public long getTravelTimeSeconds() { return travelTimeSeconds; }
    public void setTravelTimeSeconds(long travelTimeSeconds) { this.travelTimeSeconds = travelTimeSeconds; }

    // Para rellenar el ID del documento al leer de Firestore
    public static MovLinea1 fromSnapshot(DocumentSnapshot snap) {
        MovLinea1 m = snap.toObject(MovLinea1.class);
        m.setId(snap.getId());
        return m;
    }
}
