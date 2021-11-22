package api.lazada.lazop.process;

import api.lazada.lazop.api.LazopClient;
import api.lazada.lazop.api.LazopRequest;
import api.lazada.lazop.api.LazopResponse;
import api.lazada.lazop.util.ApiException;
import api.lazada.lazop.util.Logger;
import api.lazada.lazop.util.Preference;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;

import static api.lazada.lazop.util.UrlConstants.API_GATEWAY_URL_VN;
import static api.lazada.lazop.util.UrlConstants.API_GATEWAY_URL_MY;


public class GetToken extends Thread{
    public GetToken() throws IOException {
    }

    private static String[] getToken() throws IOException, ApiException, JSONException {
        String url = API_GATEWAY_URL_MY;
//Lấy list order tên lazop
        String appkey= Preference.appkey;
        String appSecret= Preference.appSecret;
        LazopClient client = new LazopClient(url, appkey, appSecret);
        LazopRequest request = new LazopRequest();
        request.setApiName("/auth/token/create");
        request.addApiParameter("code", "0_101684_Gzwgr4Sz3T6uZ9qnTZOokzMY706");
        request.addApiParameter("uuid", "");
        LazopResponse response = client.execute(request);
        JSONObject json = new JSONObject(response.getBody());

        String acess_token = json.getString("access_token");
        String refresh_token = json.getString("refresh_token");
        return new String[] {acess_token,refresh_token};

    }
    public static String freshToken() throws IOException, ApiException, JSONException {
       String[] token= getToken();
       String refresh_token= token[1];
        String url = API_GATEWAY_URL_VN;
//Lấy list order tên lazop
        String appkey= Preference.appkey;
        String appSecret= Preference.appSecret;
        LazopClient client = new LazopClient(url, appkey, appSecret);
        LazopRequest request = new LazopRequest();
        request.setApiName("/auth/token/refresh");
        request.addApiParameter("refresh_token", refresh_token);
        LazopResponse response = client.execute(request);
        JSONObject json = new JSONObject(response.getBody());
        String acess_token = json.getString("access_token");
        System.out.println(acess_token);
        return refresh_token;
    }

    public void run() {

        try {
            while(true){
                freshToken();
                sleep(561600000);
            }
        } catch (Exception ex3) {
            ex3.printStackTrace();
            Logger.info("lazop","{Thread.LoadConfig} : Loi doc file config ("+ex3+")");

        }
    }

}
