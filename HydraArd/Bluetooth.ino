// setupBluetooth: starts bluetooth serial comm, prepares bluetooth for device connection
void setupBluetooth(){
  pinMode(rxPin, INPUT);
  pinMode(txPin, OUTPUT);
  bluetooth.begin(BTOOTH_BAUD);

  // App sends acknowledgement of bluetooth connection
  waitForACK();
}

// readBluetooth: reads next Bluetooth message, calls appropriate command based on message indicator character
// C = calibrate; else = parameter update
void readBluetooth(){
  char msgType = bluetooth.read();
  Serial.print("msgType = ");
  Serial.println(msgType);
  if (msgType == 'C') { // Calibration
    calibrate();
  }
  else if (msgType == '1' || msgType == '2' || msgType == '3' || msgType == '4' || msgType == '5'){ // Parameter update
    int paramNum = msgType - '0';
    paramUpdate(paramNum);
  }
  else {
    bluetooth.flush();
  }
}

// waitForACK(): continuously flushes bluetooth buffer until acknowledgement (a character '1') received
void waitForACK(){
  while (true){
    if (bluetooth.read() == 'X'){
      Serial.println("ACK received");
      bluetooth.flush();
      break;
    }
  }
}
