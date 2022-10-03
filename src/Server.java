import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.LinkedList;
import RSA.Decrypter;
import RSA.Encrypter;
import RSA.Key_Handler;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;


//Compile files command: javac -cp "../json-20210307.jar" Client.java Server.java Serialize.java RSA/Decrypter.java RSA/Encrypter.java RSA/Key_Handler.java
//Note make sure that the terminal directory is in the src folder so that the dependancy relative path is correct
//The command used to run the Client in terminal with the dependency: java -cp "../json-20210307.jar"; Client 1 //Here the number represents the number of the config_<number>.json you want to run 
//Command to run the Server: java Server

// Server class
class Server {
    private static byte[] SALT = new byte[16]; // Salt for hashing

    int numberOfClients = 0;

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }


    private Server() {
        new SecureRandom().nextBytes(SALT);
    }

    private void start() {
        ServerSocket server = null;

        try {

            // server is listening on port 1234
            server = new ServerSocket(1234);
            server.setReuseAddress(true);

            // running infinite loop for getting
            // client request
            while (true) {

                // socket object to receive incoming client
                // requests
                Socket client = server.accept();

                // Displaying that new client is connected
                // to server
                numberOfClients++;
                System.out.println("New client connected "
                        + client.getInetAddress()
                        .getHostAddress()+ " Number of clients now connected: "+ numberOfClients);
                
                if(numberOfClients<100){
                                    // create a new thread object
                    ClientHandler clientSock = new ClientHandler(client, this);

                    // This thread will handle the client
                    // separately
                    new Thread(clientSock).start();
                }else if(numberOfClients<=300){
                    System.out.println("we are APPROACHING maximum capacity of our system we need to scale up!");
                }else{ //the maximum capacity of this system is 300
                    System.out.println("we have reached maximum capacity of our system we need to scale up");
                }
 
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    LinkedList<User> users = new LinkedList<User>();

    /**
     * Method to hash password
     * Code implemented according to  https://www.baeldung.com/java-password-hashing
     * @param password String password to hash
     * @return hashed password
     * @throws NoSuchAlgorithmException
     */
    private static String password_hashing(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        String hashed_password = new String(hash);

        return hashed_password;
    }

    /*
    *This method should be used when a client joins the server from a new instance
    if the password is incorrect we should return false and the client handler should return an error and disconnect
    else the user name has not been registered before and we set the username and password and put the number of instances to 1
    else the user name has been registered before and the password is correct so we increase the userId's number of instances by 1
    */
    private boolean registerNewClient(String userId, String password) {
        User tmp;
        for (User user : users) {
            tmp = user;
            if (tmp.getUserId().equals(userId)) {
                if (!tmp.getPassword().equals(password)) {

                    return false;
                } else {
                    tmp.increaseNumberOfInstances();
                    System.out.println("User " + userId + " now has " + tmp.numberOfInstances + " instances connected");
                    return true;
                }
            }
        }
        users.add(new User(userId, password));
        System.out.println("New user instance added " + userId );
        return true;
    }


    /*
    *This method should be called from the "finally" catch statement when a user's instance disconnects
    We should keep track if the user has no more instances left and if so we remove all data related to that instance
    ie: userId & password & counter
    */
    private void clientInstanceDisconnected(String userId) {
        User tmp;
        for (int i = 0; i < users.size(); i++) {
            tmp = users.get(i);
            if (tmp.getUserId().equals(userId)) {
                tmp.decreaseNumberOfInstances();
                System.out.println("User: " + userId + " disconnected an instance and now has "+ tmp.numberOfInstances + " instances connected");
            }
            if (tmp.numberOfInstancesIsZero()) {
                users.remove(i);
                System.out.println("User: " + userId + "'s data has been removed from the system");
            }

        }
    }

    /*
    increment a user's counter by a given amount (positive or negative)
    */
    private void increment(double value, String userId) {
        User tmp;
        for (User user : users) {
            tmp = user;
            if (tmp.getUserId().equals(userId)) {
                tmp.incrementValue(value);
            }
        }
    }

    private double getUserValue(String userId) {
        User tmp;
        for (User user : users) {
            tmp = user;
            if (tmp.getUserId().equals(userId)) {
                return tmp.getValue();
            }
        }
        return 99999;
    }
    // ClientHandler class
    private class ClientHandler implements Runnable {
		private final Socket clientSocket;
        private final Server server;
        private String userId;
        private boolean idSent = false;
		private ObjectOutputStream out;
		private Key_Handler key_handler;
		private PublicKey clientPublicKey;
		private double last_messsage_time = 0;
		

        // Constructor
        private ClientHandler(Socket socket, Server mainServer) {
            this.clientSocket = socket;
            this.server = mainServer;
            this.userId = "";
			this.key_handler = new Key_Handler("public.key", "private.key");
			
			try {
				this.key_handler.generate_keys();
				this.out = new ObjectOutputStream(clientSocket.getOutputStream());
			} catch (Exception e) {
			
			}
        }

        public void run() {
            ObjectInputStream in = null;
            try {
                // get the inputstream of client
                in = new ObjectInputStream(clientSocket.getInputStream());

                // get public key
                clientPublicKey = (PublicKey) in.readObject();

                //send public key
                out.writeObject(key_handler.getPublicKey());

                String line;
                while (true) {
                    line = (String) in.readObject();
                    line = decryptClientMessage(line);
                    if (line.equals("Done")) {
                        break;
                    }
                    if (idSent) {
                        System.out.println(line + ": " + userId);
                    }


                    String output = "Good";

                    String[] splited = line.split(" ");

                    double time = Double.parseDouble(splited[2]);


                    if (time > last_messsage_time) {
                        last_messsage_time = time;
                        if (!(splited.length == 3)) {
                            output = "Invalid input- " + line;
                        } else {
                            if (!idSent) {
                                String password_hashed = password_hashing(splited[1]); // Immediately as password is read, hash it
                                if (!server.registerNewClient(splited[0], password_hashed)) {
                                    output = "Client with ID " + splited[0] + " already registered with a different password";
                                    sendEncryptedMessage(output);
                                    throw new Exception(output);
                                } else {

                                }
                                idSent = true;
                                userId = splited[0];


                            } else {
                                try {

                                    double val = Double.parseDouble(splited[1]);
                                    if (splited[0].equals("INCREASE")) {
                                        server.increment(val, userId);
                                    } else if (splited[0].equals("DECREASE")) {
                                        server.increment(-val, userId);
                                    } else {
                                        output = "Invalid input";
                                    }
                                } catch (Exception e) {
                                    output = "Invalid input";
                                }


                            }
                        }
                        System.out.println("The value of account " + userId + " is now " + server.getUserValue(userId));
                        sendEncryptedMessage(output);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                server.clientInstanceDisconnected(userId);
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

		private void sendEncryptedMessage(String output){
			Encrypter encrypter = new Encrypter();
			String encryptedMessage = "haha";
			try{
			    byte[] cipher_text_bytes = encrypter.encrypt_message_bytes(output, clientPublicKey);
			    encryptedMessage = new String(cipher_text_bytes, "ISO-8859-1");
                //System.out.println(output);
                out.writeObject(encryptedMessage);
			}
			catch(Exception e){}
			// out.flush();
		}
		private String decryptClientMessage(String encryptedServerMessage){
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
    }


    private class User {

        private int numberOfInstances = 1;
        private String userId;
        private String password;
        private double value = 0;

        private User(String userId, String password) {
            this.userId = userId;
            this.password = password;
        }

        private boolean numberOfInstancesIsZero() {
            return (numberOfInstances == 0);
        }

        private void increaseNumberOfInstances() {
            numberOfInstances++;
        }

        private void decreaseNumberOfInstances() {
            numberOfInstances--;
        }

        private String getUserId() {
            return userId;
        }

        private String getPassword() {
            return password;
        }

        private void incrementValue(double incrementAmount) {
            value += incrementAmount;
        }
        private double getValue() {
            return value;
        }
    }

}
