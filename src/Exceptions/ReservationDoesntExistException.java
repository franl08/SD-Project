package Exceptions;

public class ReservationDoesntExistException extends Exception{
    public ReservationDoesntExistException(){
        super();
    }

    public ReservationDoesntExistException(String s){
        super(s);
    }
}
