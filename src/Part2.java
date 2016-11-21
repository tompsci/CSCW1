import javafx.util.Pair;

import java.math.BigInteger;

/**
 * Created by Thomas on 16/11/2016.
 */
public class Part2 {
    //contains code demonstrating part 2 of the coursework
    //class that acts as a server who distributes public keys on behalf of others
    //A asks S for B's public key
    //S sends A B's public key and a signature
    //A sends B a random value that's encrypted with B's pub key
    //B asks S for A's public key
    //S sends B A's public key and a signature
    //B sends A a random value encrypted with A's public key, as well as the decrypted nonce proving that B decrypted it
    //A sends B the unencrypted nonce to prove it's been decrypted





    public static void main(String[] args) {
        Server server = new Server(512);
        Client a = new Client("Alice", 512);
        Client b = new Client("Bobby", 512);
        server.addUser(a.getName(),a.pubKey());
        server.addUser(b.getName(), b.pubKey());

        //A asks S for B's public key
        //S sends A B's public key and a signature
        //a SIGNS the message, and the server checks the signature is correct before replying
        Pair<Object[], BigInteger[]> bData = server.getUserPKey("Bobby", a.signMessage("Bobby"), a.pubKey());
        //contains b, signed b, and b's public key
        //check that the signature is correct
        if(a.verifySignature((String) bData.getKey()[0], (BigInteger) bData.getKey()[1], server.pubKey())){
            System.out.println("Server signature is correct, proceeding to send b a nonce.");
        }

        Object[] encryptedNonceA = a.generateNonce(bData.getValue());
        //a now sends this to b, who decrypts both the message and the signature, but cannot validate it,
        //b proceeds to ask S for the public key of a
        Pair<Object[], BigInteger[]> aData = server.getUserPKey("Alice", b.signMessage("Alice"), b.pubKey());
        //contains b, signed b, and b's public key
        //check that the signature is correct
        if(b.verifySignature((String) aData.getKey()[0], (BigInteger) aData.getKey()[1], server.pubKey())){
            System.out.println("Server signature is correct, proceeding to validate A's nonce.");
        }

        //now a sends the nonce to b

        //b decrypts the message
        String aNonceValue = b.decrypt((String)encryptedNonceA[0]); //decrypt nonce
        String[] aNonceSignatures = (String[]) encryptedNonceA[1];
        String aNonceSignature = "";
        for(String s : aNonceSignatures){
            aNonceSignature += b.decrypt(s);
        }
        System.out.println( aNonceSignature);
        System.out.println( a.signMessage(aNonceValue));
       // System.out.println("encrypted nonce is: " + encryptedNonceA[0]);
        //System.out.println("encrypted signature is: " + encryptedNonceA[1]);
        //System.out.println("aNonceValue is: "  + aNonceValue);
        //System.out.println("aNonceSignature: " +new String(aNonceSignature));

        //System.out.println(a.signMessage(aNonceValue));


        //b verifies the signature

        if(b.verifySignature(aNonceValue, new BigInteger(aNonceSignature), aData.getValue())){
            System.out.println("A's signature is correct, proceeding to generate a nonce for A");
        }

        //b generates a new nonce and signature

        Object[] encryptedNonceB = b.generateNonce(aData.getValue());
        //b encrypts the nonce, and his decrypted message, and sends them to a with a signature

        //a decrypts the nonce, signature and replied nonce, validates the signature, and then checks the replied nonce is correct
        String bNonceValue = a.decrypt((String)encryptedNonceB[0]); //decrypt nonce
        String[] bNonceSignatures = (String[]) encryptedNonceB[1];
        String bNonceSignature = "";
        for(String s : bNonceSignatures){
            bNonceSignature += a.decrypt(s);
        }

        String encryptedReplyNonceA = b.encrypt(aNonceValue, aData.getValue());


        if(a.verifySignature(bNonceValue, new BigInteger(bNonceSignature), bData.getValue())){
            System.out.println("B's signature is correct, proceeding to check nonce is correct");
        }

        if(a.validateNonce(encryptedReplyNonceA)){
            System.out.println("A's nonce matches, proceedings to send nonce back");
        }
        String encryptedReplyNonceB = a.encrypt(bNonceValue, bData.getValue());
        //if nonce is correct, encrypt nonce reply and nonce reply signature and now b and a can trust one another(?)
        if(b.validateNonce(encryptedReplyNonceB)){
            System.out.println("B's nonce matches, communication is now established.");
        }
        //b sends the old nonce, and the new nonce signed, both encrypted, to a
        //a decrypts the old nonce, checks it is the correct value
        //a decrypts the new nonce, checks the signature
        //a signs and encrypts the new nonce, and sends it to b

        //communication is now established
    }

}
