package api.lazada.lazop.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {


    public static void error(String api,String data){
        writeLog(api,"ERRROR",data);
    }
    public static void info(String api,String data){
        writeLog(api,"INFO",data);
    }
    public static void warn(String api,String data){
        writeLog(api,"WARN",data);
    }


    public static void writeLog(String api,String level,String data){
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {

            String path = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            SimpleDateFormat sp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
            String nowString = sp.format(new Date());
            File file = new File("logs/"+api+"_"+path+".log");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            String message = nowString + " : " + level + ": " + data+"\n";
            bw.write(message);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


}

