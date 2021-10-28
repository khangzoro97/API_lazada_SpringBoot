package api.lazada;

import api.lazada.lazop.Thread.LoadConfig;
import api.lazada.lazop.process.GetOrders;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class LazadaApplication {

	public static void main(String[] args) throws IOException {


		LoadConfig config = new LoadConfig();
		config.start();
		GetOrders list = new GetOrders();
		list.start();

		SpringApplication.run(LazadaApplication.class, args);
	}

}
