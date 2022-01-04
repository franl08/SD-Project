package Model;

import Utils.City;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Route implements Serializable {

    private List<Flight> flights;

    public Route(List<Flight> flights){

        this.setFlights(flights);
    }

    public void setFlights(List<Flight> flights){
        this.flights = new ArrayList<>();
        for(Flight f : flights)
            this.flights.add(f.clone());
    }

    public Set<String> getFlightsIDs(){
        Set<String> ans = new HashSet<>();
        for(Flight f : this.flights)
            ans.add(f.getID());
        return ans;
    }

}
