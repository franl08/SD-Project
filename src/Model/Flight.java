package Model;

import Utils.City;

import java.io.Serializable;
import java.time.LocalDate;

public class Flight implements Serializable {
    private String ID;
    private int nMaxPassengers;
    private int nReservations;
    private City origin;
    private City destination;
    private boolean toGo;
    private LocalDate date;

    public Flight(String ID, int nMaxPassengers, int nReservations, City origin, City destination, boolean toGo, LocalDate date) {
        this.ID = ID;
        this.nMaxPassengers = nMaxPassengers;
        this.nReservations = nReservations;
        this.origin = origin;
        this.destination = destination;
        this.toGo = toGo;
        this.date = date;
    }

    public Flight(Flight f){
        this.ID = f.getID();
        this.nMaxPassengers = f.getnMaxPassengers();
        this.nReservations = f.getnReservations();
        this.origin = f.getOrigin();
        this.destination = f.getDestination();
        this.toGo = f.getToGo();
        this.date = f.getDate();
    }

    public Flight clone(){
        return new Flight(this);
    }

    public String getID() {
        return this.ID;
    }

    public void setID(String id){
        this.ID = id;
    }

    public int getnMaxPassengers() {
        return this.nMaxPassengers;
    }

    public int getnReservations() {
        return this.nReservations;
    }

    public City getOrigin() {
        return this.origin;
    }

    public City getDestination() {
        return this.destination;
    }

    public boolean getToGo(){
        return this.toGo;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean removeOneReservation(){
        if(this.nReservations <= 0) return false;
        this.nReservations--;
        return true;
    }

    public boolean addOneReservation(){
        if(this.nReservations >= nMaxPassengers) return false;
        this.nReservations++;
        return true;
    }

    public boolean hasFreeSpace(){
        return nReservations < nMaxPassengers;
    }

    public boolean equals(Object o){
        if (this == o) return false;
        else if (o == null || o.getClass() != this.getClass()) return false;
        Flight f = (Flight) o;
        return f.getID().equals(this.ID);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(this.ID).append("\n")
                .append("Max Passengers: ").append(this.nMaxPassengers).append("\n")
                .append("Number Of Reservations: ").append(this.nReservations).append("\n")
                .append("Origin: ").append(this.origin.toString())
                .append("Destination: ").append(this.destination.toString()).append("\n")
                .append("Date: ").append(this.date).append("\n")
                .append("------------------------------------------------------------------\n");
        return sb.toString();
    }

}
