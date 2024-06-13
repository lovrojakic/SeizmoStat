from paho.mqtt import client as mqtt_client
import random, json

FIRST_RECONNECT_DELAY = 1
RECONNECT_RATE = 2
MAX_RECONNECT_COUNT = 12
MAX_RECONNECT_DELAY = 60

broker = 'djx.entlab.hr'
port = 8883
topic = "intstv_seizmostat/input/Intensity"
publish = "intstv_seizmostat/output/BuzzerStatus"
client_id = f'python-mqtt-{random.randint(0, 1000)}'
username = 'intstv'
password = 'A4j6gC15br'
threshold = 0.12

def buzzerLogic(intensity):
    if intensity > threshold:
        return "ON"
    else:
        return "OFF"

def connect_mqtt() -> mqtt_client:
    def on_connect(client, userdata, flags, rc):
        if rc == 0:
            print("Connected to MQTT Broker!")
        else:
            print("Failed to connect, return code %d\n", rc)

    client = mqtt_client.Client(mqtt_client.CallbackAPIVersion.VERSION1, client_id)
    client.username_pw_set(username, password)
    client.on_connect = on_connect
    client.connect(broker, port)
    return client

def subscribe(client: mqtt_client):
    def on_message(client, userdata, msg):
        message = msg.payload.decode()
        message = json.loads(message)
        # {'source': {'resource': 'FER'}, 'contentNodes': [{'value': 1.552906752}]}
        resource = message['source']['resource']
        intensity = message['contentNodes'][0]['value']
        buzzerStatus = buzzerLogic(float(intensity))
        final = {'source': {'resource': resource}, 'contentNodes': [{'value': buzzerStatus}]}
        client.publish(publish, json.dumps(final))
        print(f"Published: {final}\n")
    client.subscribe(topic)
    client.on_message = on_message

def run():
    client = connect_mqtt()
    subscribe(client)
    client.loop_forever()


if __name__ == '__main__':
    run()