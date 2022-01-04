package Model;

import Exceptions.*;
import Utils.City;
import Utils.Utilities;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Model implements Serializable {

    private Map<String, String> clients;
    private Map<String, Flight> flights;
    private Map<String, Reservation> reservations;
    private Map<String, Set<String>> clientReservations;
    private Set<LocalDate> closedDays; // Point 4. of basic functionalities in utterance

    public ReadWriteLock l = new ReentrantReadWriteLock();

    public Model() {
        this.clients = new HashMap<>();
        this.flights = new HashMap<>();
        this.reservations = new HashMap<>();
        this.closedDays = new HashSet<>();
    }

    public Model(Map<String, String> clients, Map<String, Flight> flights, Map<String, Reservation> reservations, Map<String, Set<String>> clientReservations, Set<LocalDate> closedDays){
        this.clients = clients;
        this.flights = flights;
        this.reservations = reservations;
        this.clientReservations = clientReservations;
        this.closedDays = closedDays;
    }

    public Model(Model m) {
        this.clients = m.getClients();
        this.flights = m.getFlights();
        this.reservations = m.getReservations();
        this.clientReservations = m.getClientReservations();
        this.closedDays = m.getClosedDays();
    }

    public Model clone() {
        return new Model(this);
    }

    public Map<String, String> getClients(){
        return new HashMap<>(this.clients);
    }

    public Map<String, Flight> getFlights(){
        Map<String, Flight> flights = new HashMap<>();
        if(!this.flights.isEmpty())
            for(String code : this.flights.keySet())
                flights.put(code, flights.get(code).clone());
        return flights;
    }

    public Map<String, Reservation> getReservations(){
        Map<String, Reservation> reservations = new HashMap<>();
        if(!this.reservations.isEmpty())
            for(String code : this.reservations.keySet())
                reservations.put(code, reservations.get(code).clone());
        return reservations;
    }

    public Map<String,Set<String>> getClientReservations() {
        Map<String,Set<String>> clientReservations = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : this.clientReservations.entrySet())
            clientReservations.put(entry.getKey(), new HashSet<>(entry.getValue()));

        return clientReservations;
    }

    public Set<LocalDate> getClosedDays(){
        return new HashSet<>(this.closedDays);
    }

    public void setClosedDays(Set<LocalDate> dates){
        this.closedDays = new HashSet<>();
        this.closedDays.addAll(dates);
    }

    public boolean checkAuthentication(String username, String password){
        if(!this.clients.containsKey(username)) return false;
        return this.clients.get(username).equals(password);
    }

    public boolean lookupUser(String username) {
        return this.clients.containsKey(username);
    }

    public void addClient(String email, String password) throws EmailAlreadyExistsException {
        if (this.clients.containsKey(email)) throw new EmailAlreadyExistsException();
        this.clients.put(email,password);
    }

    public void addFlight(Flight f){
        f.setID(this.generateFlightID());
        this.flights.put(f.getID(), f.clone());
    }

    public void addReservation(String email, Reservation r) throws FlightDoesntExistException{
        Set<String> fIDs = r.getFlightsID();
        for(String id : fIDs){
            if(!this.flights.containsKey(id)) throw new FlightDoesntExistException("There isn't any flight with ID " + id);
        }

        r.setID(this.generateReservationID());
        this.reservations.put(r.getID(), r.clone());

        Set<String> reserv;

        if (this.clientReservations.containsKey(email))
            reserv = this.clientReservations.get(email);
        else
            reserv = new HashSet<>();

        reserv.add(r.getID());
        this.clientReservations.put(email, reserv);
    }

    public Reservation getReservation(String s) throws ReservationDoesntExistException {
        if(this.reservations.containsKey(s)) return this.reservations.get(s).clone();
        else throw new ReservationDoesntExistException("There isn't any reservation with ID " + s);
    }

    public Flight getFlight(String s) throws FlightDoesntExistException {
        if(this.reservations.containsKey(s)) return this.flights.get(s).clone();
        else throw new FlightDoesntExistException("There isn't any flight with ID " + s);
    }

    public String getFlightsString(){
        StringBuilder sb = new StringBuilder();
        for(String id : this.flights.keySet())
            sb.append(this.flights.get(id).toString());
        return (!sb.isEmpty()) ? sb.toString() : "No flights to show";
    }

    public String getFlightsStringInDate(LocalDate date){
        StringBuilder sb = new StringBuilder();
        for(String id : this.flights.keySet())
            if(this.flights.get(id).getDate().equals(date)) sb.append(this.flights.get(id).toString());
        return (!sb.isEmpty()) ? sb.toString() : "No flights to show";
    }

    public Map<String, Reservation> getReservationsFromFlight(String s){
        Map<String, Reservation> ans = new HashMap<>();
        if(!this.reservations.isEmpty())
            for(String code : this.reservations.keySet()){
                Reservation r = this.reservations.get(code);
                if(r.isToFlight(s)) ans.put(code, r.clone());
            }
        return ans;
    }

    public Map<String, Reservation> getReservationsFromUser(String s){
        Map<String, Reservation> ans = new HashMap<>();
        if(!this.reservations.isEmpty())
            for(String code : this.reservations.keySet()){
                Reservation r = this.reservations.get(code);
                if(r.isFromUser(s)) ans.put(code, r.clone());
            }
        return ans;
    }

    public void removeFlight(String s) throws FlightDoesntExistException{
        if(this.flights.containsKey(s)){
            Map<String, Reservation> reservationsOfFlight = getReservationsFromFlight(s);
            if(!reservationsOfFlight.isEmpty())
                for(String code : reservationsOfFlight.keySet())
                    try {
                        removeReservation(code);
                    } catch (Exception ignored){}
            this.flights.remove(s);
        }
        else throw new FlightDoesntExistException("There isn't any flight with ID " + s);
    }

    public void removeReservationByClient(String reservationID, String currentUser) throws DoesntExistReservationFromClient, FlightAlreadyDeparted{
        if(this.reservations.containsKey(reservationID)) {
            String username = this.reservations.get(reservationID).getClientID();
            Set<String> fs = this.reservations.get(reservationID).getFlightsID();
            for(String id : fs){
                if(!this.flights.get(id).getToGo()) throw new FlightAlreadyDeparted("The flight with ID " + id + " already departed.");
            }
            if (this.clients.containsKey(username) && currentUser.equals(username)) {
                this.clientReservations.remove(reservationID);
            }
        }
        else throw new DoesntExistReservationFromClient("Doesn't exist any reservation with ID " + reservationID + "from you.");
    }

    public void removeReservation(String reservationID) throws ReservationDoesntExistException {
        if(this.reservations.containsKey(reservationID)) {
            String username = this.reservations.get(reservationID).getClientID();
            if (this.clients.containsKey(username)) {
                this.clientReservations.remove(reservationID);
            }
        }
        else throw new ReservationDoesntExistException("There isn't any reservation with ID " + reservationID);
    }

    public String createFlight(int nMaxPassengers, int nReserve, City origin, City destination, boolean toGo, LocalDate date) throws UnavailableFlightException{
        if(closedDays.contains(date)) throw new UnavailableFlightException("The flight is in a closed day.");
        String fID = this.generateFlightID();
        Flight f = new Flight(fID, nMaxPassengers, nReserve, origin, destination, toGo, date);
        this.flights.put(fID, f);
        return fID;
    }

    public void checkSetOfFlightsToReservation(Set<String> flightsID) throws UnavailableFlightException, FlightAlreadyDeparted, FlightDoesntExistException{
        for(String fID : flightsID){
            Flight f = this.flights.getOrDefault(fID, null);
            if(f == null) throw new FlightDoesntExistException();
            else if(!f.hasFreeSpace()) throw new UnavailableFlightException();
            else if(!f.getToGo()) throw new FlightAlreadyDeparted();
        }
    }

    public String createReservation(String clientID, Set<String> flightsID) throws FlightDoesntExistException, UnavailableFlightException, FlightAlreadyDeparted {
        checkSetOfFlightsToReservation(flightsID);
        String reservationID = this.generateReservationID();
        Reservation r = new Reservation(reservationID, clientID, flightsID);

        Set<String> reservationsByClient;

        if (this.clientReservations == null) this.clientReservations = new HashMap<>();

        if (this.clientReservations.containsKey(clientID))
            reservationsByClient = this.clientReservations.get(clientID);
        else
            reservationsByClient = new HashSet<>();

        reservationsByClient.add(reservationID);
        this.clientReservations.put(clientID, reservationsByClient);

        for(String fId : flightsID){
            Flight f = this.flights.get(fId);
            f.addOneReservation();
        }
        this.reservations.put(r.getID(), r);
        return reservationID;
    }

    public void addClosedDay(LocalDate date) throws AlreadyIsAClosedDay {
        if(!this.closedDays.contains(date)){
            this.closedDays.add(date);
            for(String key : flights.keySet()){
                Flight f = flights.get(key);
                if(f.getDate().equals(date)){
                    try{
                        removeFlight(key);
                    } catch (Exception ignored){}
                }
            }
        }
        else throw new AlreadyIsAClosedDay("The selected day is already closed.");
    }

    public void removeClosedDay(LocalDate date){
        this.closedDays.remove(date);
    }

    public List<Flight> getFlightsWithOriginAndDestination(City origin, City destination){
        List<Flight> flights = new ArrayList<>();
        for(String key : this.flights.keySet()){
            Flight f = this.flights.get(key);
            if(f.getOrigin().equals(origin) && f.getDestination().equals(destination)) flights.add(f.clone());
        }
        return flights;
    }

    public List<Flight> getFlightsWithOriginDestinationAndDate(City origin, City destination, LocalDate date){
        List<Flight> flights = new ArrayList<>();
        for(String key : this.flights.keySet()){
            Flight f = this.flights.get(key);
            if(f.getOrigin().equals(origin) && f.getDestination().equals(destination) && f.getDate().equals(date)) flights.add(f.clone());
        }
        return flights;
    }

    public List<Flight> getFlightsWithOriginDestinationAndDateRange(City origin, City destination, LocalDate begin, LocalDate end){
        List<Flight> flights = new ArrayList<>();
        for(String key : this.flights.keySet()){
            Flight f = this.flights.get(key);
            if(f.getOrigin().equals(origin) && f.getDestination().equals(destination) && Utilities.isInRange(begin, end, f.getDate())) flights.add(f.clone());
        }
        return flights;
    }

    // Don't know if this works...
    public String generateFlightID(){
        String lastID = "";
        for(String s : this.flights.keySet())
            lastID = s; // recolhe o ID do último voo ig (não sei como funciona a organização interna dos maps, se "ordenar" acho q este método funciona)
        if(lastID.equals("")) return "F" + 1; // se o map tiver vazio, o ID do Flight poderá ser F1
        String[] formated = lastID.split("F");
        return "F" + (Integer.parseInt(formated[1]) + 1); // id do Flight será F seguido pelo número seguinte ao último inserido
    }

    // funcionamento semelhante ao generateFlightID
    public String generateReservationID(){
        String lastID = "";
        for(String s : this.reservations.keySet())
            lastID = s;
        if(lastID.equals("")) return "R" + 1;
        String[] formated = lastID.split("R");
        return "R" + (Integer.parseInt(formated[1]) + 1);
    }

    public List<Flight> getFlightsAvailableForReservationFromList(List<Flight> fs){
        List<Flight> ans = new ArrayList<>();
        for(Flight f : fs){
            if(this.flights.get(f.getID()).hasFreeSpace() && this.flights.get(f.getID()).getToGo()) ans.add(f);
        }
        return ans;
    }

    //Question 5 otimizada (não sei se funfa tho)
    public List<List<Flight>> getPossibleTrip(List<Flight> fs, List<List<Flight>> compared, LocalDate end){
        List<List<Flight>> ans = new ArrayList<>();
        for(Flight f : fs){
            for(List<Flight> possibleT : compared) {
                int size = possibleT.size();
                if (Utilities.isInRange(possibleT.get(size - 1).getDate(), end, f.getDate())){ // se estiver numa data possível, cria uma lista com os voos
                    List<Flight> toAdd = new ArrayList<>(possibleT);
                    toAdd.add(f); // acho q não precisa de ter clone pq eles já vêm clonados do getFlightsWithOriginDestinationAndDateRange
                    ans.add(toAdd);
                }
            }
        }
        System.out.println("Results " + ans.size());
        return ans;
    }

    public List<Route> getAvailableRoutesInDataRange(List<City> desiredCities, LocalDate begin, LocalDate end){
        List<List<Flight>> compared = new ArrayList<>();
        List<Route> ans = new ArrayList<>();
        for(int i = 0; i < desiredCities.size() - 2; i++){
            City o = desiredCities.get(i);
            City d = desiredCities.get(i + 1);
            List<Flight> fs = getFlightsWithOriginDestinationAndDateRange(o, d, begin, end);
            if(i != 0)
                compared = getPossibleTrip(fs, compared, end);
            else
                compared.add(fs);
        }
        if(compared.size() == 0) return ans; // não existe nenhuma opção viável
        for(List<Flight> fs : compared){
            Route r = new Route(fs); // passa de lista de listas para lista de rotas
            ans.add(r);
        }
        return ans;
    }

    public String createReservationGivenListRoutes(String username, List<Route> routes){
        String reservationID = "";
        for(Route r : routes){
            Set<String> fIDs = r.getFlightsIDs();

            for (String id : fIDs) System.out.println(id);
            try{
                reservationID = createReservation(username, fIDs);
                break;
            } catch (Exception ignored){}
        }
        return reservationID;
    }

    public String createReservationGivenCities(String username, List<City> desiredCities, LocalDate begin, LocalDate end){
        return createReservationGivenListRoutes(username, getAvailableRoutesInDataRange(desiredCities, begin, end));
    }

    /*
    // QUESTION 5 feita à padeiro -> pra já, dá para até 2 escalas, mas isto tá muito ineficiente (estupidamente), penso que tbm devia-se evitar os cases, mas não tou a ver como fazer
    public List<Route> getAvailableRoutesInDataRange(List<City> desiredCities, LocalDate begin, LocalDate end){
        List<Route> routes = new ArrayList<>();
        switch(desiredCities.size() - 2){
            case 0:{
                List<Flight> fs = getFlightsWithOriginDestinationAndDateRange(desiredCities.get(0), desiredCities.get(1), begin, end);
                List<Flight> availables = getFlightsAvailableForReservationFromList(fs);
                routes.add(new Route(desiredCities.get(0), desiredCities.get(1), availables));
            }
            case 1:{
                List<List<Flight>> fs = getFlightsWithOneStopOnSameDateWithDateRange(desiredCities.get(0), desiredCities.get(2), begin, end);
                for(List<Flight> list : fs){
                    List<Flight> availables = getFlightsAvailableForReservationFromList(list);
                    Route r = new Route(desiredCities.get(0), desiredCities.get(1), availables);
                    routes.add(r);
                }
            }
            case 2:{
                List<List<Flight>> fs = getFlightsWithTwoStopsOnSameDateWithDateRange(desiredCities.get(0), desiredCities.get(2), begin, end);
                for(List<Flight> list : fs){
                    List<Flight> availables = getFlightsAvailableForReservationFromList(list);
                    Route r = new Route(desiredCities.get(0), desiredCities.get(1), availables);
                    routes.add(r);
                }
            }

            // Vale a pena fazer para mais paragens? PLS ALGUÉM SAIBA UMA MANEIRA DE FAZER ISTO DE FORMA + EFICIENTE!!!!!
        }
        return routes;
    }

    public List<List<Flight>> getFlightsWithOneStopOnSameDateWithDateRange(City origin, City destination, LocalDate begin, LocalDate end){
        List<List<Flight>> ans = new ArrayList<>();
        List<Flight> fromOrigin = getFlightsFromCityWithDateRange(origin, begin, end);
        if(!fromOrigin.isEmpty()){
            List<Flight> toDestination = getFlightsToCityWithDateRange(destination, begin, end);
            if(!toDestination.isEmpty())
                for(Flight f : toDestination){
                    City o = f.getOrigin();
                    LocalDate d = f.getDate();
                    for(Flight fl : fromOrigin)
                        if(fl.getDestination().equals(o) && fl.getDate().equals(d)){
                            List<Flight> toAdd = new ArrayList<>();
                            toAdd.add(f.clone());
                            toAdd.add(fl.clone());
                            ans.add(toAdd);
                        }
                }
        }
        return ans;
    }

    public List<List<Flight>> getFlightsWithTwoStopsOnSameDateWithDateRange(City origin, City destination, LocalDate begin, LocalDate end){
        List<List<Flight>> ans = new ArrayList<>();
        List<Flight> fromOrigin = getFlightsFromCityWithDateRange(origin, begin, end);
        if(!fromOrigin.isEmpty()){
            List<Flight> toDestination = getFlightsToCityWithDateRange(destination, begin, end);
            if(!toDestination.isEmpty()) {
                Set<Flight> withoutCities = getFlightsWithoutCities(origin, destination);
                for (Flight f : withoutCities) {
                    City o = f.getOrigin();
                    City d = f.getDestination();
                    LocalDate dF = f.getDate();
                    for (Flight flO : fromOrigin)
                        if (flO.getDestination().equals(o) && flO.getDate().equals(dF)) {
                            for(Flight flD : toDestination)
                                if(flD.getOrigin().equals(d) && flD.getDate().equals(dF)){
                                    List<Flight> toAdd = new ArrayList<>();
                                    toAdd.add(flO.clone());
                                    toAdd.add(f.clone());
                                    toAdd.add(flD.clone());
                                    ans.add(toAdd);
                                }
                        }
                }
            }
        }
        return ans;
    }

    public List<Flight> getFlightsFromCity(City c){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getOrigin().equals(c)) ans.add(f.clone());
        }
        return ans;
    }

    public List<Flight> getFlightsToCity(City c){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getDestination().equals(c)) ans.add(f.clone());
        }
        return ans;
    }

    public List<Flight> getFlightsToCityWithDateRange(City c, LocalDate begin, LocalDate end){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getDestination().equals(c) && Utilities.isInRange(begin, end, f.getDate())) ans.add(f.clone());
        }
        return ans;
    }

    public List<Flight> getFlightsFromCityWithDateRange(City c, LocalDate begin, LocalDate end){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getOrigin().equals(c) && Utilities.isInRange(begin, end, f.getDate())) ans.add(f.clone());
        }
        return ans;
    }


    public Set<Flight> getFlightsWithoutCities(City c1, City c2){
        Set<Flight> ans = new HashSet<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getDestination() != c1 && f.getDestination() != c2 && f.getOrigin() != c1 && f.getOrigin() != c2) ans.add(f.clone());
        }
        return ans;
    }

    public List<List<Flight>> getFlightsWithOneStop(City origin, City destination){
        List<List<Flight>> ans = new ArrayList<>();
        List<Flight> fromOrigin = getFlightsFromCity(origin);
        if(!fromOrigin.isEmpty()){
            List<Flight> toDestination = getFlightsToCity(destination);
            if(!toDestination.isEmpty())
                for(Flight f : toDestination){
                    City o = f.getOrigin();
                    for(Flight fl : fromOrigin)
                        if(fl.getDestination().equals(o)){
                            List<Flight> toAdd = new ArrayList<>();
                            toAdd.add(f.clone());
                            toAdd.add(fl.clone());
                            ans.add(toAdd);
                        }
                }
        }
        return ans;
    }

    public List<List<Flight>> getFlightsWithTwoStops(City origin, City destination){
        List<List<Flight>> ans = new ArrayList<>();
        List<Flight> fromOrigin = getFlightsFromCity(origin);
        if(!fromOrigin.isEmpty()){
            List<Flight> toDestination = getFlightsToCity(destination);
            if(!toDestination.isEmpty()) {
                Set<Flight> withoutCities = getFlightsWithoutCities(origin, destination);
                for (Flight f : withoutCities) {
                    City o = f.getOrigin();
                    City d = f.getDestination();
                    for (Flight flO : fromOrigin)
                        if (flO.getDestination().equals(o)) {
                            for(Flight flD : toDestination)
                                if(flD.getOrigin().equals(d)){
                                    List<Flight> toAdd = new ArrayList<>();
                                    toAdd.add(flO.clone());
                                    toAdd.add(f.clone());
                                    toAdd.add(flD.clone());
                                    ans.add(toAdd);
                                }
                        }
                }
            }
        }
        return ans;
    }

    // Provavelmente dá pra otimizar isto, aliás, não faz sentido estar sempre a adicionar a cidade de origem e destino, deve dar pra fazer algo com Map.Entry, mas pra já, é o que é
    public List<Route> getRoutes(City origin, City destination){
        List<Route> ans = new ArrayList<>();

        List<Flight> flights = getFlightsWithOriginAndDestination(origin, destination);
        if(flights != null && !flights.isEmpty())
            for(Flight f : flights){
                List<Flight> toAdd = new ArrayList<>();
                toAdd.add(f.clone());
                ans.add(new Route(origin, destination, toAdd));
            }

        List<List<Flight>> flights1stop = getFlightsWithOneStop(origin, destination);
        if(flights1stop != null && !flights1stop.isEmpty())
            for(List<Flight> fls : flights1stop)
                ans.add(new Route(origin, destination, fls));

        List<List<Flight>> flights2stop = getFlightsWithTwoStops(origin, destination);
        if(flights2stop != null && !flights2stop.isEmpty())
            for(List<Flight> fls : flights2stop)
                ans.add(new Route(origin, destination, fls));

        return ans;
    }
    */
    public void serialize(String filepath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filepath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.flush();
        oos.close();
        fos.flush();
        fos.close();
    }

    public static Model deserialize(String filepath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Model accounts = (Model) ois.readObject();
        ois.close();
        fis.close();
        return accounts;
    }
}
