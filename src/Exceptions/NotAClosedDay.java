package Exceptions;

public class NotAClosedDay extends Exception{
    public NotAClosedDay(){
        super();
    }

    public NotAClosedDay(String s){
        super(s);
    }
}
