package com.alvaro.financebot.telegram;

import com.alvaro.financebot.dto.telegram.TelegramUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Recibe mensajes nuevos mediante long polling y coordina su procesamiento.
 *
 * <p>Solicita actualizaciones a {@link TelegramBotClient}, delega el texto del usuario en
 * {@link TelegramCommandHandler} y envía la respuesta al chat correspondiente.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramUpdatePoller {

  private final TelegramBotClient telegramBotClient;
  private final TelegramCommandHandler telegramCommandHandler;

  /**
   * Inicia el long polling cuando Spring Boot ha terminado de arrancar.
   */
  @EventListener(ApplicationReadyEvent.class)
  void start() {
    Thread.ofPlatform().name("telegram-update-poller").start(this::pollContinuously);
  }

  private void pollContinuously() {
    Long offset = 0L;

    while (!Thread.currentThread().isInterrupted()) {
      try {
        offset = pollOnce(offset);
      } catch (Exception exception) {
        log.error("NO SE HAN PODIDO PROCESAR LOS MENSAJES DE TELEGRAM.", exception);
        pauseBeforeRetry();
      }
    }
  }

  /**
   * Procesa una petición de actualizaciones y devuelve el siguiente offset.
   *
   * <p>El offset evita procesar el mismo mensaje varias veces.</p>
   *
   * @param offset identificador desde el que se piden mensajes a Telegram
   * @return identificador que se usará en la siguiente petición
   */
  Long pollOnce(Long offset) {
    Long nextOffset = offset;

    for (TelegramUpdateDTO update : telegramBotClient.getUpdates(offset)) {
      if (update == null || update.updateId() == null) {
        continue;
      }

      processUpdate(update);
      nextOffset = update.updateId() + 1;
    }

    return nextOffset;
  }

  private void processUpdate(TelegramUpdateDTO update) {
    if (update.telegramUserId() == null || update.chatId() == null || update.text() == null) {
      return;
    }

    String response = telegramCommandHandler.handle(update.telegramUserId(), update.text());
    telegramBotClient.sendMessage(update.chatId(), response);
  }

  private void pauseBeforeRetry() {
    try {
      Thread.sleep(2_000);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }
}
