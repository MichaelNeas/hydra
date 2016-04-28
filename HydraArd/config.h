const int numServos = 3;                          // Number of Servo motors controlling fingers

const int numParams = 5;                          // Number of Servo parameters

const int servoPins[numServos] = {11, 12, 13};    // Arduino pins for Servo output (must be same amount of numServos!)
                                                  // {Index, Outer, Thumb}
                                                  
const int EMS_PIN = 14;                           // Arduino pin for EMG Sensor input
const int RX_PIN = 2;                             // Arduino pin for reading from Bluetooth
const int TX_PIN = 3;                             // Arduino pin for writing to Bluetooth

const int SERV_START[numServos]                   // Servo starting position
  = {1300, 1100, 1700};                           // {Outer, Index, Thumb}
const int SERV_END[numServos]                     // Servo full extension position
  = {600, 1600, 1000};                             // {Outer, Index, Thumb}

const int DELAY_MIN = 100;                        // Minimum value for delay between writes to Servo's
const int DELAY_MAX = 10000;                      // Maximum value for delay between writes to Servo's

const int NUM_READS = 200;                        // Number of readings to average from EMS input (granularity)

const int BTOOTH_BAUD = 9600;                     // Bluetooth baud rate
const int maxMessageCount = 20;                   // Maximum character length of received parameter messages to handle errors

const char ACK_CHAR = 'X';                        // Acknowledgement message from phone
const char CLOSE_CHAR = '-';                      // Closed app message
const char START_CHAR = '+';                      // App started message
