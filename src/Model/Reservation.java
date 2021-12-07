package Model;

public class Reservation {
    private String ID;
    private String clientID;
    private String flightID;

    public Reservation(String ID, String clientID, String flightID){
        this.ID = ID;
        this.flightID = flightID;
    }

    public String getID() {
        return this.ID;
    }

    public String getFlightID() {
        return this.flightID;
    }
}
