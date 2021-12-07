package Model;

public class Flight {
    private String ID;
    private int nMaxPassengers;
    private int nReservations;
    private String origin;
    private String destination;
    private boolean toGo;

    public Flight(String ID, int nMaxPassengers, int nReservations, String origin, String destination, boolean toGo) {
        this.ID = ID;
        this.nMaxPassengers = nMaxPassengers;
        this.nReservations = nReservations;
        this.origin = origin;
        this.destination = destination;
        this.toGo = toGo;
    }

    public String getID() {
        return this.ID;
    }

    public int getnMaxPassengers() {
        return this.nMaxPassengers;
    }

    public int getnReservations() {
        return this.nReservations;
    }

    public String getOrigin() {
        return this.origin;
    }

    public String getDestination() {
        return this.destination;
    }

    public boolean getToGo(){
        return this.toGo;
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

}
