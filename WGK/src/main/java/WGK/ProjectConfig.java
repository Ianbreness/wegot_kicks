package WGK;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.util.Locale;

@Configuration
public class ProjectConfig implements WebMvcConfigurer {

    // Lee la ruta de application.properties
    @Value("${wgk.imagenes.ruta}")
    private String rutaImagenes;

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(new Locale("es"));
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Recursos estáticos internos del classpath (CSS, JS, webjars, etc.)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // ── CARPETA EXTERNA DE IMÁGENES ──
        // Las imágenes subidas se sirven en /uploads/**
        // Ejemplo: /uploads/airforce1.jpg → C:/wgk-imagenes/airforce1.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + rutaImagenes);
    }
}