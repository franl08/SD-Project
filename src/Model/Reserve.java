package Model;

public class Reserve {
    private String ID;
    private String clientID;
    private String flightID;

    public Reserve(String ID, String clientID, String flightID){
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
