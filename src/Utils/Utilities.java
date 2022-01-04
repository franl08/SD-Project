package Utils;

import java.time.LocalDate;

/**
 * Useful methods
 */
public class Utilities {

    /**
     * Checks if a date is between a date range
     * @param begin Beginning of the range
     * @param after End of the range
     * @param date Date to check
     * @return True if affirmative, false otherwise
     */
    public static boolean isInRange(LocalDate begin, LocalDate after, LocalDate date){
        return (date.isBefore(after) || date.isEqual(after)) && (date.isAfter(begin)) || (date.isEqual(begin));
    }
}
