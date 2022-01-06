package Utils;

/**
 * Class of utilities
 */
public class Utilities {

    /**
     * Method to check the number of '@' on a string
     * @param email String to check
     * @return Number of '@'
     */
    public static int checkEmail(String email){
        char[] chars = email.toCharArray();
        int ac = 0;
        for(char c : chars)
            if(c == '@') ac++;
        return ac;
    }

}
