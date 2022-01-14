package Utils;
import java.util.regex.*;
/**
 * Interface of utilities
 */
public interface Utilities {

    /**
     * Method to validate an email using regex
     * @param email String to check
     * @return True if the email is valid, false otherwise
     */
    static boolean checkEmail(String email){
        Pattern p = Pattern.compile("^\\w+([\\.-]?\\w+)*@\\w+(\\.\\w{2,3})+$");//. represents single character
        Matcher m = p.matcher(email);
        return m.matches();
    }

}
