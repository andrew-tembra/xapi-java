/*
 * Copyright 2016rue-2023 Berry Cloud Ltd. All rights reserved.
 */
package dev.learning.xapi.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import dev.learning.xapi.model.Statement;
import dev.learning.xapi.model.StatementFormat;
import dev.learning.xapi.model.Verb;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * XapiClient Tests.
 *
 * @author Thomas Turrell-Croft
 */
@DisplayName("XapiClient Tests")
@SpringBootTest
class XapiClientTests {

  @Autowired
  private WebClient.Builder webClientBuilder;

  private MockWebServer mockWebServer;
  private XapiClient client;

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    webClientBuilder.baseUrl(mockWebServer.url("").toString());

    client = new XapiClient(webClientBuilder);

  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  // Get Statement

  @Test
  void whenGettingStatementThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements
    client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6")).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingStatementThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statement
    client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6")).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6"));
  }

  @Test
  void whenGettingStatementThenBodyIsInstanceOfStatement() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")

        .setBody(
            "{\"actor\":{\"objectType\":\"Agent\",\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/attempted\",\"display\":{\"und\":\"attempted\"}},\"object\":{\"objectType\":\"Activity\",\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}}")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Statement
    var response = client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6")).block();

    // Then Body Is Instance Of Statement
    assertThat(response.getBody(), instanceOf(Statement.class));
  }

  @Test
  void whenGettingStatementWithAttachmentsThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statement With Attachments
    client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6").attachments(true))
        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6&attachments=true"));
  }

  @Test
  void whenGettingStatementWithCanonicalFormatThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statement With Canonical Format
    client
        .getStatement(
            r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6").format(StatementFormat.CANONICAL))
        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6&format=canonical"));
  }

  // Posting Statements

  @Test
  void whenPostingStatementsThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    final Statement attemptedStatement = Statement.builder()

        .actor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    final Statement passedStatement = attemptedStatement.toBuilder().verb(Verb.PASSED).build();

    Statement statements[] = {attemptedStatement, passedStatement};

    // When posting Statements
    client.postStatements(r -> r.statements(statements)).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingStatementsThenBodyIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    final Statement attemptedStatement = Statement.builder()

        .actor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    final Statement passedStatement = attemptedStatement.toBuilder().verb(Verb.PASSED).build();

    Statement statements[] = {attemptedStatement, passedStatement};

    // When Posting Statements
    client.postStatements(r -> r.statements(statements)).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Body Is Expected
    assertThat(recordedRequest.getBody().readUtf8(), is(
        "[{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/attempted\",\"display\":{\"und\":\"attempted\"}},\"object\":{\"objectType\":\"Activity\",\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}},{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/passed\",\"display\":{\"und\":\"passed\"}},\"object\":{\"objectType\":\"Activity\",\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}}]"));
  }

  @Test
  void whenPostingStatementsThenContentTypeHeaderIsApplicationJson() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    final Statement attemptedStatement = Statement.builder()

        .actor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    final Statement passedStatement = attemptedStatement.toBuilder().verb(Verb.PASSED).build();

    Statement statements[] = {attemptedStatement, passedStatement};

    // When Posting Statements
    client.postStatements(r -> r.statements(statements)).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  @Test
  void whenPostingStatementsThenResponseBodyIsInstanceOfUUIDArray() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody(
            "[\"2eb84e56-441a-492c-9d7b-f8e9ddd3e15d\",\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .addHeader("Content-Type", "application/json"));

    final Statement attemptedStatement = Statement.builder()

        .actor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb(Verb.ATTEMPTED)

        .activityObject(o -> o.id("https://example.com/activity/simplestatement")
            .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))

        .build();

    final Statement passedStatement = attemptedStatement.toBuilder().verb(Verb.PASSED).build();

    Statement statements[] = {attemptedStatement, passedStatement};

    // When Posting Statements
    ResponseEntity<UUID[]> response = client.postStatements(r -> r.statements(statements)).block();

    // Then Response Body Is Instance Of UUID Array
    assertThat(response.getBody(), instanceOf(UUID[].class));
  }

  // Posting a Statement

  @Test
  void whenPostingStatementThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When posting Statement
    client.postStatement(
        r -> r.statement(s -> s.actor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .verb(Verb.ATTEMPTED)

            .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))))
        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingStatementThenBodyIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When Posting Statement
    client.postStatement(
        r -> r.statement(s -> s.actor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .verb(Verb.ATTEMPTED)

            .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))))
        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Body Is Expected
    assertThat(recordedRequest.getBody().readUtf8(), is(
        "{\"actor\":{\"name\":\"A N Other\",\"mbox\":\"mailto:another@example.com\"},\"verb\":{\"id\":\"http://adlnet.gov/expapi/verbs/attempted\",\"display\":{\"und\":\"attempted\"}},\"object\":{\"objectType\":\"Activity\",\"id\":\"https://example.com/activity/simplestatement\",\"definition\":{\"name\":{\"en\":\"Simple Statement\"}}}}"));
  }

  @Test
  void whenPostingStatementThenContentTypeHeaderIsApplicationJson() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"19a74a3f-7354-4254-aa4a-1c39ab4f2ca7\"]")
        .setHeader("Content-Type", "application/json"));

    // When Posting Statement
    client.postStatement(
        r -> r.statement(s -> s.actor(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .verb(Verb.ATTEMPTED)

            .activityObject(o -> o.id("https://example.com/activity/simplestatement")
                .definition(d -> d.addName(Locale.ENGLISH, "Simple Statement")))))
        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  // Get Voided Statement

  @Test
  void whenGettingVoidedStatementThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Voided Statement
    client.getVoidedStatement(r -> r.id(UUID.fromString("4df42866-40e7-45b6-bf7c-8d5fccbdccd6")))
        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingVoidedStatementThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Voided Statement
    client.getVoidedStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6")).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?voidedStatementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6"));
  }

  @Test
  void whenGettingVoidedStatementWithAttachmentsThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Voided Statement With Attachments
    client.getStatement(r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6").attachments(true))
        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6&attachments=true"));
  }

  @Test
  void whenGettingVoidedStatementWithCanonicalFormatThenPathIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Voided Statement With Canonical Format
    client
        .getStatement(
            r -> r.id("4df42866-40e7-45b6-bf7c-8d5fccbdccd6").format(StatementFormat.CANONICAL))
        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?statementId=4df42866-40e7-45b6-bf7c-8d5fccbdccd6&format=canonical"));
  }

  // Get Statements

  @Test
  void whenGettingStatementsThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements
    client.getStatements().block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }


  @Test
  void whenGettingStatementsThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements
    client.getStatements().block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is("/statements"));
  }

  @Test
  void whenGettingStatementsWithAllParametersThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With All Parameters
    client.getStatements(r -> r

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .verb("http://adlnet.gov/expapi/verbs/answered")

        .activity("https://example.com/activity/1")

        .registration("dbf5d9e8-d2aa-4d57-9754-b11e3f195fe3")

        .relatedActivities(true)

        .relatedAgents(true)

        .since(Instant.parse("2016-01-01T00:00:00Z"))

        .until(Instant.parse("2018-01-01T00:00:00Z"))

        .limit(10)

        .format(StatementFormat.CANONICAL)

        .attachments(true)

        .ascending(true)

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/statements?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&verb=http%3A%2F%2Fadlnet.gov%2Fexpapi%2Fverbs%2Fanswered&activity=https%3A%2F%2Fexample.com%2Factivity%2F1&since=2016-01-01T00%3A00%3A00Z&until=2018-01-01T00%3A00%3A00Z&registration=dbf5d9e8-d2aa-4d57-9754-b11e3f195fe3&related_activities=true&related_agents=true&limit=10&format=canonical&attachments=true&ascending=true"));
  }

  @Test
  void whenGettingStatementsWithAgentParameterThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Agent Parameter
    client.getStatements(r -> r

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/statements?agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D"));
  }

  @Test
  void whenGettingStatementsWithVerbParameterThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Verb Parameter
    client.getStatements(r -> r

        .verb("http://adlnet.gov/expapi/verbs/answered")

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?verb=http%3A%2F%2Fadlnet.gov%2Fexpapi%2Fverbs%2Fanswered"));
  }

  @Test
  void whenGettingStatementsWithActivityParameterThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Activity Parameter
    client.getStatements(r -> r

        .activity("https://example.com/activity/1")

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(),
        is("/statements?activity=https%3A%2F%2Fexample.com%2Factivity%2F1"));
  }



  @Test
  void whenGettingMoreStatementsThenRequestMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Activity Parameter
    client.getMoreStatements(r -> r

        .more(mockWebServer.url("/xapi/statements/869cc589-76fa-4283-8e96-eea86f9124e1").uri())

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingMoreStatementsThenRequestURLExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting Statements With Activity Parameter
    client.getMoreStatements(r -> r

        .more(mockWebServer.url("/xapi/statements/869cc589-76fa-4283-8e96-eea86f9124e1").uri())

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Request URL Is Expected
    assertThat(recordedRequest.getRequestUrl(),
        is(mockWebServer.url("/xapi/statements/869cc589-76fa-4283-8e96-eea86f9124e1")));
  }

  // Get Single State

  @Test
  void whenGettingASingleStateThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting A Single State
    client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark"), String.class).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingASingleStateThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK"));

    // When Getting A Single State
    client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark"), String.class).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  @Test
  void whenGettingASingleStateWithoutRegistrationThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Getting A Single State Without Registration
    client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark"), String.class)

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingASingleStateWithoutRegistrationThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Getting A Single State Without Registration
    client.getState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark"), String.class)

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&stateId=bookmark"));
  }

  @Test
  void givenStateContentTypeIsTextPlainWhenGettingStateThenBodyIsInstanceOfString()
      throws InterruptedException {

    // Given State Content Type Is Text Plain
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody("Hello World!")
        .addHeader("Content-Type", "text/plain; charset=utf-8"));

    // When Getting State
    ResponseEntity<String> response = client
        .getState(r -> r.activityId("https://example.com/activity/1")

            .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

            .stateId("bookmark"), String.class)

        .block();

    // Then Body Is Instance Of String
    assertThat(response.getBody(), instanceOf(String.class));
  }

  @Test
  void givenStateContentTypeIsTextPlainWhenGettingStateThenBodyIsExpected()
      throws InterruptedException {

    // Given State Content Type Is Text Plain
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK").setBody("Hello World!")
        .addHeader("Content-Type", "text/plain; charset=utf-8"));

    // When Getting State
    ResponseEntity<String> response = client
        .getState(r -> r.activityId("https://example.com/activity/1")

            .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

            .stateId("bookmark"), String.class)

        .block();

    // Then Body Is Expected
    assertThat(response.getBody(), is("Hello World!"));
  }

  // Post Single State

  @Test
  void whenPostingASingleStateThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingASingleStateThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  @Test
  void whenPostingASingleStateWithContentTypeTextPlainThenContentTypeHeaderIsTextPlain()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State With Content Type Text Plain
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!")

        .contentType(MediaType.TEXT_PLAIN))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Text Plain
    assertThat(recordedRequest.getHeader("content-type"), is("text/plain"));
  }

  @Test
  void whenPostingASingleStateWithoutContentTypeThenContentTypeHeaderIsApplicationJson()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State Without Content Type
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  @Test
  void whenPostingASingleStateWithoutRegistrationThenMethodIsPost() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State Without Registration
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("POST"));
  }

  @Test
  void whenPostingASingleStateWithoutRegistrationThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Posting A Single State Without Registration
    client.postState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&stateId=bookmark"));
  }

  // Put Single State

  @Test
  void whenPuttingASingleStateThenMethodIsPut() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("PUT"));
  }

  @Test
  void whenPuttingASingleStateThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  @Test
  void whenPuttingASingleStateWithContentTypeTextPlainThenContentTypeHeaderIsTextPlain()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State With Content Type Text Plain
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!")

        .contentType(MediaType.TEXT_PLAIN))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Text Plain
    assertThat(recordedRequest.getHeader("content-type"), is("text/plain"));
  }

  @Test
  void whenPuttingASingleStateWithoutContentTypeThenContentTypeHeaderIsApplicationJson()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State Without Content Type
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Content Type Header Is Application Json
    assertThat(recordedRequest.getHeader("content-type"), is("application/json"));
  }

  @Test
  void whenPuttingASingleStateWithoutRegistrationThenMethodIsPut() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State Without Registration
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Post
    assertThat(recordedRequest.getMethod(), is("PUT"));
  }

  @Test
  void whenPuttingASingleStateWithoutRegistrationThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Putting A Single State Without Registration
    client.putState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")

        .state("Hello World!"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&stateId=bookmark"));
  }

  // Deleting Single State

  @Test
  void whenDeletingASingleStateThenMethodIsDelete() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single State
    client.deleteState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletingASingleStateThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single State
    client.deleteState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  @Test
  void whenDeletingASingleStateWithoutRegistrationThenMethodIsDelete() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single State Without Registration
    client.deleteState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .stateId("bookmark")).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletingASingleStateWithoutRegistrationThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting A Single State Without Registration
    client.deleteState(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

        .stateId("bookmark")).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6&stateId=bookmark"));
  }

  // Getting Multiple States

  @Test
  void whenGettingMultipleStatesThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States
    client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingMultipleStatesThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States
    client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6"))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6"));
  }

  @Test
  void whenGettingMultipleStatesWithoutRegistrationThenMethodIsGet() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States Without Registration
    client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com")))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Get
    assertThat(recordedRequest.getMethod(), is("GET"));
  }

  @Test
  void whenGettingMultipleStatesWithoutRegistrationThenPathIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States Without Registration
    client.getStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com")))

        .block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D"));
  }

  @Test
  void givenMultipleStatesExistWhenGettingMultipleStatesThenBodyIsInstanceOfStringArray()
      throws InterruptedException {

    // Given Multiple States Exist
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States
    ResponseEntity<String[]> response = client
        .getStates(r -> r.activityId("https://example.com/activity/1")

            .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6"))

        .block();

    // Then Body Is Instance Of String Array
    assertThat(response.getBody(), instanceOf(String[].class));
  }

  @Test
  void givenMultipleStatesExistWhenGettingMultipleStatesThenBodyIsExpected()
      throws InterruptedException {

    // Given Multiple States Exist
    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 200 OK")
        .setBody("[\"State1\", \"State2\", \"State3\"]")
        .addHeader("Content-Type", "application/json; charset=utf-8"));

    // When Getting Multiple States
    ResponseEntity<String[]> response = client
        .getStates(r -> r.activityId("https://example.com/activity/1")

            .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

            .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6"))

        .block();

    // Then Body Is Expected
    assertThat(response.getBody(), is(new String[] {"State1", "State2", "State3"}));
  }

  // Deleting Multiple States

  @Test
  void whenDeletingMultipleStatesThenMethodIsDelete() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting Multiple States
    client.deleteStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletingMultipleStatesThenPathIsExpected() throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting Multiple States
    client.deleteStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

        .registration("67828e3a-d116-4e18-8af3-2d2c59e27be6")

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D&registration=67828e3a-d116-4e18-8af3-2d2c59e27be6"));
  }

  @Test
  void whenDeletingMultipleStatesWithoutRegistrationThenMethodIsDelete()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting Multiple States Without Registration
    client.deleteStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Method Is Delete
    assertThat(recordedRequest.getMethod(), is("DELETE"));
  }

  @Test
  void whenDeletingMultipleStatesWithoutRegistrationThenPathIsExpected()
      throws InterruptedException {

    mockWebServer.enqueue(new MockResponse().setStatus("HTTP/1.1 204 No Content"));

    // When Deleting Multiple States Without Registration
    client.deleteStates(r -> r.activityId("https://example.com/activity/1")

        .agent(a -> a.name("A N Other").mbox("mailto:another@example.com"))

    ).block();

    RecordedRequest recordedRequest = mockWebServer.takeRequest();

    // Then Path Is Expected
    assertThat(recordedRequest.getPath(), is(
        "/activities/state?activityId=https%3A%2F%2Fexample.com%2Factivity%2F1&agent=%7B%22name%22%3A%22A%20N%20Other%22%2C%22mbox%22%3A%22mailto%3Aanother%40example.com%22%7D"));
  }

}