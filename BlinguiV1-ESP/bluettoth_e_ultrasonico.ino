    #include "Servo.h"
    Servo motor;
    const int buzzer = 6;
    
    char incomingByte;  // incoming data
    const int  LED = 13;      // LED pin
    const int LED2 = 8;
    //distancia
    #define echoPin A0
    #define trigPin A1
    
    void setup() {
     motor.attach(3);
    
      Serial.begin(9600); // initialization
      pinMode(LED, OUTPUT);
        pinMode(LED2, OUTPUT);
    
      //sensor de distancia
      pinMode(echoPin,INPUT);
      pinMode(trigPin,OUTPUT);
      pinMode(buzzer, OUTPUT);
    
      
      Serial.println("Press 1 to LED ON or 0 to LED OFF...");
      
    }
    
    void loop() {
      int i;
      for(i=20; i<100; i++) {
          
      digitalWrite(trigPin, LOW);
      delayMicroseconds(1);
      digitalWrite(trigPin, HIGH);
      delayMicroseconds(1);
      digitalWrite(trigPin, LOW);
      int duration = pulseIn(echoPin, HIGH);
      int distancia = duration /29 / 2 ;
      
          if (i>=20 && i <=55){                 
              if (distancia <=50){
                    Serial.println('1');
                    delay(50);
                    digitalWrite(LED, LOW);
                    digitalWrite(LED2, HIGH);
                    tone(buzzer,500);   
                    delay(20);
                    noTone(buzzer);
                
                if( distancia >20 && distancia<35 || distancia >45 && distancia<=50)
                  Serial.println('3');
                    delay(50);
                    digitalWrite(LED, LOW);
                    digitalWrite(LED2, HIGH);
                    tone(buzzer,500);   
                    delay(10);
                    noTone(buzzer);
                }
    
               else if (distancia >50){
                    Serial.println('4');
                    digitalWrite(LED, HIGH);
                    digitalWrite(LED2, LOW);
                    noTone(buzzer);}
               }
           
      
          if (i>=55 && i <=70){                 
              if (distancia <=100){
                    Serial.println('1');
                    delay(50);
                    digitalWrite(LED, LOW);
                    digitalWrite(LED2, HIGH);
                    tone(buzzer,500);   
                    delay(20);
                    noTone(buzzer);
                
                    if(distancia < 10 || distancia >25 && distancia<35 || distancia >50 && distancia<60 ||  distancia >80 && distancia<90 || distancia<=100){
                  Serial.println('3');
                    delay(50);
                    digitalWrite(LED, LOW);
                    digitalWrite(LED2, HIGH);
                    tone(buzzer,500);   
                    delay(10);
                    noTone(buzzer);
                }
    
               else if (distancia >100){
                    Serial.println('4');
                    digitalWrite(LED, HIGH);
                    digitalWrite(LED2, LOW);
                    noTone(buzzer);}
               }
           
      
          }
      
      
          if (i >70 && i<=100){
                if (distancia <=100){			
                     Serial.println('2');
                     delay(100);
                     digitalWrite(LED, LOW);
                     digitalWrite(LED2, HIGH);
                     tone(buzzer,500);   
                     delay(30);
                     noTone(buzzer);
                 
                    if(distancia < 10 || distancia >25 && distancia<35 || distancia >50 && distancia<60 ||  distancia >80 && distancia<90 || distancia<=100){
                     Serial.println('3');
                     delay(100);
                     digitalWrite(LED, LOW);
                     digitalWrite(LED2, HIGH);
                     tone(buzzer,500);   
                     delay(30);
                     noTone(buzzer);
                    }
                      
  
                 }
    
                   else if (distancia >90){
                      Serial.println('4');
                     digitalWrite(LED, HIGH);
                     digitalWrite(LED2, LOW);
                     noTone(buzzer);}
                   }
           
            motor.write(i);
            delay(50);
     }
                   delay(10);
 
    }
