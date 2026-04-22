package WGK;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WgkApplication {

    public static void main(String[] args) {

        // Aumenta el límite de partes multipart de Tomcat 11.
        // Tomcat 11 redujo el default a 1 parte, lo que causa
        // FileCountLimitExceededException al subir formularios con imágenes.
        // Este system property debe setearse ANTES de que Spring arranque.
        System.setProperty(
                "org.apache.tomcat.util.http.fileupload.FileUploadBase.ATTACHMENT_MAX_COUNT",
                "50"
        );

        SpringApplication.run(WgkApplication.class, args);
    }
}
