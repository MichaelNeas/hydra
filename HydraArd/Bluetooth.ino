// setupBluetooth: starts bluetooth serial comm, prepares bluetooth for device connection
void setupBluetooth(){
  pinMode(RX_PIN, INPUT);
  pinMode(TX_PIN, OUTPUT);
  bluetooth.begin(BTOOTH_BAUD);

  // App sends acknowledgement when started
  waitForSTART();
}

// readBluetooth: reads next Bluetooth message, calls appropriate command based on message indicator character
// C = calibrate; int = paramUpdate; close message -> run setup
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
  else if (msgType == CLOSE_CHAR){
    bluetooth.flush();
    setup();
  }
  else {
    bluetooth.flush();
  }
}

// Wait for message from Hydra app
void waitFor(int msgNum){
  char done;
  switch (msgNum){
    // 0 is general ACK message
    case 0:{
      done = ACK_CHAR;
    }
    break;

    // 1 is START message
    case 1:{
      done = START_CHAR;
    }
    break; 
  }

  // Wait for desired message
  while (true){
    if (bluetooth.read() == done){
      Serial.println("Awaited msg received");
      bluetooth.flush();
      break;
    }
  }
  
}

void waitForACK(){
  waitFor(0);
}

void waitForSTART(){
  waitFor(1);
}

