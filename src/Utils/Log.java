package Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Log {

    public static void appendMessage(String message){
        File f = new File(".logs.txt");
        try {
            if(!f.exists())
                f.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
            out.write(message + "\n");
            out.flush();
            out.close();
        } catch (Exception ignored){}
    }

    public static void appendSeparator() {
        File f = new File(".logs.txt");
        try {
            if(!f.exists())
                f.createNewFile();
            else{
                BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
                out.write("\n-----------------------------------HF-----------------------------------\n");
            }
        } catch (Exception ignored){}
    }
}
