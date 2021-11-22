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
import java.util.*;

import static api.lazada.lazop.util.UrlConstants.API_GATEWAY_URL_VN;
import static org.apache.coyote.http11.Constants.a;


public class GetOrders extends Thread {


    public GetOrders() throws IOException {
    }
    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -100);
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
        /*String accessToken= GetToken.freshToken();*/
        String accessToken= Preference.accessToken;
        LazopResponse response = client.execute(request, accessToken);

        JSONObject json = new JSONObject(response.getBody());

        Logger.error("lazop","{process.GetOrders} : "+"request(data:"+response.getBody()+"), response: "+response);
//Nếu code=0, lấy danh sách order từ data , phân biệt Jsonarray với Jsonobject
        if(json.getInt("code")==0){
            JSONArray orders = json.getJSONObject("data").getJSONArray("orders");       //(array)

            System.out.println(orders.toString());
/////////////////////////////
            for (int i = 0; i < orders.length(); i++) {
                String name = orders.getJSONObject(i).getJSONObject("address_billing").getString("first_name");
                String phone = orders.getJSONObject(i).getJSONObject("address_billing").getString("phone");
                String address1 = orders.getJSONObject(i).getJSONObject("address_billing").getString("address1");
                String city = orders.getJSONObject(i).getJSONObject("address_billing").getString("city");
                String country = orders.getJSONObject(i).getJSONObject("address_billing").getString("country");
                String address= address1+","+city+","+country;

                JSONObject rec = orders.getJSONObject(i);   //lấy thông tin từng đơn hàng  (object{})
                String order_number = rec.getString("order_number");        //lấy order_number của đơn hàng

                if( !Arrays.asList(arr_order).contains(order_number)){     //nếu order_number ko có trong chuỗi arr_order
                    /*System.out.println(order_number);*/

                    // Lấy thông tin chi tiết đơn hàng bằng /order/items/get
                    LazopClient client2 = new LazopClient(url, appkey, appSecret);
                    LazopRequest request2 = new LazopRequest();
                    request2.setApiName("/order/items/get");
                    request2.setHttpMethod("GET");
                    request2.addApiParameter("order_id", order_number);
                    LazopResponse response2 = client2.execute(request2, accessToken);
                    JSONObject detail = new JSONObject(response2.getBody());

                    JSONArray arr = detail.getJSONArray("data");    // lấy được data của đơn hàng (array)
                    JSONObject obj = arr.getJSONObject(0);  // ---lấy được data của đơn hàng (object)
                    String sku = obj.getString("sku");     // lấy được sku sp order (string)
                    /*System.out.println(obj);*/
                    if(arr.length()>=1){

                        if(Objects.equals(sku,"c050_1M") || Objects.equals(sku, "c050_3M") || Objects.equals(sku, "c050_6M") || Objects.equals(sku, "c025_1M") || Objects.equals(sku, "c025_3M") || Objects.equals(sku, "c025_6M")){

                        String email = obj.getString("digital_delivery_info");



                        String order_item_id = obj.getString("order_item_id");
                        int fee_ship = obj.getInt("shipping_service_cost");

                        int count = arr.length();
                        int total_price = obj.getInt("paid_price") * count;
                             // Lấy được các trường mong muốn
                        processBuySim(order_number,sku,name,phone,address,email,count,total_price,fee_ship,order_item_id);

                        }
                    else {
                        String[] arr_sku = sku.split("\\+");     // tách chuỗi bằng dấu +
                        String product_code = arr_sku[0];
                        String id_product = arr_sku[1];
                        String price = arr_sku[2];
                        int price_1 = Integer.parseInt(price);
                        String email = obj.getString("digital_delivery_info");

                        String order_item_id = obj.getString("order_item_id");
                        int count = arr.length();
                        int total_price = price_1 * count;
                        int total_charge = obj.getInt("paid_price") * count;
                             // Lấy được các trường mong muốn
                        processCheckBy(order_number,product_code,id_product,email,count,total_price,total_charge);
                       }
                    }

                }


            }

        }


    }

    private void processCheckBy(String order_number, String product_code, String id_product, String email, int count, int total_price, int total_charge) throws IOException {
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
        RequestBody body = RequestBody.create(mediaType, "<mps>\n    <username>lazada</username>\n    <pass>"+password+"</pass>\n    <requestid>"+request_id+"</requestid>\n    <product_code>"+product_code+"</product_code>\n    <product_id>"+id_product+"</product_id>\n    <count>"+count+"</count>\n    <email>khangnd@mobifoneplus.com.vn</email>\n    <send_code_email>1</send_code_email>\n    <isdn>0903967333</isdn>\n    <cus_name>Lazada Test</cus_name>\n    <request_date>"+current+"</request_date>\n    <address>Lazada Test</address>\n    <total_price>"+total_price+"</total_price>\n    <total_charge>"+total_charge+"</total_charge>\n</mps>");
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

    private void processBuySim(String order_number, String sku, String name, String phone, String address, String email, int count, int total_price, int fee_ship, String order_item_id) throws IOException {
        Random generator = new Random();
        String request_id = "1" + generator.nextInt(999999);
        String shop_code= Preference.shop_code;
        String user= Preference.user;
        String passLaz= Preference.pass;
        String pass = request_id+shop_code+user+passLaz;
        String password = getMD5(pass);

        String url_sim= Preference.url_sim;
        String api_key= Preference.api_key;
        String data = "{\r\n    \"infor\":{\r\n        \"request_id\": \""+request_id+"\",\r\n        \"shop_code\":\""+shop_code+"\",\r\n        \"user\":\""+user+"\",\r\n        \"password\":\""+password+"\"\r\n    },\r\n    \"order\":{\r\n        \"product_code\":\""+sku+"\",\r\n        \"name\":\""+name+"\",\r\n        \"phone\": \""+phone+"\",\r\n        \"email\": \"khangnd@mobifoneplus.com.vn\",\r\n        \"address\":\""+address+"\",\r\n        \"count\": "+count+",\r\n        \"total_price\": "+total_price+",\r\n        \"fee_ship\": "+fee_ship+",\r\n        \"trans_id\": \""+order_item_id+"\",\r\n        \"status_payment\":1\r\n    }\r\n}\r\n";
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url_sim)
                .method("POST", body)
                .addHeader("api_key", api_key)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        String res = response.body().string();
        System.out.println(res);
        JSONObject json = null;
        try {
            json = new JSONObject(res);
            int status = json.getInt("status");
            Logger.info("lazop","{process.GetOrders} : "+"request(status: "+status+
                    ",\n data:"+data+"), response: "+res);
            if(status ==1){
                WriteOrder.writeOrder(order_number);
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
