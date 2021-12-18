package Model;

import java.util.HashSet;
import java.util.Set;

public class Reservation {
    private String ID;
    private String clientID;
    private Set<String> flightsID;

    public Reservation(String ID, String clientID, Set<String> flightID){
        this.ID = ID;
        this.clientID = clientID;
        this.flightsID = flightID;
    }

    public Reservation(Reservation r){
        this.ID = r.getID();
        this.clientID = r.getClientID();
        this.flightsID = r.getFlightsID();
    }

    public Reservation clone(){
        return new Reservation(this);
    }

    public String getID() {
        return this.ID;
    }

    public Set<String> getFlightsID() {
        return new HashSet<>(this.flightsID);
    }

    public String getClientID(){
        return this.clientID;
    }

    public void setFlightsID(Set<String> flightsID){
        this.flightsID = new HashSet<>();
        this.flightsID.addAll(flightsID);
    }

    public boolean isToFlight(String id){
        return this.ID.equals(id);
    }

    public boolean isFromUser(String id){
        return this.ID.equals(clientID);
    }

}
