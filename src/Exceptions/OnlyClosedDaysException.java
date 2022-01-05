package Exceptions;

public class OnlyClosedDaysException extends Exception{
    public OnlyClosedDaysException(){
        super();
    }
    public OnlyClosedDaysException(String s){
        super(s);
    }
}
