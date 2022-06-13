package com.iot.server;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


// This annotation instructs Spring to initialize its configuration - which is needed to start a
// new application
@SpringBootApplication
// Indicates that this class contains RESTful methods to handle incoming HTTP requests
@RestController
public class ApplicationController {

	private static InfluxDBClient client;
	private static WriteApiBlocking writeApi;


	// We can start our application by calling the run method with the primary class
	public static void main(String[] args) {

		SpringApplication.run(ApplicationController.class, args);

		String token = "vzHGyarqLYKp9O-sT2DeuE1kDOne-16Iox-Oy0GLiGsFua-5CqitjHoGvWoz3Lv39-2WrSzb_b2_c0_gK5IbQw==";
		String url = "http://localhost:8086";
		String bucket = "IOT_exam";
		String org = "Unibo";

		client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
		writeApi = client.getWriteApiBlocking();
	}


	@GetMapping("/simple-request")
	public String simpleRequest() {
		// In this case, we return the plain text response "ok"
		return "ok";
	}

	@PostMapping("/endpoint")
	public Boolean post_catch(@RequestBody Point point) {
		writeApi.writeMeasurement(WritePrecision.NS, point);


		if (point != null) {
			System.out.println(point.toString());
			return true;

		}
		return false;
	}
}
