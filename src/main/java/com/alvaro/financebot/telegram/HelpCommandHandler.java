package com.alvaro.financebot.telegram;

import org.springframework.stereotype.Component;

/**
 * Devuelve la guía de comandos disponibles para el usuario de Telegram.
 */
@Component
public class HelpCommandHandler {

  private static final String HELP_MESSAGE = """
      COMANDOS DISPONIBLES:

      /GASTO /EXPENSE - AÑADE UN NUEVO GASTO. EJ: /GASTO CENAAMIGOS 25
      /INGRESO /INCOME - AÑADE UN NUEVO INGRESO. EJ: /INGRESO SUELDO 1600
      /MENSUAL /MONTHLY - MUESTRA TODAS LAS OPERACIONES DEL MES ACTUAL Y SUS TOTALES.
      /HISTORICO /HISTORY - MUESTRA EL RESULTADO FINAL DE CADA MES Y EL AHORRO ACUMULADO.
      /ELIMINAR /DELETE - ELIMINA UNA OPERACIÓN MEDIANTE SU ID. EJ: /ELIMINAR 34
      /AYUDA /HELP - MUESTRA LOS COMANDOS DISPONIBLES Y CÓMO UTILIZARLOS.
      """;

  public String handle() {
    return HELP_MESSAGE;
  }
}
