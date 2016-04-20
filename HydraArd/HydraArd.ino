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
SoftwareSerial bluetooth(RX_PIN, TX_PIN);     // Software serial comm via bluetooth on rx/tx

String defaultMsg = "1=D;2=0.5;3=5.0;4=100,100,100;5=5.0,5.0,5.0;"; // Default settings message


void setup() {
  Serial.begin(9600); // For debug/print messages

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
