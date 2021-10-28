package api.lazada.lazop.process;

import api.lazada.lazop.Thread.LoadConfig;
import api.lazada.lazop.api.LazopClient;
import api.lazada.lazop.api.LazopRequest;
import api.lazada.lazop.api.LazopResponse;
import api.lazada.lazop.util.ApiException;
import api.lazada.lazop.util.Preference;

import java.io.IOException;

import static api.lazada.lazop.util.UrlConstants.API_GATEWAY_URL_VN;

public class GetOrderItems {

    public static void select() throws IOException, ApiException {
        String url = API_GATEWAY_URL_VN;

        String appkey = Preference.appkey;
        String appSecret= Preference.appSecret;
        LazopClient client = new LazopClient(url, appkey, appSecret);
        LazopRequest request = new LazopRequest();
        request.setApiName("/order/items/get");
        request.setHttpMethod("GET");
        request.addApiParameter("order_id", "291474665828547");

        String accessToken= Preference.accessToken;
        LazopResponse response = client.execute(request, accessToken);
        System.out.println(response.getBody());
    }
}
