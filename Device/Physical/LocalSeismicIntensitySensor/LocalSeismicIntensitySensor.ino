#include <WiFi.h>
#include <PubSubClient.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>
#include <cmath>

// Wifi credentials
const char* ssid = "test";
const char* password = "test12345";

// MQTT Broker settings
const char* mqtt_broker = "djx.entlab.hr";
const int mqtt_port = 8883;
const char* mqtt_username = "intstv";
const char* mqtt_password = "A4j6gC15br";

WiFiClient espClient;
PubSubClient mqttClient(espClient);

Adafruit_MPU6050 mpu;
sensors_event_t a, g, temp;

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

  String messageTemp;

  for (int i = 0; i < length; i++) {
    messageTemp += (char)message[i];
  }

  // Handle message received
  Serial.print("Message received: ");
  Serial.println(messageTemp);
}

void reconnect() {
  // Loop until we're reconnected
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (mqttClient.connect("ESP32Client", mqtt_username, mqtt_password)) {
      Serial.println("Connected to MQTT broker!");
      mqttClient.subscribe("device/actuation");
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

// Function to send a random message to the device/acceleration topic
void sendMessage() {
  float fakeAcceleration = random(0, 100) / 10.0;  // Generate a random number between 0 and 10
  String payload = String(fakeAcceleration);
  mqttClient.publish("device/acceleration", payload.c_str());
  Serial.printf("Published: %s to topic device/acceleration\n", payload.c_str());
}


void setup() {
  setupWiFi();

  Serial.begin(115200);
  while (!Serial)
    delay(10);

  //MPU6050_init();
  mqttClient.setServer(mqtt_broker, mqtt_port);
  mqttClient.setCallback(callback);
  
}

void loop() {
  if (!mqttClient.connected()) {
    reconnect();
  }

  mqttClient.loop();

  // Example of publishing a message with sensor data  - this is what we're going to send
  /*mpu.getEvent(&a, &g, &temp);
  float accelMagnitude = std::sqrt(a.acceleration.x * a.acceleration.x + a.acceleration.y * a.acceleration.y);
  String payload = String(accelMagnitude);
  mqttClient.publish("device/acceleration", payload.c_str());

  Serial.printf("%f g\n", accelMagnitude);

  delay(1000);*/

 // Send a message every 5 seconds to test
  static unsigned long lastSendTime = 0;
  if (millis() - lastSendTime > 5000) {
    sendMessage();
    lastSendTime = millis();
  }

  delay(100);

}
