# FallApp

This application was developed as part of a master's degree for the UC Denver Bioengineering program. It communicates with the Benchmark Electronics ScioMed G4 wearable patch over BLE.
The application is currently includes a UI that initates BLE scanning and connection and provides the UI inputs to read BLE attributes and change system state.
The BLE connection process is based on [Android documentation](https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview) and [Punch Through's Android BLE guide](https://punchthrough.com/android-ble-guide/)

## Next Steps
The next step is to save the BLE connection object so it can be accessed by the UI elements. 
The list of BLE attributes is recorded in the app log but also are listed in a document from Benchmark.
A queue should also be implemented to prevent ensure that a previous BLE operation is finished before another is initiated.
Benchmark will also explain how the process of setting the patch to an active, recording state.
