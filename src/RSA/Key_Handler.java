package RSA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Key_Handler {

    String path_public;
    String path_private;
    private PublicKey publicKey;
    private PrivateKey privateKey;


    public Key_Handler(String path_public, String path_private){
        this.path_public = path_public;
        this.path_private = path_private;
    }

    public PublicKey getPublicKey(){
        return this.publicKey;
    }
    public PrivateKey getPrivateKey(){
        return this.privateKey;
    }

    public KeyPair generate_keys() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
        return pair;
}

    public PublicKey read_public_key(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        //To read the key from a file, we'll first need to load the content as a byte array:
        File publicKeyFile = new File(path);
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
        //use the KeyFactory to recreate the actual instance:
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
       // EncodedKeySpec publicKeySpec = new PKCS8EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(publicKeySpec);
    }


    public PrivateKey read_private_key(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        //To read the key from a file, we'll first need to load the content as a byte array:
        File privateKeyFile = new File(path);
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
        //use the KeyFactory to recreate the actual instance:
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        return keyFactory.generatePrivate(privateKeySpec);
    }




}
