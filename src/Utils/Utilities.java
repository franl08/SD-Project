package Utils;

import java.time.LocalDate;

public class Utilities {
    public static boolean isInRange(LocalDate begin, LocalDate after, LocalDate date){
        return (date.isBefore(after) && date.isAfter(begin)) || date.isEqual(begin) || date.isEqual(after);
    }
}
