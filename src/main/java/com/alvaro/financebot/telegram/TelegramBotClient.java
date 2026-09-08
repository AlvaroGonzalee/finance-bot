package com.alvaro.financebot.telegram;

import com.alvaro.financebot.dto.telegram.TelegramUpdateDTO;
import java.util.List;

/**
 * Define las acciones que nuestra aplicación puede realizar con la API de Telegram.
 */
public interface TelegramBotClient {

  /**
   * Obtiene los mensajes nuevos que Telegram tiene pendientes para el bot.
   *
   * @param offset identificador desde el que se deben solicitar las actualizaciones
   * @return actualizaciones recibidas desde Telegram
   */
  List<TelegramUpdateDTO> getUpdates(Long offset);

  /**
   * Envía un mensaje de texto a un chat de Telegram.
   *
   * @param chatId identificador del chat que recibirá el mensaje
   * @param text mensaje que se enviará
   */
  void sendMessage(Long chatId, String text);
}
