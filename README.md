This project is meant  control and monitor arduino based Gas Detection System.

how ever you can easily adapt the project to your needs by:
Android Application Side
        -modifying SetTheTexts() method in Control.java //which controls what happens with data receivd in the app from arduino
        -using the SendData() method in Control.java //which sends strings to arduino (commands) 
                                                    //also MAKE SURE that the strings are EXACTLY 5 charachters long
Arduino Side:
         -make sure you have atleast a delay of 200ms in the loop //delay(200);
         -and the strings you send out with //Serial.write are EXACTLY 5 charachters long
         -also you should only send one string at a time in the loop cycle


