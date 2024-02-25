package chord;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.Math.pow;

class Util {

    static boolean inRange_OpenOpen(int id, int start, int end) {
        return inRange_CloseOpen(id, start, end) && id != start;
    }

    static boolean inRange_CloseOpen(int id, int start, int end) {
        // id ∈ [start, end) % 2^m
        if(start > end) {
            return !inRange_CloseOpen(id, end, start);
        }
        return id >= start && id < end;
    }

    static boolean inRange_OpenClose(int id, int start, int end) {
        // id ∈ (start, end] % 2^m
        if(start > end) {
            return !inRange_OpenClose(id, end, start);
        }
        return id > start && id <= end;
    }

    public static int calculateSHA1(String input, int m) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
//            logger.warn("Failed to calculate SHA-1 for input %s", input);
            e.printStackTrace(System.err);
            return 0;
        }

        return new BigInteger(1,
                md.digest(input.getBytes(StandardCharsets.UTF_8)))
                .mod(BigInteger.valueOf((long)pow(2,m)-1))
                .intValue();
    }

}
