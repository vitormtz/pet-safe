#include <WiFi.h>
#include <HTTPClient.h>
#include <TinyGPSPlus.h>

const char* WIFI_SSID = "SUA_REDE";
const char* WIFI_PASS = "SUA_SENHA";
const char* SERVER_URL = "http://177.44.248.27:5000/dados";

TinyGPSPlus gps;
HardwareSerial gpsSerial(1); // UART1

void conectaWiFi(){
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  while (WiFi.status() != WL_CONNECTED) { delay(500); }
}

void setup() {
  Serial.begin(115200);
  gpsSerial.begin(9600, SERIAL_8N1, 16, 17); // RX,TX
  conectaWiFi();
}

void loop() {
  while (gpsSerial.available()) gps.encode(gpsSerial.read());

  static uint32_t last=0;
  if (millis()-last > 5000) {
    last = millis();
    if (gps.location.isValid()) {
      float lat = gps.location.lat();
      float lon = gps.location.lng();
      float spd = gps.speed.kmph();
      int sats  = gps.satellites.value();

      String payload = String("{")+
        "\"device\":\"esp32_gps\","+
        "\"lat\":"+String(lat,6)+","+
        "\"lon\":"+String(lon,6)+","+
        "\"speed\":"+String(spd,2)+","+
        "\"sats\":"+String(sats)+","+
        "\"utc\":\""+ String(gps.time.isValid()? String(gps.time.hour())+":"+String(gps.time.minute())+":"+String(gps.time.second()) : "na") +"\","+
        "\"date\":\""+ String(gps.date.isValid()? String(gps.date.day())+"/"+String(gps.date.month())+"/"+String(gps.date.year()) : "na") +"\"}";

      HTTPClient http;
      http.begin(SERVER_URL);
      http.addHeader("Content-Type","application/json");
      int code = http.POST(payload);
      http.end();

      Serial.printf("POST %d -> %s\n", code, payload.c_str());
    } else {
      Serial.println("GPS sem fix válido...");
    }
  }
}
