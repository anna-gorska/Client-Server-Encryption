import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Serialize {

    public static String serialize(PublicKey object) throws IOException {
        ByteArrayOutputStream bOutputStream;
        ObjectOutputStream objectOutputStream;
        bOutputStream = new ByteArrayOutputStream();
        objectOutputStream = new ObjectOutputStream(bOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        byte[] bytes = bOutputStream.toByteArray();
        bOutputStream.close();
        objectOutputStream.close();
        String decoded = new String(bytes, "ISO-8859-1");
        return decoded;
    }
    @SuppressWarnings("unchecked")
    public static PublicKey deserialize(String message) throws ClassCastException,IOException, ReflectiveOperationException {
        byte[] bytes = message.getBytes("ISO-8859-1"); 
        PublicKey obj;
        ByteArrayInputStream bInputStream;
        ObjectInputStream objectInputStream;
        bInputStream = new ByteArrayInputStream(bytes);
        objectInputStream = new ObjectInputStream(bInputStream);
        obj = (PublicKey) objectInputStream.readObject();
        bInputStream.close();
        objectInputStream.close();
        return obj;
    }
}
