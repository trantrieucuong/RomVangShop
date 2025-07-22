package vn.fs.bank;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.payos.PayOS;

@SpringBootApplication
@Configuration
public class PayOSConfig implements WebMvcConfigurer {
    @Value("ef457fa9-fc46-4d84-b19d-3f76ac707c32")
    private String clientId;

    @Value("8624f0c7-34c2-45bd-b33c-fcb0eb58714a")
    private String apiKey;

    @Value("42e3af69fde2c3bc8bd0baa580a0aaf215ce0d65691d3679731ca05a3e8525e9")
    private String checksumKey;

    @Bean
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}
