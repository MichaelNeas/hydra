#include <Servo.h>
#include <SoftwareSerial.h>
#include <Time.h>
#include <TimeLib.h>

#include "config.h"

// SERVO VARIABLES 
Servo servos[numServos];                // Array of servos

int servoControl[numServos];            // Servo control values for each Servo
int lastControl[numServos];             // Last values written to Servos
int servoResp[numServos];               // Controls change in sweep of each Servo, i.e. grip speed
int servoDepth[numServos];              // Controls extent of Servo grip (some % of full grip)

bool servoDirection[numServos];         // Direction of rotation for each Servo
                                        // False = counterclockwise, true = clockwise; {Index, Outer, Thumb}

bool dynamic = true;                    // Determines if Servo responds to sensor input

int SERV_GO[numServos];                 // Threshold for initiating Servo movement for each Servo

int WRITE_DELAY;                        // Delay (microseconds) between writes to servos

int RESP_MIN[numServos];                // Minimum increment value for Servo movement
int RESP_MAX[numServos];                // Maximum increment value for Servo movement



// EMS (INPUT) VARIABLES
int ARM_LOW = 100;                      // Min default sensor value
int ARM_HIGH = 1000;                    // Max default sensor value
long reading;                           // Reading from EMS


// BLUETOOTH COMMUNICATION VARIABLES
SoftwareSerial bluetooth(rxPin, txPin);     // Software serial comm via bluetooth on rx/tx

String defaultMsg = "1=D;2=0.5;3=5.0;4=100,100,100;5=5.0,5.0,5.0;"; // Default settings message


void setup() {
  //Serial.begin(9600); // For debug/print messages

  // Initialize default parameters
  pinMode(EMS_PIN, INPUT);
  setupServos();
  setupBluetooth();
}



void loop() {  
  // Check for bluetooth input
  if (bluetooth.available() > 0) {
    readBluetooth();
  }

  // If no messages, process movement
  else {
    
  // 1. Obtain sensor reading
    reading = readMuscle();
    
  // 2. Based on sensor readings, write to Servo's
    moveMuscle(reading);
    
    delayMicroseconds(WRITE_DELAY);                               // Delay before taking next reading and writing values
  }
}



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

// setupBluetooth: starts bluetooth serial comm, prepares bluetooth for device connection
void setupBluetooth(){
  pinMode(rxPin, INPUT);
  pinMode(txPin, OUTPUT);
  bluetooth.begin(BTOOTH_BAUD);
  
  while (bluetooth.available() <= 0){
    // App sends default parameter message before engaging in motion
    delay(2000);
  }
}

// readBluetooth: reads next Bluetooth message, calls appropriate command based on message indicator character
// C = calibrate; else = parameter update
void readBluetooth(){
  char msgType = bluetooth.read();
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

// readMuscle: gets EMG sensor reading
long readMuscle(){
  long sensorVal = getSensorReading(NUM_READS);
  long cutoffVal = constrain(sensorVal, ARM_LOW, ARM_HIGH);
  return cutoffVal;
}

// mapReading(int): map EMG reading to respective Servo i
long mapReading(long cutoffVal, int i){
  long mappedVal = map(cutoffVal, ARM_LOW, ARM_HIGH, SERV_START[i], SERV_END[i]);
  return mappedVal;
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

// paramUpdate(int): sets a parameter based on incoming bluetooth message, parameter number
void paramUpdate(int paramNum){
  char current;                           // Current character being read
  int it = 0;                             // Iterator for parsing
  String bToothMsg = "";                  // String to hold message
  
  // Get message up to semicolon
  while (current != ';') {
    current = bluetooth.read();
    bToothMsg.concat(current);
  }

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

  bToothMsg = ""; 
}


// calibrate: sets ARM_HIGH and ARM_LOW to new values
void calibrate(){
  long currentReading;

  // Retract fingers to base before calibrating 
  retract();
  
  // Stage 1
  waitForACK();         // Wait for "Relaxed" signal
  
  currentReading = getSensorReading(NUM_READS^2);
  ARM_LOW = (int) currentReading;
  // Low threshold set
 
  // Stage 2
  waitForACK();         // Wait for "Flexed" signal
  
  currentReading = getSensorReading(NUM_READS^2);
  ARM_HIGH = (int) currentReading;
  // High threshold set
  
  bluetooth.flush();

  waitForACK();
}

// waitForACK(): continuously flushes bluetooth buffer until acknowledgement (a character '1') received
void waitForACK(){
  while (true){
    if (bluetooth.read() == '1'){
      bluetooth.flush();
      break;
    }
  }
}

// UTILITY METHODS

// average input EMS readings
long getSensorReading(int n){
  long readings[n];
  for(int i = 0; i < n; i++){
   readings[i] = analogRead(EMS_PIN);
  }
  return mean(readings, n);
}

// mean of an array
long mean(long array[], int length){
  long temp = 0;
  for(int i = 0; i < length; i++){
    temp += array[i]; 
  }
  return temp/length; 
}

