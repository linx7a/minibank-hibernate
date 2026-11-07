# MiniBank (Spring Core + Hibernate)

Консольное учебное банковское приложение на Java, Spring Core и Hibernate.
Данные хранятся в PostgreSQL и сохраняются между перезапусками.
## Что умеет
- создавать пользователей;
- показывать всех пользователей и их счета;
- создавать дополнительные счета;
- пополнять и снимать деньги;
- переводить между счетами (с комиссией для разных пользователей);
- закрывать счет с переносом остатка;
- завершать работу по команде `EXIT`.

## Технологии
- Java 21
- Spring Core (`spring-context`)
- Hibernate Core 
- PostgreSQL


## Архитектура
- `User`, `Account` — Hibernate-сущности.
- `UserService`, `AccountService` — бизнес-логика, работа с БД через `Session`.
- `TransactionHelper` — транзакционная обвязка с поддержкой nested-вызовов.
- `HibernateConfiguration` — создание `SessionFactory`.
- `OperationCommand` + `ConsoleOperationType` — обработка команд (Command pattern).
- `OperationsConsoleListener` — главный цикл приложения.
- `ConsoleInput` — единая точка чтения и валидации консольного ввода.

## Команды
- `USER_CREATE`
- `SHOW_ALL_USERS`
- `ACCOUNT_CREATE`
- `ACCOUNT_DEPOSIT`
- `ACCOUNT_WITHDRAW`
- `ACCOUNT_TRANSFER`
- `ACCOUNT_CLOSE`
- `EXIT`

## Запуск
1. Запустить PostgreSQL. Параметры подключения (адрес, порт, база, пользователь, пароль) указаны в `src/main/resources/application.properties`; при необходимости измените их под свою базу.
2. Запустить приложение из IDE (класс `sorokin.java.course.Main`) или через Maven:
```bash
mvn clean package
mvn exec:java -Dexec.mainClass="sorokin.java.course.Main"
```

Таблицы создаются автоматически (`hibernate.hbm2ddl.auto=update`).
## Пример работы

Реальная сессия на чистой базе (SQL-запросы Hibernate из лога опущены).

```text
MiniBank started. Type EXIT to stop.
Enter command:
USER_CREATE
Enter login:
krypto_babka
User created: User{id=1, login='krypto_babka', accountList=[Account{id=1, moneyAmount=500}]}
 
Enter command:
USER_CREATE
Enter login:
broke_but_happy
User created: User{id=2, login='broke_but_happy', accountList=[Account{id=2, moneyAmount=500}]}
 
Enter command:
USER_CREATE
Enter login:
krypto_babka
Error: User already exists with login=krypto_babka
 
Enter command:
ACCOUNT_CREATE
Enter user id:
1
Account created: Account{id=3, moneyAmount=500}
 
Enter command:
ACCOUNT_DEPOSIT
Enter account id:
1
Enter amount:
100000
Deposited 100000 to account 1.
 
Enter command:
ACCOUNT_TRANSFER
Enter source account id:
1
Enter target account id:
2
Enter amount:
10000
Transfer completed from account 1 to account 2.
 
Enter command:
SHOW_ALL_USERS
List of all users:
User{id=1, login='krypto_babka', accountList=[Account{id=3, moneyAmount=500}, Account{id=1, moneyAmount=90500}]}
User{id=2, login='broke_but_happy', accountList=[Account{id=2, moneyAmount=10300}]}
 
Enter command:
ACCOUNT_TRANSFER
Enter source account id:
1
Enter target account id:
3
Enter amount:
500
Transfer completed from account 1 to account 3.
 
Enter command:
SHOW_ALL_USERS
List of all users:
User{id=1, login='krypto_babka', accountList=[Account{id=1, moneyAmount=90000}, Account{id=3, moneyAmount=1000}]}
User{id=2, login='broke_but_happy', accountList=[Account{id=2, moneyAmount=10300}]}
 
Enter command:
ACCOUNT_CLOSE
Enter account id to close:
1
Account 1 closed.
 
Enter command:
ACCOUNT_CLOSE
Enter account id to close:
2
Error: Can't close the only one account
 
Enter command:
ACCOUNT_TRANSFER
Enter source account id:
3
Enter target account id:
2
Enter amount:
99999999
Error: insufficient funds on account id=3, moneyAmount=91000, attempted transfer=99999999
 
Enter command:
SHOW_ALL_USERS
List of all users:
User{id=1, login='krypto_babka', accountList=[Account{id=3, moneyAmount=91000}]}
User{id=2, login='broke_but_happy', accountList=[Account{id=2, moneyAmount=10300}]}
 
Enter command:
EXIT
MiniBank stopped.
```

Что здесь происходит:
- при создании пользователя сразу создаётся дефолтный счёт на 500; пользователю `krypto_babka` добавлен второй счёт (`ACCOUNT_CREATE`, счёт 3);
- повторное создание пользователя с тем же логином отклоняется с понятной ошибкой;
- после пополнения счёта 1 на 100000 на нём 100500;
- перевод между разными пользователями (счёт 1 → 2, сумма 10000) идёт с комиссией 2%: со счёта 1 списано 10000 (100500 → 90500), на счёт 2 дошло 9800 (500 → 10300);
- перевод между своими счетами (счёт 1 → 3, сумма 500) идёт без комиссии: 90500 → 90000 и 500 → 1000;
- при закрытии счёта 1 его остаток (90000) переносится на оставшийся счёт 3 пользователя: 1000 + 90000 = 91000;
- единственный счёт пользователя закрыть нельзя (счёт 2 у `broke_but_happy`);
- перевод больше баланса отклоняется, балансы при этом не меняются (видно по `SHOW_ALL_USERS` сразу после);
- приложение завершается командой `EXIT`.