package WGK;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 👇 imports nuevos
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;

@SpringBootApplication
public class WgkApplication {

    public static void main(String[] args) {
        SpringApplication.run(WgkApplication.class, args);
    }

    // 👇 AQUÍ MISMO
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addContextCustomizers(context -> {
                context.setAllowCasualMultipartParsing(true);
                context.addParameter("maxFileCount", "20");
            });
        };
    }
}