package api.lazada.lazop.process;

import api.lazada.lazop.api.LazopClient;
import api.lazada.lazop.api.LazopRequest;
import api.lazada.lazop.api.LazopResponse;
import api.lazada.lazop.util.ApiException;
import api.lazada.lazop.util.Logger;
import api.lazada.lazop.util.Preference;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static api.lazada.lazop.util.UrlConstants.API_GATEWAY_URL_VN;


public class GetOrders extends Thread {


    public GetOrders() throws IOException {
    }
    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
    public void select() throws IOException, ApiException, JSONException {
// Đọc file paided_code
        ReadOrder read = new ReadOrder();
        String list_order =read.getPropValues();         ///////////////////////////////
        String[] arr_order = list_order.split(",");

        String url = API_GATEWAY_URL_VN;
//Lấy list order tên lazop
        String appkey= Preference.appkey;
        String appSecret= Preference.appSecret;
        LazopClient client = new LazopClient(url, appkey, appSecret);
        LazopRequest request = new LazopRequest();
        request.setApiName("/orders/get");
        request.setHttpMethod("GET");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String cur_date = dateFormat.format(yesterday());

        request.addApiParameter("created_after", cur_date+"T23:59:30+07:00");
        request.addApiParameter("status", "pending");
        String accessToken= Preference.accessToken;
        LazopResponse response = client.execute(request, accessToken);

        JSONObject json = new JSONObject(response.getBody());

        Logger.error("lazop","{process.GetOrders} : "+"request(data:"+response.getBody()+"), response: "+response);
//Nếu code=0, lấy danh sách order từ data , phân biệt Jsonarray với Jsonobject
        if(json.getInt("code")==0){
            JSONArray orders = json.getJSONObject("data").getJSONArray("orders");

            System.out.println(orders.toString());

            for (int i = 0; i < orders.length(); i++) {
                JSONObject rec = orders.getJSONObject(i);   //lấy thông tin từng đơn hàng
                String order_number = rec.getString("order_number");        //lấy order_number của đơn hàng

                if( !Arrays.asList(arr_order).contains(order_number)){     //nếu order_number ko có trong chuỗi arr_order
                    System.out.println(order_number);

                    // Lấy thông tin chi tiết đơn hàng bằng /order/items/get
                    LazopClient client2 = new LazopClient(url, appkey, appSecret);
                    LazopRequest request2 = new LazopRequest();
                    request2.setApiName("/order/items/get");
                    request2.setHttpMethod("GET");
                    request2.addApiParameter("order_id", order_number);
                    LazopResponse response2 = client.execute(request2, accessToken);
                    JSONObject detail = new JSONObject(response2.getBody());

                    JSONArray arr = detail.getJSONArray("data");    // lấy được data của đơn hàng
                    if(arr.length()>=1){
                        JSONObject obj = arr.getJSONObject(0);
                        String sku = obj.getString("sku");     // lấy được sku sp order
                        String[] arr_sku = sku.split("\\+");     //tách chuỗi bằng dấu +
                        String product_code = arr_sku[0];
                        String id_product = arr_sku[1];
                        String price = arr_sku[2];
                        int price_1 = Integer.parseInt(price);
                        String email = obj.getString("digital_delivery_info");
                        int count = arr.length();
                        int total_price = price_1 * count;
                        int total_charge = obj.getInt("paid_price") * count;
// Lấy được các trường mong muốn
                        processCheckBy(order_number,product_code,id_product,email,count,total_price,total_charge);

                    }

                }

             /*   LazopClient client3 = new LazopClient(url, appkey, appSecret);
                LazopRequest request3 = new LazopRequest();
                request3.setApiName("/order/sof/delivered");
                request3.addApiParameter("order_item_ids",order_number);
                LazopResponse response3 = client3.execute(request, accessToken);*/  //tự động chuyển trạng thái delivered
            }

        }


    }

    private void processCheckBy(String order_number,String product_code, String id_product,  String email, int count, int total_price,int total_charge) throws IOException {
// tạo request_id theo time_now
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String current = formatter.format(date);
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
        Random generator = new Random();
        String request_id = ft.format(dNow)+generator.nextInt(9999) + 1;


        String passLaz= Preference.passLaz;
        String pass = passLaz +request_id;
        String password = getMD5(pass);              //password

// Gọi vào API buy BHNB của anh Quân
        String url_bhnb= Preference.url_bhnb;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/xml");
        RequestBody body = RequestBody.create(mediaType, "<mps>\n    <username>lazada</username>\n    <pass>"+password+"</pass>\n    <requestid>"+request_id+"</requestid>\n    <product_code>"+product_code+"</product_code>\n    <product_id>"+id_product+"</product_id>\n    <count>"+count+"</count>\n    <email>"+email+"</email>\n    <send_code_email>1</send_code_email>\n    <isdn>0903967333</isdn>\n    <cus_name>Lazada Test</cus_name>\n    <request_date>"+current+"</request_date>\n    <address>Lazada Test</address>\n    <total_price>"+total_price+"</total_price>\n    <total_charge>"+total_charge+"</total_charge>\n</mps>");
        Request request = new Request.Builder()
                .url(url_bhnb)
                .method("POST", body)
                .addHeader("Content-Type", "application/xml")
                .build();
        Response response = client.newCall(request).execute();

        String res = response.body().string();
        String status = res.substring(res.indexOf("<result>") + 8, res.indexOf("</result>"));
        System.out.println(res);
        Logger.info("lazop","{process.GetOrders} : "+"request(status: "+status+
                ",\n data:"+body.toString()+"), response: "+res.toString());
        if(Integer.parseInt(status) ==1){
            WriteOrder.writeOrder(order_number);
            System.out.println(Integer.parseInt(status));
            //chuyển pending....
        }


    }
//  phương thức mã hóa MD5
    private String getMD5(String pass) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(pass.getBytes());
            return convertByteToHex1(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertByteToHex1(byte[] data) {
        BigInteger number = new BigInteger(1, data);
        String hashtext = number.toString(16);
        // Now we need to zero pad it if you actually want the full 32 chars.
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }


    public void run() {

        try {
            while(true){
                select();
                sleep(10000);

            }
        } catch (Exception ex3) {
            ex3.printStackTrace();

        }
    }
}
