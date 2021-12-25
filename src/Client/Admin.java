package Client;

public class Admin extends User{

    public Admin(String username, String email, String fullName, String password) {
        super(username, email, fullName, password);
    }

    public Admin(Admin a){
        super(a.getUsername(), a.getEmail(),a.getFullName(), a.getPassword());
    }

    public Admin clone(){
        return new Admin(this);
    }

    public void run() {

    }
}
