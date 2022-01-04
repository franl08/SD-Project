package Model;

import Utils.City;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Class that represents a flight
 */
public class Flight implements Serializable {
    /**
     * ID of the flight
     */
    private final String ID;
    /**
     * Maximum number of passengers
     */
    private final int nMaxPassengers;
    /**
     * Number of reserved seats in this flight
     */
    private int nReservations;
    /**
     * Origin of the flight
     */
    private final City origin;
    /**
     * Destination of the flight
     */
    private final City destination;
    /**
     * Represents if the flight hasn't left
     */
    private boolean toGo;
    /**
     * Date of the flight
     */
    private LocalDate date;

    /**
     * Parametrized constructor
     * @param ID ID
     * @param nMaxPassengers Maximum number of passengers
     * @param nReservations Number of reservations
     * @param origin Origin
     * @param destination Destination
     * @param toGo True if the flight hasn't left yet, otherwise false
     * @param date Date
     */
    public Flight(String ID, int nMaxPassengers, int nReservations, City origin, City destination, boolean toGo, LocalDate date) {
        this.ID = ID;
        this.nMaxPassengers = nMaxPassengers;
        this.nReservations = nReservations;
        this.origin = origin;
        this.destination = destination;
        this.toGo = toGo;
        this.date = date;
    }

    /**
     * Copy constructor
     * @param f Flight
     */
    public Flight(Flight f){
        this.ID = f.getID();
        this.nMaxPassengers = f.getnMaxPassengers();
        this.nReservations = f.getnReservations();
        this.origin = f.getOrigin();
        this.destination = f.getDestination();
        this.toGo = f.getToGo();
        this.date = f.getDate();
    }

    /**
     * Clones a flight
     * @return Flight
     */
    public Flight clone(){
        return new Flight(this);
    }

    /**
     * Gets the ID of the flight
     * @return ID
     */
    public String getID() {
        return this.ID;
    }

    /**
     * Get the maximum number of passengers
     * @return Maximum number of passengers
     */
    public int getnMaxPassengers() {
        return this.nMaxPassengers;
    }

    /**
     * Gets the number of reservations
     * @return Number of reservations
     */
    public int getnReservations() {
        return this.nReservations;
    }

    /**
     * Gets the origin of the flight
     * @return City of origin
     */
    public City getOrigin() {
        return this.origin;
    }

    /**
     * Gets the destination of the flight
     * @return City of destination
     */
    public City getDestination() {
        return this.destination;
    }

    /**
     * Gets if the flight hasn't already left
     * @return True if the flight hasn't left, false otherwise
     */
    public boolean getToGo(){
        return this.toGo;
    }

    /**
     * Sets the flight toGo status
     * @param bool New status
     */
    public void setToGo(boolean bool) {
        this.toGo = bool;
    }

    /**
     * Gets the date of the flight
     * @return Date
     */
    public LocalDate getDate() {
        return this.date;
    }

    /**
     * Removes one reservation
     * @return True if it was possible, false otherwise
     */
    public boolean removeOneReservation(){
        if(this.nReservations <= 0) return false;
        this.nReservations--;
        return true;
    }

    /**
     * Adds one reservation
     * @return True if it was possible, false otherwise
     */
    public boolean addOneReservation(){
        if(this.nReservations >= nMaxPassengers) return false;
        this.nReservations++;
        return true;
    }

    /**
     * Checks if there is free space in the flight
     * @return True if affirmative, false otherwise
     */
    public boolean hasFreeSpace(){
        return nReservations < nMaxPassengers;
    }

    /**
     * Checks if an object is equal to this flight
     * @param o Object
     * @return True if affirmative, false otherwise
     */
    public boolean equals(Object o){
        if (this == o) return false;
        else if (o == null || o.getClass() != this.getClass()) return false;
        Flight f = (Flight) o;
        return f.getID().equals(this.ID);
    }

    /**
     * Places the flight information in a string
     * @return String with the flight information
     */
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(this.ID).append("\n")
                .append("Max Passengers: ").append(this.nMaxPassengers).append("\n")
                .append("Number Of Reservations: ").append(this.nReservations).append("\n")
                .append("Origin: ").append(this.origin.toString())
                .append(" -> Destination: ").append(this.destination.toString()).append("\n")
                .append("Date: ").append(this.date).append("\n")
                .append("------------------------------------------------------------------\n");
        return sb.toString();
    }

}
