package Exceptions;

/**
 * Exception that happens when a flight does not exist
 */
public class FlightDoesntExistException extends Exception{
    /**
     * Empty exception constructor
     */
    public FlightDoesntExistException(){
        super();
    }

    /**
     * Exception constructor
     * @param s String that explain the exception
     */
    public FlightDoesntExistException(String s){
        super(s);
    }
}
