package com.iot.server;

import com.google.gson.Gson;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


// This annotation instructs Spring to initialize its configuration - which is needed to start a new application
@SpringBootApplication
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

	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}


	@GetMapping("/simple-request")
	public String simpleRequest() {
		// In this case, we return the plain text response "ok"
		return "ok";
	}

	@PostMapping("/endpoint")
	@Timed(value = "post.time", description = "Time taken to execute the post method")
	public Boolean post_catch(@RequestBody Point point) {
		System.out.println("Receiving data with http protocol:");
		writeApi.writeMeasurement(WritePrecision.NS, point);


		if (point != null) {
			System.out.println(point.toString());
			return true;

		}
		return false;
	}

	// MQTT

	@Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

	@Bean
	@Timed(value = "mqtt_prod.time", description = "Time taken to execute the MessageProducer method")
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
					"tcp://192.168.178.69:1883", 
				"dataProxy",
                "esp32/point");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

	@Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
	@Timed(value = "mqtt_handler.time", description = "Time taken to execute the MessageHandler method")
    public MessageHandler handler() {
        return new MessageHandler() {

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
				Point p = new Gson().fromJson(message.getPayload().toString(), Point.class);

				writeApi.writeMeasurement(WritePrecision.NS, p);
				System.out.println("Receiving data with mqtt protocol:");
				System.out.println(p.toString());
            }

        };
    }
}
