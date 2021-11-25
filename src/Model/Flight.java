package Model;

public class Flight {
    private String ID;
    private int nMaxPassengers;
    private int nReserves;
    private String origin;
    private String destination;

    public Flight(String ID, int nMaxPassengers, int nReserves, String origin, String destination) {
        this.ID = ID;
        this.nMaxPassengers = nMaxPassengers;
        this.nReserves = nReserves;
        this.origin = origin;
        this.destination = destination;
    }

    public String getID() {
        return this.ID;
    }

    public int getnMaxPassengers() {
        return this.nMaxPassengers;
    }

    public int getnReserves() {
        return this.nReserves;
    }

    public String getOrigin() {
        return this.origin;
    }

    public String getDestination() {
        return this.destination;
    }
}
