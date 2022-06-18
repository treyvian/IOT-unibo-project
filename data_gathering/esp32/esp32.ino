/*********
  Davide Pozzoli
  Complete project details at https://github.com/treyvian/IOT-unibo-project
*********/
#include "env.h"
#include "DHTesp.h"
#include <PubSubClient.h>
#include <WiFi.h>
#include <HTTPClient.h>

//pin
#define mq2 34
#define DHTpin 27

#define serverName "http://192.168.178.69:8080/endpoint"
#define ID "IOT_exam"

#define latitude (float)44.49435102646965
#define longitude (float)11.346639990806581

#define BUF_SIZE 5

unsigned long lastTime = 0;
// 10 minutes = 600000
// Set timer to 5 seconds (5000)
unsigned long SAMPLE_FREQUENCY = 15000;

// trasmission protocol
boolean is_http = 1;

//MQTT
IPAddress broker(192,168,178,69);


WiFiClient wifi_client_mqtt;
WiFiClient wifi_client_http;

PubSubClient client(wifi_client_mqtt);

DHTesp dht;

//MQ-2 variables      
int8_t buf_index = 0;
int buf[BUF_SIZE] = {0};
int sum = 0;
int MIN_GAS_VALUE = 50;
int MAX_GAS_VALUE = 900;


void setup()
{    
  Serial.begin(115200);
  delay(100);
  
  dht.setup(DHTpin, DHTesp::DHT11);
  pinMode(mq2, INPUT);

  setup_wifi();
  client.setServer(broker, 1883);
  client.setCallback(callback);
  mqtt_connect();
}

void loop(){ 
  client.loop();
  
  uint32_t chip_id = ESP.getEfuseMac();

  float humidity = dht.getHumidity();
  float temperature = dht.getTemperature();
  int gas = analogRead(mq2);
   
  int avg = sliding_avg(gas);
  // AQI
  int8_t AQI = 2;
  if (avg >= MAX_GAS_VALUE){
    AQI = 0; 
  }
  if (avg < MAX_GAS_VALUE & MIN_GAS_VALUE <= avg){
    AQI = 1;
  }

  int rssi = WiFi.RSSI();

  if ((millis() - lastTime) > SAMPLE_FREQUENCY) {
    
    // Check WiFi connection status
    if(WiFi.status()== WL_CONNECTED){
      String body = "{\"id\":" + String(chip_id) + ",\"latitude\":" + latitude + ",\"longitude\":" + longitude 
                      + ",\"rssi\":" + rssi + ",\"temperature\":" + temperature +
                      ",\"humidity\":" + humidity + ",\"gas\":" + gas + ",\"aqi\":" + AQI + "}";
      if(is_http){
        HTTPClient http;
        // Your Domain name with URL path or IP address with path
        http.begin(wifi_client_http, serverName);
        
        http.addHeader("Content-Type", "application/json");

        
        int httpResponseCode = http.POST(body);
        
        // Free resources
        http.end();
      } else {
        client.publish("esp32/point", body.c_str());
      }
    } else {
      Serial.println("WiFi Disconnected");
      setup_wifi();
      mqtt_connect();
    }
    lastTime = millis();
  }

}

void setup_wifi() {
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  while (WiFi.status() != WL_CONNECTED) {
     delay(1000);
     Serial.print(".");
  }
  Serial.println("");  
  Serial.print("WiFi connected to: "); 
  Serial.println(WIFI_SSID);  
  Serial.println("IP address: ");  
  Serial.println(WiFi.localIP());
}

void mqtt_connect(){
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    if(client.connect(ID)) {
      Serial.println("Connected to broker");
      client.subscribe("esp32/max_gas");
      client.subscribe("esp32/frequency");
      client.subscribe("esp32/min_gas");
      client.subscribe("esp32/protocol");
    } else {
        Serial.println(" try again in 5 seconds");
        delay(5000);
    }
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  String response;

  for (int i = 0; i < length; i++) {
    response += (char)payload[i];
  }
  Serial.print("Message arrived in topic:");
  Serial.print(topic);
  Serial.print(" ");
  Serial.println(response);
  
  // Protocol
  if (String(topic) == "esp32/protocol") {
    Serial.print("Changing protocol to ");
    if(response == "http"){
      Serial.println("http");
      is_http = 1;
    }
    else if(response == "mqtt"){
      Serial.println("mqtt");
      is_http = 0;
    }
  }

  if (String(topic) == "esp32/frequency") {
    unsigned long freq = response.toInt();
    if(freq > 0){
      SAMPLE_FREQUENCY = freq*1000;
    } else {
      Serial.println("Sample frequency must be a positive integer different from 0");
    }
  }
 if (String(topic) == "esp32/max_gas") {
    long max_gas = response.toInt();
    MAX_GAS_VALUE = max_gas;
  }

  
  if (String(topic) == "esp32/min_gas") {
    long min_gas = response.toInt();
    MIN_GAS_VALUE = min_gas;
  }
}

int sliding_avg(int new_value){
  if (BUF_SIZE == buf_index) {buf_index = 0;}
  sum -= buf[buf_index];
  sum += new_value;
  buf[buf_index] = new_value;
  buf_index++;
  return sum / BUF_SIZE;
}
