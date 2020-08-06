# bluetoothtester

Simple Bluetooth Tester App

Used to test Prototype Devices like Arduino shields HC05/06.

Through using this App you can 

- Show Bonded Devices
- Scan for new Devices
- Connect to new Devices
- Send and Receive Data

To be noted:

- An utility class that encapsulate the bluetoothsocket handling was implemented. Can be reused.
- All the code was based on guides from https://developer.android.com/guide/topics/connectivity/bluetooth


### Example Arduino Sketch to test HC06

    #include <Arduino.h>
    #include <SoftwareSerial.h>

    SoftwareSerial hc06(10,11);

    void setup() {
      Serial.begin(9600);
      hc06.begin(9600);
      Serial.println("Welcome to HC06 Tester");

      hc06.print("AT+VERSION");
    }


    #define LF 0x0A
    #define CR 0x0D

    char buffer[80];
    unsigned int buffer_count = 0;
    void loop() {

      if (hc06.available()) {
        char c = hc06.read();
        Serial.write(c);
      }
  
 
      if (Serial.available())
       {
        char c = Serial.read();
    
        if (((c == CR) || (buffer_count >= sizeof(buffer))) && (buffer_count > 0)) {
          if (strncmp(buffer, "AT", 2) != 0) {
            Serial.print("\nSending...");
            buffer[buffer_count++] = '\n';
          } 
          hc06.write(buffer, buffer_count);
          memset(buffer, 0, sizeof(buffer)-1);
          buffer_count = 0;
        } else if (c != LF && c != CR) {
          buffer[buffer_count++] = c;
        }
        Serial.write(c); 
      }    
 
    }

### TODO

- Try to encapsulate Bluetooth Scan Discovery 
- Use coroutines instead of Threads
- Hexdump mode for reading
- Better UI
