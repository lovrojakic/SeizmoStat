# SeizmoStat

## Sensor Network
1. Connect your board along with your components according to the schema (`Docs/`) to a computer
2. Open project in Arduine IDE
3. Choose the correct board (eg. ESP32)
4. Choose the correct port (`/dev/ttyUSBx` or `COMx` depending on your OS)
5. Upload the code to the board. This will compile the current firmware and flash it to the board.

## IoT Platform
1. Add sensors to platform
2. Add actuators to platform
3. Add subscriptions to arbitrary topics

## Actuation
1. `pip install paho-mqtt`
2. `python3 Actuation/BuzzerLogic.py`

## Mobile App
1. Run `./MobileApp/gradlew` (Linux) or `\MobileApp\gradlew.bat` (Windows) to build the app
2. Run the app on an emulator or a physical device

## Web App
1. Run `npm install` to install dependencies
2. Run `npm start` to start the app
