package Model;

import Utils.City;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Route implements Serializable {
    private City origin;
    private City destination;
    private List<Flight> flights;

    public Route(){
        this.origin = null;
        this.destination = null;
        this.flights = new ArrayList<>();
    }

    public Route(City origin, City destination, List<Flight> flights){
        this.origin = origin;
        this.destination = destination;
        this.setFlights(flights);
    }

    public void setFlights(List<Flight> flights){
        this.flights = new ArrayList<>();
        for(Flight f : flights)
            this.flights.add(f.clone());
    }

    public List<Flight> getFlights(){
        List<Flight> ans = new ArrayList<>();
        for(Flight f : this.flights)
            ans.add(f.clone());
        return ans;
    }

    public City getOrigin() {
        return this.origin;
    }

    public void setOrigin(City origin) {
        this.origin = origin;
    }

    public City getDestination() {
        return this.destination;
    }

    public void setDestination(City destination) {
        this.destination = destination;
    }
}
