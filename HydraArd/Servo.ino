// setupServos: sets pin associations, control variables
void setupServos(){
  for (int i = 0; i < numServos; i++) {
    // Initialize control values
    servoControl[i] = SERV_START[i];                      // All Servo's start out at least extension
    lastControl[i] = SERV_START[i];

    RESP_MIN[i] = floor((SERV_END[i] - SERV_START[i])/50);
    RESP_MAX[i] = floor((SERV_END[i] - SERV_START[i])/5);

    servoDirection[i] = SERV_START[i] < SERV_END[i];
    
    // Attach servos to pins
    pinMode(servoPins[i], OUTPUT);
    servos[i].attach(servoPins[i]);
  }
}

// moveMuscle(long): if mapped EMG reading represents a flex, increment Servo control values based on parameters
void moveMuscle(long reading){

    bool fullyExtended = true;                                    // True if all Servo's are fully extended
    
    for (int i = 0; i < numServos; i++){

      // Map reading to given Servo
      long mappedReading = mapReading(reading, i);

      // Clockwise Servo rotation
      if(servoDirection[i]){

        // User is engaging
        if (mappedReading > SERV_GO[i]){   
          if(servoControl[i] < servoDepth[i]) {                      // Is Servo able extend further?
            if(servoControl[i] + servoResp[i] < servoDepth[i]) {     // Can it extend one more response step?
              servoControl[i] = servoControl[i] + servoResp[i];      // Update servoControl by one step
              fullyExtended = false;
            }
            else {
              servoControl[i] = servoDepth[i];                       // Update servoControl to max depth
            }
          }
        }

        // User not engaging
        else{  
          fullyExtended = false;                                                      
          if(servoControl[i] > SERV_START[i]){                         // Is Servo at base position?
            if(servoControl[i] - servoResp[i] > SERV_START[i]) {       // Can it come back one more response step?
              servoControl[i] = servoControl[i] - servoResp[i];      // If yes, bring back
            }
            else {
              servoControl[i] = SERV_START[i];                         // If not, set to base value
            }
          }
        }
      }

      // Counterclockwise Servo rotation
      else{
        
        // User is engaging
        if (mappedReading < SERV_GO[i]){
          if(servoControl[i] > servoDepth[i]) {                      // Is Servo able extend further?
            if(servoControl[i] + servoResp[i] > servoDepth[i]) {     // Can it extend one more response step?
              servoControl[i] = servoControl[i] + servoResp[i];      // Update servoControl by one step
              fullyExtended = false;
            }
            else {
              servoControl[i] = servoDepth[i];                       // Update servoControl to max depth
            }
          }
        }

        // User not engaging
        else{
          fullyExtended = false;                                                        
          if(servoControl[i] < SERV_START[i]){                         // Is Servo at base position?
            if(servoControl[i] - servoResp[i] < SERV_START[i]) {       // Can it come back one more response step?
              servoControl[i] = servoControl[i] - servoResp[i];        // If yes, bring back
            }
            else {
              servoControl[i] = SERV_START[i];                         // If not, set to base value
            }
          }
        }
      }
    }

    // Write to Servos if changes occurred
    for (int i = 0; i < numServos; i++){
      if (servoControl[i] != lastControl[i]) {                  // Only write if changes have occurred
        servos[i].writeMicroseconds(servoControl[i]);           // Write control values to each Servo
        lastControl[i] = servoControl[i];                       // Update last control values
      }
    }

    // Hold if in Static mode and fully extended: wait for app to cancel the position
    if (!dynamic && fullyExtended){
        // TODO: send message to app as only way to break position???
        waitForACK();
    }
}

// retract(): bring fingers to base position
void retract(){
  bool isRetracted = false;
  while (!isRetracted){
    moveMuscle(ARM_LOW);
    isRetracted = true;
    for (int i = 0; i < numServos; i ++){
      // Fully retracted when all control values at start position
      isRetracted = isRetracted && (servoControl[i] == SERV_START[i]);
    }
  }
}
