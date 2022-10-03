package RSA;
//https://www.baeldung.com/java-rsa
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Trying {

    public static void main(String[] args) throws Exception {

    Key_Handler key_handler = new Key_Handler("public.key", "private.key");

    key_handler.generate_keys();
    PublicKey publicKey = key_handler.read_public_key(key_handler.path_public);
    Encrypter encrypter = new Encrypter();
    String plain_text = "We are going to ace this project guys!";
    byte[] cipher_text_bytes = encrypter.encrypt_message_bytes(plain_text, publicKey);

    String decoded = new String(cipher_text_bytes, "ISO-8859-1");

    byte[] encoded = decoded.getBytes("ISO-8859-1"); 

    PrivateKey privateKey = key_handler.read_private_key(key_handler.path_private);
    Decrypter decrypter = new Decrypter();
    String decrypted_cipher_text = decrypter.decrypt(encoded, privateKey);

    System.out.println(decrypted_cipher_text);

    }
}
