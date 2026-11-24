package br.com.ekan.beneficiarios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Beneficiarios - Ekan")
                        .version("v1")
                        .description("API REST para gestao de beneficiarios de plano de saude e seus documentos")
                        .contact(new Contact()
                                .name("Everton")
                                .url("https://github.com/Irinius777")));
    }
}
