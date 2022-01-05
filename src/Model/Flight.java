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
     * Origin of the flight
     */
    private final City origin;
    /**
     * Destination of the flight
     */
    private final City destination;

    /**
     * Parametrized constructor
     * @param ID ID
     * @param nMaxPassengers Maximum number of passengers
     * @param origin Origin
     * @param destination Destination
     */
    public Flight(String ID, int nMaxPassengers, City origin, City destination) {
        this.ID = ID;
        this.nMaxPassengers = nMaxPassengers;
        this.origin = origin;
        this.destination = destination;
    }

    /**
     * Copy constructor
     * @param f Flight
     */
    public Flight(Flight f){
        this.ID = f.getID();
        this.nMaxPassengers = f.getnMaxPassengers();
        this.origin = f.getOrigin();
        this.destination = f.getDestination();
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
                .append("Origin: ").append(this.origin.toString())
                .append(" -> Destination: ").append(this.destination.toString()).append("\n");
        sb.append("------------------------------------------------------------------\n");
        return sb.toString();
    }

}
