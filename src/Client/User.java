package Client;

public class User {
    private String username;
    private String email;
    private String[] fullName;
    private String ID;
    private String password;

    public String getPassword(){
        return this.password;
    }

    public String getUsername(){
        return this.username;
    }

    public String getEmail(){
        return this.email;
    }

    public String[] getFullName(){
        String[] ans = new String[this.fullName.length];
        int ac = 0;
        for(String name : this.fullName) ans[ac++] = name;
        return ans;
    }

    public String getID(){
        return this.ID;
    }
}
