package Model;

import Client.Client;
import Client.User;

import java.util.Map;
import java.util.Set;

public class Model {
    private Map<String, User> users;
    private Map<String, Flight> flights;
    private Map<String, Reserve> reserves;


    public boolean checkAutentication(String username, String password){
        if(!users.containsKey(username)) return false;
        User u = users.get(username);
        return u.getPassword().equals(password);
    }

    public void addUser(User u){
        this.users.put(u.getID(), u);
    }

    public void addFlight(Flight f){
        this.flights.put(f.getID(), f);
    }

    public void addReserve(Reserve r){
        this.reserves.put(r.getID(), r);
    }

    public Reserve getReserve(String s){
        return reserves.get(s);
    }

    public Flight getFlight(String s){
        return flights.get(s);
    }

    public boolean removeFlight(String s){
        if(!this.flights.containsKey(s)) return false;
        this.flights.remove(s);
        return true;
    }

    // TODO Remove reserves from each client
    public boolean removeReserve(String s){
        if(!this.reserves.containsKey(s)) return false;
        this.reserves.remove(s);
        return true;
    }

    public void deleteUser(String ID){
        this.users.remove(ID);
    }

    public boolean createFlight(String ID, int nMaxPassengers, int nReserve, String origin, String destination){
        if(flights.containsKey(ID)) return false;
        Flight f = new Flight(ID, nMaxPassengers, nReserve, origin, destination);
        this.flights.put(f.getID(), f);
        return true;
    }

    public boolean createReserve(String ID, String clientID, String flightID){
        if(reserves.containsKey(ID)) return false;
        Reserve r = new Reserve(ID, clientID, flightID);
        Client c = (Client) this.users.get(clientID);
        Map<String, Reserve> clientReserves = c.getReserves();
        if (!reserves.containsKey(r.getID())) c.addReserve(r);
        this.reserves.put(r.getID(), r);
        return true;
    }
}
