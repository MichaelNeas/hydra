// To handle duplicate messages
String lastParams[numParams];

// paramUpdate(int): sets a parameter based on incoming bluetooth message, parameter number
void paramUpdate(int paramNum){
  char current;                           // Current character being read
  int it = 0;                             // Iterator for parsing
  String bToothMsg = "";                  // String to hold message

  // Previous message to prevent duplicate sends
  String lastbToothMsg = lastParams[paramNum - 1];
  
  // For preventing errors via continuing messages
  int charCount = 0;
  
  // Get message up to semicolon
  while (current != ';') {
    current = bluetooth.read();
    bToothMsg.concat(current);

    // If message continues for too long, flush bluetooth, error occurred
    if (charCount == maxMessageCount){
      Serial.println("Message length exceeded");
      bluetooth.flush();
      break;
    }
  }

  Serial.print("bToothMsg = ");
  Serial.println(bToothMsg);

  Serial.print("last bToothMsg = ");
  Serial.println(lastbToothMsg);

  if (bToothMsg == lastbToothMsg){
    // Do nothing if duplicate message
    Serial.println("Duplicate message");
  }
  else {
    switch (paramNum) {
    
        case 1: // Parameter 1 - Dynamic or Static - D, S
        {
          int it = 0;
          for (it; it < bToothMsg.length(); it++) {
            if (bToothMsg[it] == 'D') {
              dynamic = true;
              break;
            }
            else if (bToothMsg[it] == 'S') {
              dynamic = false;
              break;
            }
          }
        }
        break;
    
        
        case 2: // Parameter 2 - Action Threshold - SERV_GO
          // (Low 0.05 to High 0.75)*(SERV_END - SERV_START) + SERV_START
        {
          String ATStr = "";
          // Get characters after "2=" up to ';'
          for (it; it < bToothMsg.length(); it++) {
            if (bToothMsg[it] == '=' || bToothMsg[it] == ';'){
              // Do nothing
            }
            else {
              ATStr.concat(bToothMsg[it]);
            }
          }
          
          int newATStrLength = ATStr.length();
          char newATStr[newATStrLength];
          for (int i = 0; i < newATStrLength; i++){
            newATStr[i] = ATStr[i];
          }
          float ATVal = atof(newATStr); // Convert received characters for param 2 to float value
    
          // Set SERV_GO threshold for each Servo
          for (int i = 0; i < numServos; i++){
            SERV_GO[i] = floor(ATVal*(SERV_END[i] - SERV_START[i])) + SERV_START[i];
          }
          
        }
        break;
    
        
        case 3: // Parameter 3 - Grip Speed - WRITE_DELAY
          // (Low 1.0 to High 10.0) -> 100 us to 10000 us
        {  
          float SET_DELAY_LOW = 1.0;
          float SET_DELAY_HIGH = 10.0;
          
          String delStr = "";
          // Get characters after "3=" up to ';'
          for (it; it < bToothMsg.length(); it++) {
            if (bToothMsg[it] == '=' || bToothMsg[it] == ';'){
              // Do nothing
            }
            else {
              delStr.concat(bToothMsg[it]);
            }
          }
          
          int newDelStrLength = delStr.length();
          char newDelStr[newDelStrLength];
          for (int i = 0; i < newDelStrLength; i++){
            newDelStr[i] = delStr[i];
          }
          float delVal = atof(newDelStr); // Convert received characters for param 3 to float value
          WRITE_DELAY = (int) floor(map(delVal, SET_DELAY_LOW, SET_DELAY_HIGH, DELAY_MIN, DELAY_MAX));
        }
        break;
    
    
        case 4: // Parameter 4 - Grip Depth per Servo
          // ((Shallow 0 to Deep 100)/100)*(SERV_END - SERV_START) + SERV_START
        {  
          String fullDepthStr = "";
          // Get characters after "4=" up to ';'
          for (it; it < bToothMsg.length(); it++) {
            if (bToothMsg[it] == '=' || bToothMsg[it] == ';'){
              // Do nothing
            }
            else {
              fullDepthStr.concat(bToothMsg[it]);
            }
          }
          
          int jt = 0;
          for (int i = 0; i < numServos; i ++){
            String depthStr = "";
            // Get number before next comma or end of message
            while (fullDepthStr[jt] != ',' && jt < fullDepthStr.length()){
              depthStr.concat(fullDepthStr[jt]);
              jt ++; 
            }
            jt ++;
            int newDepthStrLength = depthStr.length();
            char newDepthStr[newDepthStrLength + 1];
            for (int i = 0; i < newDepthStrLength; i++){
              newDepthStr[i] = depthStr[i];
            }
            newDepthStr[newDepthStrLength] = NULL;
            float depthVal = atoi(newDepthStr);
            float depthPercent = depthVal/100;
            servoDepth[i] = floor((depthPercent)*(SERV_END[i] - SERV_START[i])) + SERV_START[i];
          }
        }
        break;
    
        case 5: // Parameter 5 - Responsivity per Servo
          // (Low 1.0 to High 10.0) -> NEED RANGE FOR SERVO.WRITEMICROSECONDS
        {  
          float SET_RESP_LOW = 1.0;
          float SET_RESP_HIGH = 10.0;
          
          String fullRespStr = "";
          // Get characters after "5=" up to ';'
          for (it; it < bToothMsg.length(); it++) {
            if (bToothMsg[it] == '=' || bToothMsg[it] == ';'){
              // Do nothing
            }
            else {
              fullRespStr.concat(bToothMsg[it]);
            }
          }
          
          int kt = 0;
          for (int i = 0; i < numServos; i ++){
            String respStr = "";
            while (fullRespStr[kt] != ',' && kt < fullRespStr.length()){
              respStr.concat(fullRespStr[kt]);
              kt ++; 
            }
            kt ++;
            int newRespStrLength = respStr.length();
            char newRespStr[newRespStrLength + 1];
            for (int i = 0; i < newRespStrLength; i++){
              newRespStr[i] = respStr[i];
            }
            newRespStr[newRespStrLength] = NULL;
            float respVal = atof(newRespStr);
            servoResp[i] = (int) floor(map(respVal, SET_RESP_LOW, SET_RESP_HIGH, RESP_MIN[i], RESP_MAX[i]));
          }
        }
        break;
      }
    }

    
    lastParams[paramNum - 1] = bToothMsg;

    bToothMsg = ""; 
}


// calibrate: sets ARM_HIGH and ARM_LOW to new values
void calibrate(){
  long currentReading;
  int CAL_DELAY = 1000;
  
  Serial.println("Calibrate called");
  // Retract fingers to base before calibrating 
  retract();

  delay(CAL_DELAY);
  
  // Stage 1
  waitForACK();         // Wait for "Relaxed" signal
  
  currentReading = getMinReading(NUM_READS^2);
  ARM_LOW = (int) currentReading;
  // Low threshold set
  Serial.println(ARM_LOW);

  delay(CAL_DELAY);
  
  // Stage 2
  waitForACK();         // Wait for "Flexed" signal
  
  currentReading = getMaxReading(NUM_READS^2);
  ARM_HIGH = (int) currentReading;
  // High threshold set
  Serial.println(ARM_HIGH);
  
  bluetooth.flush();
  
  delay(CAL_DELAY);

  waitForACK();
}

