package api.lazada.lazop.Thread;

import api.lazada.lazop.util.Logger;
import api.lazada.lazop.util.Preference;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;


    public class LoadConfig extends Thread{
        public static void getPropValues() throws IOException {

            try {
                FileInputStream propsFile = new FileInputStream(new File("config.properties"));
                Properties properties = new Properties();
                properties.load(new InputStreamReader(propsFile, Charset.forName("UTF-8")));
                propsFile.close();

             /*   String key_sms = properties.getProperty("appkey", "");
                synchronized (Preference.key_sms) {
                    Preference.key_sms = key_sms.split(",");

                }

                String ip_access = properties.getProperty("ip_access", "");
                synchronized (Preference.ip_access) {
                    Preference.ip_access = ip_access.split(",");
                }*/

                String appkey = properties.getProperty("appkey", "");
                synchronized (Preference.appkey) {
                    Preference.appkey = appkey;
                }

                String url_bhnb = properties.getProperty("url_bhnb", "");
                synchronized (Preference.url_bhnb) {
                    Preference.url_bhnb = url_bhnb;
                }

                String url_sim = properties.getProperty("url_sim", "");
                synchronized (Preference.url_sim) {
                    Preference.url_sim = url_sim;
                }
                String shop_code = properties.getProperty("shop_code", "");
                synchronized (Preference.shop_code) {
                    Preference.shop_code = shop_code;
                }
                String user = properties.getProperty("user", "");
                synchronized (Preference.user) {
                    Preference.user = user;
                }
                String pass = properties.getProperty("pass", "");
                synchronized (Preference.pass) {
                    Preference.pass = pass;
                }
                String api_key = properties.getProperty("api_key", "");
                synchronized (Preference.api_key) {
                    Preference.api_key = api_key;
                }


                String appSecret = properties.getProperty("appSecret", "");
                synchronized (Preference.appSecret) {
                    Preference.appSecret = appSecret;
                }

                String accessToken = properties.getProperty("accessToken", "");
                synchronized (Preference.accessToken) {
                    Preference.accessToken = accessToken;
                }

                String passLaz = properties.getProperty("passLaz", "");
                synchronized (Preference.passLaz) {
                    Preference.passLaz = passLaz;
                }

                String refreshToken = properties.getProperty("refreshToken", "");
                synchronized (Preference.refreshToken) {
                    Preference.refreshToken = accessToken;
                }



                Logger.info("lazop","{Thread.LoadConfig} : Load file config success");
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.error("lazop","{Thread.LoadConfig} : Error:("+ex+")");
            }


        }
        public void run() {

            try {
                while(true){
                    getPropValues();
                    sleep(300000);
                }
            } catch (Exception ex3) {
                ex3.printStackTrace();
                Logger.info("lazop","{Thread.LoadConfig} : Loi doc file config ("+ex3+")");

            }
        }

    }
