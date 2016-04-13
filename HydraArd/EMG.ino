// readMuscle: gets EMG sensor reading
long readMuscle(){
  long sensorVal = getAvgReading(NUM_READS);
  long cutoffVal = constrain(sensorVal, ARM_LOW, ARM_HIGH);
  return cutoffVal;
}

// mapReading(int): map EMG reading to respective Servo i
long mapReading(long cutoffVal, int i){
  long mappedVal = map(cutoffVal, ARM_LOW, ARM_HIGH, SERV_START[i], SERV_END[i]);
  return mappedVal;
}

// average input EMS readings
long getAvgReading(int n){
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

// For calibration, return highest of some # of readings
long getMaxReading(int n){
  long max = analogRead(EMS_PIN);
  int i = 0;
  while (i < n){
    long currentReading = analogRead(EMS_PIN);
    if (currentReading > max){
      max = currentReading;
    }
    i++;
  }
  return max;
}

// For calibration, return lowest of some # of readings
long getMinReading(int n){
  long min = analogRead(EMS_PIN);
  int i = 0;
  while (i < n){
    long currentReading = analogRead(EMS_PIN);
    if (currentReading < min){
      min = currentReading;
    }
    i++;
  }
  return min;
}
