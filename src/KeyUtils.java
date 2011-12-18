
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author willem
 */
public class KeyUtils {

    private static KeyPair keyPair;
    private static ArrayList<PublicKey> authorizedKeys;

    static void loadOrGenerateKeys() {
        File keyDir = new File(RemoteControl.getInstance().getDirectory(),
                "keys");

        if (!keyDir.exists()) {
            try {
                keyDir.mkdirs();
                RemoteControl.log.info("[RemoteControl] Generating keypair.");
                KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
                RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048,
                        RSAKeyGenParameterSpec.F4);
                keygen.initialize(spec);
                KeyPair newPair = keygen.generateKeyPair();
                
                PublicKey publicKey = newPair.getPublic();
                PrivateKey privateKey = newPair.getPrivate();
                
                // Store the public key.
                X509EncodedKeySpec publicSpec =
                        new X509EncodedKeySpec(publicKey.getEncoded());
                FileOutputStream out =
                        new FileOutputStream(new File(keyDir, "public.key"));
                out.write(DatatypeConverter.printBase64Binary(
                        publicSpec.getEncoded()).getBytes());
                out.close();

                // Store the private key.
                PKCS8EncodedKeySpec privateSpec =
                        new PKCS8EncodedKeySpec(privateKey.getEncoded());
                out = new FileOutputStream(new File(keyDir, "private.key"));
                out.write(DatatypeConverter.printBase64Binary(
                        privateSpec.getEncoded()).getBytes());
                out.close();
            } catch (InvalidAlgorithmParameterException ex) {
                RemoteControl.log.log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                RemoteControl.log.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                RemoteControl.log.log(Level.SEVERE, null, ex);
            }
        }
        try {
            // Following code was borrowed from Vex Software LLC
            // Read the public key file.
            File publicKeyFile = new File(keyDir, "id_rsa.pub");
            FileInputStream in = new FileInputStream(publicKeyFile);
            byte[] encodedPublicKey =
                    new byte[(int) publicKeyFile.length()];
            in.read(encodedPublicKey);
            encodedPublicKey = DatatypeConverter.parseBase64Binary(
                    new String(encodedPublicKey));
            in.close();

            // Read the private key file.
            File privateKeyFile = new File(keyDir, "id_rsa");
            in = new FileInputStream(privateKeyFile);
            byte[] encodedPrivateKey =
                    new byte[(int) privateKeyFile.length()];
            in.read(encodedPrivateKey);
            encodedPrivateKey = DatatypeConverter.parseBase64Binary(
                    new String(encodedPrivateKey));
            in.close();

            // Instantiate and set the key pair.
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec =
                    new X509EncodedKeySpec(encodedPublicKey);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            PKCS8EncodedKeySpec privateKeySpec =
                    new PKCS8EncodedKeySpec(encodedPrivateKey);
            PrivateKey privateKey =
                    keyFactory.generatePrivate(privateKeySpec);
            keyPair = new KeyPair(publicKey, privateKey);

            // Read the authorized keys file.
            File authorizedKeysFile = new File(keyDir, "authorized_keys");
            Scanner scan = new Scanner(authorizedKeysFile);
            while (scan.hasNextLine()) {
                encodedPublicKey = DatatypeConverter.parseBase64Binary(
                        scan.nextLine());
                publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
                publicKey = keyFactory.generatePublic(publicKeySpec);
                authorizedKeys.add(publicKey);
            }
            scan.close();
        } catch (Exception e) {
            RemoteControl.log.log(Level.SEVERE, "Error while loading keys", e);
        }

    }

    public static ArrayList<PublicKey> getAuthorizedKeys() {
        return new ArrayList<PublicKey>(authorizedKeys);
    }

    public static KeyPair getKeyPair() {
        return keyPair;
    }
    
}
