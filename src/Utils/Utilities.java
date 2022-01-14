package Utils;
import java.util.regex.*;
/**
 * Interface of utilities
 */
public interface Utilities {

    /**
     * Method to check the number of '@' on a string
     * @param email String to check
     * @return True if the email is valid, false otherwise
     */
    static boolean checkEmail(String email){
        Pattern p = Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");//. represents single character
        Matcher m = p.matcher(email);
        boolean b = m.matches();
        /*char[] chars = email.toCharArray();
        int ac = 0;
        for(char c : chars)
            if(c == '@') ac++;
        return (ac == 1);*/
        return b;
    }

}
