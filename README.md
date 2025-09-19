# Projeto Music Notificator RabbitMQ - Producer & Consumer

## Integrantes
- Pedro Ueda - RA: 01241090
- Ronielli Andrade - RA: 01241040

---

## Visão geral do projeto
O tema do projeto é alertar quando uma nova música é lançada.

Este projeto demonstra o fluxo **Producer → RabbitMQ → Consumer**.
- A **API Java (Producer)** recebe requisições HTTP POST e envia mensagens para a fila RabbitMQ.
- O **RabbitMQ** gerencia a fila de mensagens.
- O **Consumer Node.js** consome mensagens da fila, salva em memória e em um arquivo `messages.txt`, e disponibiliza via HTTP GET.

---

## Conteúdo deste README
- Como configurar e rodar o Producer (API Java Spring Boot)
- Como iniciar um RabbitMQ local para testes (Docker Compose)
- Exemplos de requests

---

## Producer (API Java)

O Producer é uma aplicação Spring Boot localizada em `music-notificator-rabbitmq-producer` que expõe o endpoint HTTP:

- POST http://localhost:8080/musics

Exemplo de payload:
```json
{
  "name": "Shape of You",
  "artist": "Ed Sheeran",
  "album": "Divide"
}
```

Arquivos e propriedades importantes:
- `music-notificator-rabbitmq-producer/src/main/resources/application.properties` contém propriedades do app. Atualmente inclui:
  - `broker.exchange.name` (ex.: `example.fanout.exchange`)
  - `broker.queue.name` (ex.: `example.queue`)

Conexão com o RabbitMQ
- A aplicação usa as propriedades padrão do Spring Boot para RabbitMQ quando configuradas (por exemplo `spring.rabbitmq.host`, `spring.rabbitmq.port`, `spring.rabbitmq.username`, `spring.rabbitmq.password`).
- Você pode definir essas propriedades no `application.properties`, via variáveis de ambiente (ex.: `SPRING_RABBITMQ_USERNAME`) ou via argumentos de linha de comando ao executar a aplicação.

Pré-requisitos
- Java 21 (conforme `pom.xml`)
- Docker + Docker Compose (opcional, recomendado para subir um RabbitMQ local)
- Rede com porta 5672 disponível para AMQP e 15672 para a console do RabbitMQ (se usar Docker Compose)

Passo a passo: iniciar RabbitMQ (opcional, usando Docker Compose)
1. Entre na pasta do producer:

```cmd
cd music-notificator-rabbitmq-producer
```

2. Suba o serviço RabbitMQ definido em `compose.yaml` (isso traz a imagem com a interface de gerenciamento):

```cmd
docker compose -f compose.yaml up -d
```

- A compose define usuário e senha conforme o arquivo `compose.yaml`:
  - usuário: `myuser`
  - senha: `secret`
- Console do RabbitMQ disponível em: http://localhost:15672 (user: `myuser`, pass: `secret`)

Rodando o Producer localmente (usando o wrapper Maven incluído)

1) Executando diretamente com o wrapper (modo desenvolvimento):

```cmd
cd music-notificator-rabbitmq-producer
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.rabbitmq.host=localhost --spring.rabbitmq.port=5672 --spring.rabbitmq.username=myuser --spring.rabbitmq.password=secret"
```

Observações:
- Ajuste `--spring.rabbitmq.*` se o RabbitMQ estiver em outro host/porta/credenciais.
- Você também pode passar `--broker.exchange.name` e `--broker.queue.name` para alterar exchange/fila em tempo de execução.

2) Buildar e executar o JAR:

```cmd
cd music-notificator-rabbitmq-producer
.\mvnw.cmd clean package -DskipTests
java -jar target\music-notificator-0.0.1-SNAPSHOT.jar --spring.rabbitmq.host=localhost --spring.rabbitmq.username=myuser --spring.rabbitmq.password=secret
```

Usando variáveis de ambiente
- Em vez de argumentos, você pode usar as variáveis de ambiente:
  - SPRING_RABBITMQ_HOST
  - SPRING_RABBITMQ_PORT
  - SPRING_RABBITMQ_USERNAME
  - SPRING_RABBITMQ_PASSWORD

Exemplo (cmd.exe):

```cmd
set SPRING_RABBITMQ_USERNAME=myuser
set SPRING_RABBITMQ_PASSWORD=secret
set SPRING_RABBITMQ_HOST=localhost
.\mvnw.cmd spring-boot:run
```

Testando o endpoint (curl)
- Enviar uma música para o Producer:

```cmd
curl -X POST http://localhost:8080/musics -H "Content-Type: application/json" -d "{\"name\":\"Shape of You\",\"artist\":\"Ed Sheeran\",\"album\":\"Divide\"}"
```

- Se tudo estiver funcionando, o Producer publica uma mensagem na exchange/queue configurada e o Consumer (Node.js) poderá consumi-la.

Observações sobre exchange/queue
- Os nomes padrão estão em `application.properties`:
  - `broker.exchange.name=example.fanout.exchange`
  - `broker.queue.name=example.queue`
- Para alterar, edite `src/main/resources/application.properties` ou passe `--broker.exchange.name=novo` na linha de comando.

## Consumer (Node.js)

O Consumer é uma aplicação Node.js que se conecta ao RabbitMQ, consome mensagens da fila e as expõe via HTTP.

### Executando o Consumer

1. No diretório do consumer:

```cmd
cd music-notificator-rabbitmq-consumer
```

2. Instale as dependências:

```cmd
npm install express amqplib
```

3. Execute o consumer:

```cmd
node consumer.js
```

O consumer ficará escutando a fila example.queue e salvará todas as mensagens recebidas em memória (messages[]) e no arquivo messages.txt.

### Testando o Consumer

- Endpoint do consumer para ver mensagens: http://localhost:3000/messages

Exemplo de teste usando curl:

```cmd
curl http://localhost:3000/messages
```

---
