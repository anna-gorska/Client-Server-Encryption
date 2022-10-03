import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.json.JSONObject;

import RSA.Decrypter;
import RSA.Encrypter;
import RSA.Key_Handler;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.StringBuilder;

/**
 * @author Crunchify.com
 * How to Read JSON Object From File in Java?
 * https://crunchify.com/how-to-read-json-object-from-file-in-java/
 */

//Compile files command: javac -cp "../json-20210307.jar" Client.java Server.java Serialize.java RSA/Decrypter.java RSA/Encrypter.java RSA/Key_Handler.java
//Note make sure that the terminal directory is in the src folder so that the dependancy relative path is correct
//The code used to run in terminal with the dependency: java -cp "../json-20210307.jar"; Client 1 //Here the number represents the number of the config_<number>.json you want to run 

// Client class
class Client {
    private String id;
    private String password;
    private String ip;
    private String port;
    private float delay;
    private ArrayList<String> steps = new ArrayList<>();
    private PublicKey serverKey; 

    private ObjectOutputStream out;
    private Key_Handler key_handler;
    private PublicKey serverPublicKey;

    
    // driver code
    public static void main(String[] args) {


        int configNumber = Integer.parseInt(args[0]);

        StringBuilder builder = new StringBuilder();

        builder.append("../config_");
        builder.append(configNumber);
        builder.append(".json");

        //String file = "project/config_1.json"; //Used when running through the intelliJ client if the source folder
        String file = builder.toString(); //used when running in the terminal


        // establish a connection by providing host and port
        // number
        try (Socket socket = new Socket("localhost", 1234)) {
            //instantiate a client field to be accessible through the entire try loop
       

            
                //once the connection is made we create the client object
                String json_as_string = Client.readFileAsString(file);
//                System.out.println(json_as_string);
            final Client client = new Client(json_as_string);
          
            // writing to server
            client.out = new ObjectOutputStream(socket.getOutputStream());

            // reading from server
            ObjectInputStream in =  new ObjectInputStream(socket.getInputStream());

            //Add method here that instead of looking for the user input automatically loads the client data and sends
            // it one by one to the Server side.

            //Send public key
            client.out.writeObject(client.key_handler.getPublicKey());

            // get public key
            client.serverPublicKey = (PublicKey) in.readObject();

            String credentials = client.retrieveLoginCredentials() + " " + System.currentTimeMillis();
            client.sendEncryptedMessage(credentials);

            String input = (String) in.readObject();
            input = client.decryptServerMessage(input);
            System.out.println(input);
            if(!input.equals("Good")){
                System.exit(0);
            }


            //Sends steps with the specified delay from the config file

            ArrayList<String> steps = client.getSteps();
            int delay = (int) (client.getDelay()*1000);
            int stepCounter = 0;
            while (stepCounter < steps.size()) {
                Timer timer = new Timer();
                int currentStepCounter = stepCounter;
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("here is the command:" + steps.get(currentStepCounter));
                        String mess = steps.get(currentStepCounter) + " " + System.currentTimeMillis();
                        client.sendEncryptedMessage(mess);
                    }
                }, delay);
                stepCounter++;

                //Response from Server
                String input2 = (String) in.readObject();
                System.out.println(client.decryptServerMessage(input2));

            }
            client.sendEncryptedMessage("Done");
            //all commands have now been completed from the config file and we are just waiting for any input or output
            // from the client or the server end.

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private Client(String jsonString) throws Exception {
        JSONObject obj = new JSONObject(jsonString);
        this.id = obj.getString("id");
        this.password = obj.getString("password");
        this.ip = obj.getJSONObject("server").getString("ip");
        this.port = obj.getJSONObject("server").getString("port");
        this.delay = obj.getJSONObject("actions").getFloat("delay");
        for (int i = 0; i < obj.getJSONObject("actions").getJSONArray("steps").length(); i++) {
            this.steps.add(obj.getJSONObject("actions").getJSONArray("steps").getString(i));
        }
//        System.out.println(steps);
            this.key_handler = new Key_Handler("public.key", "private.key");
			
			try {
				this.key_handler.generate_keys();
			} catch (Exception e) {
			
			}
    }
    private void sendPublicKey(String publicKey){
        try {
            out.writeObject(publicKey);
        }catch(Exception e){

        }
    }
    private void sendEncryptedMessage(String output){
        Encrypter encrypter = new Encrypter();
        String encryptedMessage = "haha";
        try{
            byte[] cipher_text_bytes = encrypter.encrypt_message_bytes(output, serverPublicKey);
            encryptedMessage = new String(cipher_text_bytes, "ISO-8859-1");
            out.writeObject(encryptedMessage);
        }
        catch(Exception e){}


    }

    private String decryptServerMessage(String encryptedServerMessage){
        String decryptedMessage = "";
        try {
            byte[] encoded = encryptedServerMessage.getBytes("ISO-8859-1"); 

            Decrypter decrypter = new Decrypter();
            decryptedMessage = decrypter.decrypt(encoded, this.key_handler.getPrivateKey());
        } catch (Exception e) {
            //TODO: handle exception
        }
        return decryptedMessage;
    }

    private String retrieveLoginCredentials() {
        String id = this.getID();
        String password = this.getPassword();

        //creating the id and password in the correct format
        StringBuilder sb = new StringBuilder(100);
        sb.append(id);
        sb.append(" ");
        sb.append(password);

        //returning the built string "<id> <password>" back
        return sb.toString();

    }

    private static String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    private String getID() {
        return this.id;
    }

    private String getPassword() {
        return this.password;
    }

    private float getDelay() {
        return this.delay;
    }

    private ArrayList<String> getSteps() {
        return this.steps;
    }

}
