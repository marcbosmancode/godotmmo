package godotmmoserver.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author marcb
 */
public class PasswordEncrypter {
    public byte[] generateSalt(){
        //use SecureRandom instead of Random
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            //generate a 8 byte salt
            byte[] salt = new byte[8];
            random.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error generating the salt");
            return null;
        }
    }
    
    public byte[] encryptPassword(String password, byte[] salt){
        try {
            //1000 iterations, 160 derivedKeyLength
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, 160);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            //encrypt and return the password
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error encrypting the password");
            return null;
        }
    }
    
    public boolean checkPassword(String password, byte[] encryptedPassword, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException{
        //encrypt the given password with the same salt used for the original password
        byte[] encryptedGivenPassword = encryptPassword(password, salt);
        //return if the given password is correct
        return Arrays.equals(encryptedGivenPassword, encryptedPassword);
    }
}
