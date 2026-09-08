package com.alvaro.financebot.telegram;

import com.alvaro.financebot.dto.telegram.TelegramUpdateDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Implementación HTTP de {@link TelegramBotClient} que se comunica con la API oficial de Telegram.
 */
@Component
@RequiredArgsConstructor
public class TelegramApiClient implements TelegramBotClient {

  private static final int POLLING_TIMEOUT_SECONDS = 50;

  private final ObjectMapper objectMapper;
  private final RestClient restClient = RestClient.create();

  @Value("${telegram.bot.token}")
  private String botToken;

  @Override
  public List<TelegramUpdateDTO> getUpdates(Long offset) {
    String responseBody = restClient.get()
        .uri("%s/getUpdates?offset=%d&timeout=%d&allowed_updates=%%5B%%22message%%22%%5D".formatted(
            apiUrl(), offset, POLLING_TIMEOUT_SECONDS))
        .retrieve()
        .body(String.class);

    return parseUpdates(responseBody);
  }

  @Override
  public void sendMessage(Long chatId, String text) {
    restClient.post()
        .uri(apiUrl() + "/sendMessage")
        .body(Map.of("chat_id", chatId, "text", text))
        .retrieve()
        .toBodilessEntity();
  }

  private List<TelegramUpdateDTO> parseUpdates(String responseBody) {
    try {
      JsonNode response = objectMapper.readTree(responseBody);
      if (!response.path("ok").asBoolean()) {
        throw new IllegalStateException("TELEGRAM HA DEVUELTO UN ERROR.");
      }

      List<TelegramUpdateDTO> updates = new ArrayList<>();
      for (JsonNode update : response.path("result")) {
        JsonNode message = update.path("message");
        updates.add(new TelegramUpdateDTO(
            update.path("update_id").asLong(),
            message.path("from").path("id").asLong(),
            message.path("chat").path("id").asLong(),
            message.path("text").asText()
        ));
      }
      return updates;
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("NO SE HA PODIDO LEER LA RESPUESTA DE TELEGRAM.", exception);
    }
  }

  private String apiUrl() {
    return "https://api.telegram.org/bot" + botToken;
  }
}
