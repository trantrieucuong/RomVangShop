package vn.fs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image_uploads/**")
                .addResourceLocations("file:image_uploads/");

        registry.addResourceHandler("/chat/**")
                .addResourceLocations("classpath:/static/chat/");
        // Đường dẫn tới thư mục thực tế

    }
}
