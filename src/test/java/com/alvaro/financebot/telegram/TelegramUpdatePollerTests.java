package com.alvaro.financebot.telegram;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alvaro.financebot.dto.telegram.TelegramUpdateDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TelegramUpdatePollerTests {

  @Mock
  private TelegramBotClient telegramBotClient;

  @Mock
  private TelegramCommandHandler telegramCommandHandler;

  @InjectMocks
  private TelegramUpdatePoller telegramUpdatePoller;

  @Test
  void processesAnUpdateAndReturnsTheNextOffset() {
    TelegramUpdateDTO update = new TelegramUpdateDTO(10L, 100L, 200L, "/HELP");
    when(telegramBotClient.getUpdates(0L)).thenReturn(List.of(update));
    when(telegramCommandHandler.handle(100L, "/HELP")).thenReturn("COMANDOS DISPONIBLES");

    Long nextOffset = telegramUpdatePoller.pollOnce(0L);

    assertThat(nextOffset).isEqualTo(11L);
    verify(telegramCommandHandler).handle(100L, "/HELP");
    verify(telegramBotClient).sendMessage(200L, "COMANDOS DISPONIBLES");
  }
}
