package Exceptions;

public class AlreadyIsAClosedDay extends Exception{
    public AlreadyIsAClosedDay(){
        super();
    }

    public AlreadyIsAClosedDay(String s){
        super(s);
    }
}
