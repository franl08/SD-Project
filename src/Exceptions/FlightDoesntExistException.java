package Exceptions;

public class FlightDoesntExistException extends Exception{
    public FlightDoesntExistException(){
        super();
    }

    public FlightDoesntExistException(String s){
        super(s);
    }
}
