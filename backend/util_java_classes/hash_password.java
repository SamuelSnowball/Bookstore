import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class hash_password {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("DemoUserPassword!$$");
        System.out.println(hash);
    }
}
