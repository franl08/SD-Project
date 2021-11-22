package Exceptions;

public class UnavailableFlightException extends Exception{
    public UnavailableFlightException(){
        super();
    }

    public UnavailableFlightException(String m){
        super(m);
    }
}
