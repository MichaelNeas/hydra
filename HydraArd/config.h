const int numServos = 3;                          // Number of Servo motors controlling fingers

const int servoPins[numServos] = {11, 12, 13};    // Arduino pins for Servo output (must be same amount of numServos!)
                                                  // {Index, Outer, Thumb}
                                                  
const int EMS_PIN = 14;                           // Arduino pin for EMG Sensor input
const int rxPin = 2;                              // Arduino pin for reading from Bluetooth
const int txPin = 3;                              // Arduino pin for writing to Bluetooth

const int SERV_START[numServos]                   // Servo starting position
  = {1800, 1800, 1800};                           // {Index, Outer, Thumb}
const int SERV_END[numServos]                     // Servo full extension position
  = {600, 600, 2400};                             // {Index, Outer, Thumb}

const int DELAY_MIN = 100;                        // Minimum value for delay between writes to Servo's
const int DELAY_MAX = 10000;                      // Maximum value for delay between writes to Servo's

const int NUM_READS = 200;                        // Number of readings to average from EMS input (granularity)

const int BTOOTH_BAUD = 9600;                     // Bluetooth baud rate
