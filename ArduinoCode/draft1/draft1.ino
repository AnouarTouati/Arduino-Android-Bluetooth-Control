// we have to send data to application  on connection
//we might want to keep the fans and windows open even after the value of sensor is low
//because there might still be gas that is far from the sensor
//so it is up to the user to shut it off when he thinks it is safe
//only when all peripherals (FAN BUZZER AND WINDOWS) are closed by the user that we return to normal operation

#include <Servo.h>

Servo s1 ;
Servo s2 ;
String data;
 const int BuzzerPin=52;
 const int FanPin=22;
bool AlarmMode=false;
bool FansUserWantedStateAfterAlarm=true;
bool BuzzerUserWantedStateAfterAlarm=true;
bool WindowsUserWantedStateAfterAlarm=true;

bool DeactivateFanPermanently=false;
bool DeactivateBuzzerPermanently=false;
bool DeactivateWindowsPermanently=false;

bool BuzzerPreviousState=false;
bool FanPreviousState=false;
int ServoS1PreviousState=0;
int ServoS2PreviousState=90;

int NumberUsedToLimitSendingToOneStringByLoop=99;
int sensorValue=0;
void setup() {
  
Serial.begin(9600);
pinMode(13, OUTPUT); 
pinMode(52, OUTPUT);
pinMode(22, OUTPUT);

s1.attach(A7);
s2.attach(A5);
s1.write(90);
s2.write(0);
}

void loop() {
  delay(200);
  if(NumberUsedToLimitSendingToOneStringByLoop<8)
  {
    NumberUsedToLimitSendingToOneStringByLoop++;
    }else 
    {
       NumberUsedToLimitSendingToOneStringByLoop=0;
      }
   SendAllDataAndCheckState();
   
  if(Serial.available() > 0)      
   {
     
         data=Serial.readString();
         Serial.print("\n");
         Serial.print("           ");
         Serial.print(data);         
         Serial.print("\n");  

       //we dont use switch because it doesnt accept strings
                if(data=="BZOFF")  {BuzzerUserWantedStateAfterAlarm=false;}//BUZZER OFF COMMAND
           else if(data=="FNOFF")    {FansUserWantedStateAfterAlarm=false;}// FAN OFF COMMAND
           else if(data=="WDOFF") {WindowsUserWantedStateAfterAlarm=false;}//WINDOWS OFF COMMAND
           
           else if(data=="BZOFFP")  {DeactivateBuzzerPermanently=true; }//BUZZER OFF COMMAND PERMANETLY
           else if(data=="FNOFFP")    {DeactivateFanPermanently=true; }//FAN OFF COMMAND PERMANETLY
           else if(data=="WDOFFP") {DeactivateWindowsPermanently=true; }//WINDOWS OFF COMMAND PERMANETLY
           
           else if(data=="BZONP")  {DeactivateBuzzerPermanently=false;}//FAN ON COMMAND PERMANETLY
           else if(data=="FNONP")    {DeactivateFanPermanently=false;}//FAN ON COMMAND PERMANETLY
           else if(data=="WDONP") {DeactivateWindowsPermanently=false;}//FAN ON COMMAND PERMANETLY
       
   }
 
 

sensorValue =  analogRead(A0);

 
     if ( sensorValue > 350 && AlarmMode==false) 
     {
              if(!DeactivateBuzzerPermanently)
              {
                digitalWrite (BuzzerPin , HIGH);
                }
               if(!DeactivateFanPermanently)
               {
                 digitalWrite (FanPin , HIGH); 
                }      
                    
                if(!DeactivateWindowsPermanently)
                    {
                          s1.write(0);
                          s2.write(90);
                      }
                         
                    AlarmMode=true;
              

      }     
      if(!digitalRead(BuzzerPin) && !digitalRead(FanPin) && s1.read()==90 && s2.read()==0)
      {
        AlarmMode=false;
       
        
               FansUserWantedStateAfterAlarm=true;
               BuzzerUserWantedStateAfterAlarm=true;
               WindowsUserWantedStateAfterAlarm=true;
        }
        
        if(AlarmMode)
        {
            if(BuzzerUserWantedStateAfterAlarm==false && digitalRead(BuzzerPin))
               {
                    digitalWrite(BuzzerPin,LOW);
               }
        
            if(WindowsUserWantedStateAfterAlarm==false && (s1.read()==0 || s2.read()==90))
               {
                     s1.write(90);
                     s2.write(0);
               }
         
            if(FansUserWantedStateAfterAlarm==false && digitalRead(FanPin))
               {
                     digitalWrite(FanPin,LOW);

               }
          }

}
void SendAllDataAndCheckState()
{
    if(NumberUsedToLimitSendingToOneStringByLoop==0){
  if(digitalRead(BuzzerPin))
  
  {Serial.write("DBZSON");// BUZZER STATE ON
  }else
    {Serial.write("DBZSOF");//BUZZER STATE OFF}
    } else if (NumberUsedToLimitSendingToOneStringByLoop==1){
      if(digitalRead(FanPin))
      {Serial.write("DFNSON");//FAN STATE ON
        }else
        {Serial.write("DFNSOF");//FAN STATE OFF
          }
          
    }else if (NumberUsedToLimitSendingToOneStringByLoop==2){
          if(s1.read()==90 && s2.read()==0)
          {Serial.write("DWDSOF");//WINDOWS STATE OFF
            }else
            {Serial.write("DWDSON");// WINDOWS STATE ON
              }}
    else if (NumberUsedToLimitSendingToOneStringByLoop==3)
    {
      if(AlarmMode==false)
      {
        Serial.write("DARNOF");//ALARMNOTIFICTION OFF
       // Serial.print("\n");
        }else
        {
          Serial.write("DARNON");//ALARMNOTIFICTION On
        //  Serial.print("\n");
          }
      } else if(NumberUsedToLimitSendingToOneStringByLoop==4)
      {
         Serial.write(sensorValue);
       Serial.print(sensorValue);
        }else if(NumberUsedToLimitSendingToOneStringByLoop==5)
      {
        if(DeactivateBuzzerPermanently)
        {
          Serial.write("DBZOFP");
          }else
          {
            Serial.write("DBZONP");
            }
        }else if(NumberUsedToLimitSendingToOneStringByLoop==6)
      {
        if(DeactivateWindowsPermanently)
        {
          Serial.write("DWDOFP");
          }else
          {
            Serial.write("DWDONP");
            }
        }else if(NumberUsedToLimitSendingToOneStringByLoop==7)
      {
        if(DeactivateFanPermanently)
        {
          Serial.write("DFNOFP");
          }else
          {
            Serial.write("DFNONP");
            }
        }
  }
