package Exceptions;

public class DoesntExistReservationFromClient extends Exception{

    public DoesntExistReservationFromClient(){
        super();
    }

    public DoesntExistReservationFromClient(String s){
        super(s);
    }
}
