package com.ntt.sample.movie.config;

import com.ntt.sample.movie.entity.MovieModel;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Component
@Slf4j
public class DynamoDbInitializer {

  private final DynamoDbConfigProperties config;
  private final DynamoDbClient dynamoDbClient;
  private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
  private final DynamoDbWaiter dynamoDbWaiter;

  public DynamoDbInitializer(
          DynamoDbConfigProperties config,
          DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient dynamoDbEnhancedClient,
          DynamoDbWaiter dynamoDbWaiter) {
    this.config = config;
    this.dynamoDbClient = dynamoDbClient;
    this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
    this.dynamoDbWaiter = dynamoDbWaiter;
  }

  @PostConstruct
  public void initializeTables() {
    log.info("Started to initialize the dynamoDB tables.");

    createDynamoDBTable(
        MovieModel.class,
        MovieModel.TABLE_NAME,
        createTableEnhancedRequest(List.of(MovieModel.GSI_1, MovieModel.GSI_2)));

    log.info("Finished to initialize the dynamoDB tables.");
  }

  private void createDynamoDBTable(
      Class<?> modelClazz, String tableName, CreateTableEnhancedRequest request) {
    try {
      dynamoDbEnhancedClient
          .table(tableName, TableSchema.fromClass(modelClazz))
          .createTable(request);
      log.info("Created a table for entity {}.", tableName);
    } catch (ResourceInUseException e) {
      log.info("A table for entity {} already exists.", tableName);
    }

    try (dynamoDbWaiter) {
      ResponseOrException<DescribeTableResponse> response =
          dynamoDbWaiter
              .waitUntilTableExists(builder -> builder.tableName(tableName).build())
              .matched();
      DescribeTableResponse tableDescription =
          response
              .response()
              .orElseThrow(
                  () -> new RuntimeException(String.format("%s was not created.", tableName)));

      log.info(tableDescription.table().tableName() + " was created.");
    }
  }

  private CreateTableEnhancedRequest createTableEnhancedRequest(List<String> indexNames) {

      List<EnhancedGlobalSecondaryIndex> enhancedGlobalSecondaryIndices = null;
      if (!indexNames.isEmpty()) {
        enhancedGlobalSecondaryIndices =
            indexNames.stream().map(this::createIndex).collect(Collectors.toList());
      }
      return CreateTableEnhancedRequest.builder()
          .globalSecondaryIndices(enhancedGlobalSecondaryIndices)
          .build();

  }

  private EnhancedGlobalSecondaryIndex createIndex(String indexName) {
      return EnhancedGlobalSecondaryIndex.builder()
          .indexName(indexName)
          .projection(p -> p.projectionType(ProjectionType.ALL).build())
          .build();
    }
}
