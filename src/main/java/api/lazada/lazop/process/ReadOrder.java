package api.lazada.lazop.process;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ReadOrder {
    InputStream inputStream;   //lớp cha của tất cả các lớp đại diện cho một luồng byte đầu vào.


    public String getPropValues() throws IOException {
        String res_config = new String();

        try {
            FileInputStream propsFile = new FileInputStream(new File("paided_code"));
            Properties properties = new Properties();
            properties.load(new InputStreamReader(propsFile, Charset.forName("UTF-8")));
            propsFile.close();

//Đọc giá trị order_id
           res_config = properties.getProperty("order_id");

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        //return output
        return res_config;
    }
}
