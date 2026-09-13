package com.banking.transaction.util;

import com.banking.transaction.dto.TransferRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class KeyGeneratorUtil {

    public static String generateTransferKey(TransferRequest req) {
        // Group by 1-minute time window to catch accidental multi-clicks
        long minuteBucket = Instant.now().getEpochSecond() / 60;

        String rawString = req.getFromAccountId() + ":"
                + req.getToAccountId() + ":"
                + req.getAmount() + ":"
                + minuteBucket;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.substring(0, 16); // Short hash
        } catch (NoSuchAlgorithmException e) {
            return req.getFromAccountId() + "-" + minuteBucket;
        }
    }
}