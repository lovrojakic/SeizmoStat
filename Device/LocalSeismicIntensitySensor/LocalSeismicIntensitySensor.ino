#include <WiFi.h>
#include <PubSubClient.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>
#include <cmath>
#include <ArduinoJson.h>

#define RESOURCE_URN "FER"

// Wifi credentials
const char* ssid = "Wokwi-GUEST";
const char* password = "";

// MQTT Broker settings
const char* mqtt_broker = "djx.entlab.hr";
const int mqtt_port = 8883;
const char* mqtt_username = "intstv";
const char* mqtt_password = "A4j6gC15br";

WiFiClient espClient;
PubSubClient mqttClient(espClient);

Adafruit_MPU6050 mpu;
sensors_event_t a, g, temp;
float accelMagnitudeSum = 0.0f;
int accelMagnitudeCount = 0;

int buzzerPin = 25;

void setupWiFi() {
  Serial.begin(115200); // init serial port

  // connecting to wifi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.println("Connecting to Wifi...");
  }

  Serial.println("Connected to WiFi.");
}

// Callback function to handle messages received on subscribed topics
// buzzer here probably
void callback(char* topic, byte* message, unsigned int length) {
  Serial.print("Message arrived on topic: ");
  Serial.println(topic);

  StaticJsonDocument<512> doc;
  deserializeJson(doc, message, length);
  if (doc["source"]["resource"] != RESOURCE_URN) {
    return;
  }
  if (doc["contentNodes"][0]["value"] == "ON") {
    ledcWriteTone(0, 440);
  } else if ((doc["contentNodes"][0]["value"] == "OFF")) {
    ledcWriteTone(0, 0);
  }
}

void reconnect() {
  // Loop until we're reconnected
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (mqttClient.connect("ESP32Client", mqtt_username, mqtt_password)) {
      Serial.println("Connected to MQTT broker!");
      mqttClient.subscribe("intstv_seizmostat/output/BuzzerStatus");
      Serial.println("Subscribed to actuation.");
    } else {
      Serial.print("failed, rc=");
      Serial.print(mqttClient.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void MPU6050_init() {
  Serial.println("Adafruit MPU6050 initialization started!");
  if (!mpu.begin()) {
    Serial.println("Failed to find MPU6050 chip");
    while (1) {
      delay(10);
    }
  }
  Serial.println("MPU6050 Found!");

  mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
  Serial.print("Accelerometer range set to: ");
  switch (mpu.getAccelerometerRange()) {
    case MPU6050_RANGE_2_G:
      Serial.println("+-2G");
      break;
    case MPU6050_RANGE_4_G:
      Serial.println("+-4G");
      break;
    case MPU6050_RANGE_8_G:
      Serial.println("+-8G");
      break;
    case MPU6050_RANGE_16_G:
      Serial.println("+-16G");
      break;
  }
  mpu.setGyroRange(MPU6050_RANGE_500_DEG);
  Serial.print("Gyro range set to: ");
  switch (mpu.getGyroRange()) {
    case MPU6050_RANGE_250_DEG:
      Serial.println("+- 250 deg/s");
      break;
    case MPU6050_RANGE_500_DEG:
      Serial.println("+- 500 deg/s");
      break;
    case MPU6050_RANGE_1000_DEG:
      Serial.println("+- 1000 deg/s");
      break;
    case MPU6050_RANGE_2000_DEG:
      Serial.println("+- 2000 deg/s");
      break;
  }
  mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);
  Serial.print("Filter bandwidth set to: ");
  switch (mpu.getFilterBandwidth()) {
    case MPU6050_BAND_260_HZ:
      Serial.println("260 Hz");
      break;
    case MPU6050_BAND_184_HZ:
      Serial.println("184 Hz");
      break;
    case MPU6050_BAND_94_HZ:
      Serial.println("94 Hz");
      break;
    case MPU6050_BAND_44_HZ:
      Serial.println("44 Hz");
      break;
    case MPU6050_BAND_21_HZ:
      Serial.println("21 Hz");
      break;
    case MPU6050_BAND_10_HZ:
      Serial.println("10 Hz");
      break;
    case MPU6050_BAND_5_HZ:
      Serial.println("5 Hz");
      break;
  }

  Serial.println("");
  delay(100);
}

void sendMessage() {
  // Create JSON object
  StaticJsonDocument<512> doc;

  // Specify the sender resource
  doc["source"]["resource"] = RESOURCE_URN;
  JsonArray contentNodes = doc.createNestedArray("contentNodes");

  // Add the magnitude to the JSON object
  JsonObject magnitudeNode = contentNodes.createNestedObject();
  magnitudeNode["value"] = accelMagnitudeSum/accelMagnitudeCount;

  // Serialize JSON
  char json[256];
  size_t n = serializeJson(doc, json);

  // Publish the message
  mqttClient.publish("intstv_seizmostat/input/Intensity", json, n);
  Serial.printf("Topic: intstv_seizmostat/input/Intensity\nPublished: %s\n\n", json);
}


void setup() {
  setupWiFi();

  Serial.begin(115200);
  while (!Serial)
    delay(10);

  MPU6050_init();

  mqttClient.setServer(mqtt_broker, mqtt_port);
  mqttClient.setCallback(callback);
  mqttClient.setBufferSize(512);

  ledcSetup(0, 10000, 12);
  ledcAttachPin(buzzerPin, 0);
}

void loop() {
  if (!mqttClient.connected()) {
    reconnect();
  }

  mqttClient.loop();

  mpu.getEvent(&a, &g, &temp);
  accelMagnitudeSum += std::sqrt(a.acceleration.x * a.acceleration.x + a.acceleration.y * a.acceleration.y);
  accelMagnitudeCount++;

 // Send a message every 1 second
  static unsigned long lastSendTime = millis();
  if (millis() - lastSendTime > 1000) {
    sendMessage();
    lastSendTime = millis();
    accelMagnitudeSum = 0.0f;
    accelMagnitudeCount = 0;
  }

  delay(100);

}
