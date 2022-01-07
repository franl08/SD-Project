package Utils;

/**
 * Class of utilities
 */
public class Utilities {

    /**
     * Method to check the number of '@' on a string
     * @param email String to check
     * @return True if the email is valid, false otherwise
     */
    public static boolean checkEmail(String email){
        char[] chars = email.toCharArray();
        int ac = 0;
        for(char c : chars)
            if(c == '@') ac++;
        return (ac == 1);
    }

}
