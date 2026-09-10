package com.alvaro.financebot.telegram;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HelpCommandHandlerTests {

  private final HelpCommandHandler helpCommandHandler = new HelpCommandHandler();

  @Test
  void returnsAllAvailableCommands() {
    String response = helpCommandHandler.handle();

    assertThat(response)
        .contains("/GASTO /EXPENSE")
        .contains("/INGRESO /INCOME")
        .contains("/MENSUAL /MONTHLY")
        .contains("/HISTORICO /HISTORY")
        .contains("/ELIMINAR /DELETE")
        .contains("/AYUDA /HELP");
  }
}
