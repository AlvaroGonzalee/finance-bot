# Finance Bot

A personal-finance Telegram bot built with Java 21 and Spring Boot. Record income and expenses in a Telegram chat, then consult the current month, your historical balances, and accumulated savings.

The bot uses **long polling**: it can run locally during development or on a VPS later, without needing a public domain or a webhook.

## Features

- Register income and expenses from Telegram.
- Persist every operation in PostgreSQL.
- Show the operations, income, expenses, and balance for the current month.
- Show the balance of every month and accumulated savings.
- Delete the most recently registered operation for the current Telegram user.
- Validate the amount, concept, and command structure before writing to the database.
- Keep each user's operations isolated through their Telegram user ID.
- Use unit tests with Mockito; the Spring Boot context is not started during tests.

## Commands

Commands and concepts are converted to uppercase before being processed and saved.

| Command | What it does | Example |
| --- | --- | --- |
| `/AYUDA` or `/HELP` | Shows all available commands. | `/AYUDA` |
| `/GASTO` or `/EXPENSE` | Registers an expense. | `/GASTO 25 CENAAMIGOS` |
| `/INGRESO` or `/INCOME` | Registers an income. | `/INGRESO 1600 SUELDO` |
| `/MENSUAL` or `/MONTHLY` | Shows this month's operations and totals. | `/MENSUAL` |
| `/HISTORICO` or `/HISTORY` | Shows monthly balances and accumulated savings. | `/HISTORICO` |
| `/ELIMINAR` or `/DELETE` | Deletes your latest registered operation. | `/ELIMINAR` |

### Operation format

Use the following structure to register an operation:

```text
/COMMAND AMOUNT CONCEPT
```

Examples:

```text
/GASTO 24,50 CENAAMIGOS
/EXPENSE 100 AGUA
/INGRESO 1600 SUELDO
```

The amount must be greater than zero and can have up to two decimal places. Both `,` and `.` are accepted as decimal separators.

### Deleting an operation

`/ELIMINAR` deletes only the latest operation created by **your own Telegram user**. For example:

```text
/GASTO 100 AGUA
/ELIMINAR
```

The second command removes the `AGUA` expense. It cannot delete another user's data.

## Technology

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Telegram Bot API
- Lombok
- JUnit 5, AssertJ, and Mockito
- Maven Wrapper

## Project structure

```text
src/main/java/com/alvaro/financebot
├── config       # Spring configuration
├── dto          # Data Transfer Objects exchanged between layers
├── entity       # JPA entities mapped to database tables
├── repository   # Database access through Spring Data JPA
├── service      # Financial business logic
└── telegram     # Telegram API client, polling, and commands
```

The flow of a Telegram message is:

```text
Telegram -> TelegramUpdatePoller -> TelegramCommandHandler -> OperationService -> Repository -> PostgreSQL
```

## Requirements

- Java 21
- PostgreSQL running locally or on a reachable server
- A Telegram bot token created with [@BotFather](https://t.me/BotFather)

## Configuration

The application reads secrets and database connection details from environment variables:

```text
DB_URL=jdbc:postgresql://localhost:5432/finance_bot
DB_USERNAME=YOUR_POSTGRES_USER
DB_PASSWORD=YOUR_POSTGRES_PASSWORD
TELEGRAM_BOT_TOKEN=YOUR_TELEGRAM_BOT_TOKEN
```

Do not commit `TELEGRAM_BOT_TOKEN` or database passwords to Git.

For a local terminal session, set them before starting the application:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/finance_bot"
export DB_USERNAME="financebot"
export DB_PASSWORD="YOUR_POSTGRES_PASSWORD"
export TELEGRAM_BOT_TOKEN="YOUR_TELEGRAM_BOT_TOKEN"
```

## Run locally

1. Create a PostgreSQL database called `finance_bot`.
2. Set the four environment variables above.
3. Start the bot:

   ```bash
   ./mvnw spring-boot:run
   ```

At startup, `schema.sql` creates the `financial_operation` table and its index when they do not already exist. Spring validates that the JPA entity matches the database schema, then the bot starts polling Telegram for new messages.

Keep the application running while you use the bot: long polling only receives messages while this process is active.