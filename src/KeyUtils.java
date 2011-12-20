/*
 * RemoteControl - A remote control plugin for CanaryMod
 * Copyright (C) 2011 Willem Mulder (14mRh4X0r)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author 14mRh4X0r
 */
public class KeyUtils {

    private static KeyPair keyPair;
    private static ArrayList<PublicKey> authorizedKeys;

    static void loadOrGenerateKeys() {
        File keyDir = new File(RemoteControl.getInstance().getDirectory(),
                "keys");
        
        try {
            if (!keyDir.exists()) {
                keyDir.mkdirs();
                RemoteControl.log.info("[RemoteControl] Generating keypair...");
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
            }
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
