package Client;


import Model.Reserve;

import java.util.Map;

public class Client extends User{
    private Map<String, Reserve> reserves;

    public Map<String, Reserve> getReserves(){
        return reserves;
    }

    public void addReserve(Reserve r){
        reserves.put(r.getID(), r);
    }
}
