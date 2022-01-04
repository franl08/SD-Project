package Model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Class that stores a reservation
 */
public class Reservation implements Serializable {
    /**
     * ID of the reservation
     */
    private final String ID;
    /**
     * Client ID that owns the reservation
     */
    private final String clientID;
    /**
     * Set with the flights ID from the reservation
     */
    private Set<String> flightsID;

    /**
     * Parametrized constructor
     * @param ID Reservation ID
     * @param clientID Client ID
     * @param flightID Set of flights IDs
     */
    public Reservation(String ID, String clientID, Set<String> flightID){
        this.ID = ID;
        this.clientID = clientID;
        this.setFlightsID(flightID);
    }

    /**
     * Copy constructor
     * @param r Reservation
     */
    public Reservation(Reservation r){
        this.ID = r.getID();
        this.clientID = r.getClientID();
        this.flightsID = r.getFlightsID();
    }

    /**
     * Clones a reservation
     * @return Reservation
     */
    public Reservation clone(){
       return new Reservation(this);
    }

    /**
     * Gets the id of a reservation
     * @return ID
     */
    public String getID() {
        return this.ID;
    }

    /**
     * Gets the flight IDS
     * @return Set of flight IDS
     */
    public Set<String> getFlightsID() {
        return new HashSet<>(this.flightsID);
    }

    /**
     * Gets the client ID
     * @return Client ID
     */
    public String getClientID(){
        return this.clientID;
    }

    /**
     * Sets the flight IDS of a reservation
     * @param flightsID Flight IDS
     */
    public void setFlightsID(Set<String> flightsID){
        this.flightsID = new HashSet<>();
        this.flightsID.addAll(flightsID);
    }
}
