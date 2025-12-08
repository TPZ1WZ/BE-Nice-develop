package com.proj.webprojrct;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptHashTest {
    
    @Test
    public void testPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "password";
        
        // Hash hiện tại trong database
        String currentHash = "$2a$12$T3ztDZ4qOq39rCbtwiY7CeJu2sp2wU6yyLPalK4VgxEPhiv1PM7Vq";
        
        System.out.println("=".repeat(60));
        System.out.println("TESTING BCRYPT PASSWORD HASH");
        System.out.println("=".repeat(60));
        System.out.println("Password to test: '" + password + "'");
        System.out.println("Current DB hash:  " + currentHash);
        System.out.println();
        System.out.println("Does 'password' match current hash? " + encoder.matches(password, currentHash));
        System.out.println();
        
        // Generate new correct hash
        String newHash = encoder.encode(password);
        System.out.println("NEW CORRECT HASH FOR 'password':");
        System.out.println(newHash);
        System.out.println();
        System.out.println("Verification - Does 'password' match new hash? " + encoder.matches(password, newHash));
        System.out.println("=".repeat(60));
    }
}
