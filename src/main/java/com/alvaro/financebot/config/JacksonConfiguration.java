package com.alvaro.financebot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura las herramientas de Jackson usadas para convertir JSON a objetos Java.
 */
@Configuration
public class JacksonConfiguration {

  /**
   * Crea el conversor JSON que necesita {@code TelegramApiClient}.
   *
   * @return conversor JSON compartido por la aplicación
   */
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
