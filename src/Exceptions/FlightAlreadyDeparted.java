package Exceptions;

/**
 * Exception that happens during a removal of a reservation, but the flight already left
 */
public class FlightAlreadyDeparted extends Exception{
    /**
     * Empty exception constructor
     */
    public FlightAlreadyDeparted(){
        super();
    }

    /**
     * Exception constructor
     * @param s String that explain the exception
     */
    public FlightAlreadyDeparted(String s){
        super();
    }
}
