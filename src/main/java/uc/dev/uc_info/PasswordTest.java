package uc.dev.uc_info;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String encodedPassword = encoder.encode("1234");

        System.out.println("BCrypt password:");
        System.out.println(encodedPassword);
    }
}