A simple client-server implementation, where client sends login credentials via a JSON file
The credentials are encrypted using RSA
The credentials are stored only as long as a server is active
Developed to be run in the terminal

To compile in terminal, make sure the working directory is the src folder
javac -cp "../json-20210307.jar" Client.java RSA\Decrypter.java RSA\Encrypter.java RSA\Key_Handler.java Server.java

first run the server
java Server

then run client in another terminal
java -cp "../json-20210307.jar"; Client 1

in order to run a config_<number>.json just replace the 1 in the line above with the number of the config_<number>.json
