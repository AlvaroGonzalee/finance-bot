package com.alvaro.financebot.dto.telegram;

/**
 * Datos de un mensaje recibido desde Telegram que necesitamos para procesar un comando.
 *
 * @param updateId identificador único de la actualización enviada por Telegram
 * @param telegramUserId identificador del usuario que ha enviado el mensaje
 * @param chatId identificador del chat al que se debe responder
 * @param text contenido del mensaje enviado por el usuario
 */
public record TelegramUpdateDTO(
    Long updateId,
    Long telegramUserId,
    Long chatId,
    String text
) {
}
