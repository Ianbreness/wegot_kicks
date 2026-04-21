package WGK;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // Carga el usuario con el servicio por defecto
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Google devuelve "name" con el nombre completo y "email" con el correo.
        // Usamos el nombre; si no existe, usamos el email antes del @.
        String nombre = (String) attributes.get("name");
        if (nombre == null || nombre.isBlank()) {
            String email = (String) attributes.get("email");
            nombre = (email != null) ? email.split("@")[0] : "Usuario";
        }

        // Devolvemos el usuario OAuth2 usando "name" como clave de nombre principal
        // en lugar del "sub" (que es el número de ID que mostraba antes)
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "name"   // <-- aquí estaba el problema: Spring usaba "sub" por defecto
        );
    }
}