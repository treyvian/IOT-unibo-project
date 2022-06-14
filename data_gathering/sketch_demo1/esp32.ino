#include "DHTesp.h"
#include "WiFi.h"
#include "HTTPClient.h"

//pin
#define mq2 34
#define DHTpin 27

// wifi config
#define ssid "FRITZ7490"
#define password "Dade1998"

#define serverName "http://192.168.178.69:8080/endpoint"

#define latitude (float)44.49435102646965
#define longitude (float)11.346639990806581

#define BUF_SIZE 5

unsigned long lastTime = 0;
// 10 minutes = 600000
// Set timer to 5 seconds (5000)
unsigned long SAMPLE_FREQUENCY = 15000;


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
  WiFi.mode(WIFI_STA);          //The WiFi is in station mode. The other is the softAP mode
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
     delay(500);
     Serial.print(".");
  }
  Serial.println("");  
  Serial.print("WiFi connected to: "); 
  Serial.println(ssid);  
  Serial.println("IP address: ");  
  Serial.println(WiFi.localIP());
  delay(2000);
  
  dht.setup(DHTpin, DHTesp::DHT11);
  pinMode(mq2, INPUT);
}

void loop()
{  
  //dht
  float humidity = dht.getHumidity();
  float temperature = dht.getTemperature();

  uint32_t chip_id = ESP.getEfuseMac();

  //mq
  int gas = analogRead(mq2);
   
  int avg = sliding_avg(gas);
  Serial.println(avg); 

  // AQI
  int8_t AQI = 2;
  if (avg >= MAX_GAS_VALUE){
    AQI = 0; 
  }
  if (avg < MAX_GAS_VALUE && MIN_GAS_VALUE <= avg){
    AQI = 1;
  }

  int rssi = WiFi.RSSI();

  //Send an HTTP POST request every 10 minutes
  if ((millis() - lastTime) > SAMPLE_FREQUENCY) {
    
    // Check WiFi connection status
    if(WiFi.status()== WL_CONNECTED){
      WiFiClient client;
      HTTPClient http;

      // Your Domain name with URL path or IP address with path
      http.begin(client, serverName);

      http.addHeader("Content-Type", "application/json");

      String body = "{\"id\":" + String(chip_id) + ",\"latitude\":" + latitude + ",\"longitude\":" + longitude 
                    + ",\"rssi\":" + rssi + ",\"temperature\":" + temperature +
                    ",\"humidity\":" + humidity + ",\"gas\":" + gas + ",\"aqi\":" + AQI + "}";
      int httpResponseCode = http.POST(body);

      // Free resources
      http.end();
    } else {
      Serial.println("WiFi Disconnected");
    }
    lastTime = millis();
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