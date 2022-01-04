package Exceptions;

/**
 * Exception that happens when the flight is unavailable
 */
public class UnavailableFlightException extends Exception{

    /**
     * Empty exception constructor
     */
    public UnavailableFlightException(){
        super();
    }

    /**
     * Exception constructor
     * @param m Explanation of the exception
     */
    public UnavailableFlightException(String m){
        super(m);
    }
}
