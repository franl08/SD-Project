package Model;

public class Flight {
    private String ID;
    private int nMaxPassengers;
    private int nReservations;
    private String origin;
    private String destination;

    public Flight(String ID, int nMaxPassengers, int nReservations, String origin, String destination) {
        this.ID = ID;
        this.nMaxPassengers = nMaxPassengers;
        this.nReservations = nReservations;
        this.origin = origin;
        this.destination = destination;
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
}
