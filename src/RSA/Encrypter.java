package RSA;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

public class Encrypter {

    //Working With Strings
    String secretMessage = "The secret message";
    //we'll need a Cipher object initialized for encryption with the public key that we generated previously:

    public String encrypt_message_String(String secretMessage, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        //Note that it accepts only byte array arguments, so we need to transform our string before
        byte[] secretMessageBytes = secretMessage.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        //Now, our message is successfully encoded.
        // If we'd like to store it in a database or send it via REST API, it would be more convenient to encode it with the Base64 Alphabet:
        String encodedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes);
        return encodedMessage;
    }

    public byte[] encrypt_message_bytes(String secretMessage, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        //Note that it accepts only byte array arguments, so we need to transform our string before
        byte[] secretMessageBytes = secretMessage.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        //Now, our message is successfully encoded.

        return encryptedMessageBytes;
    }

}
