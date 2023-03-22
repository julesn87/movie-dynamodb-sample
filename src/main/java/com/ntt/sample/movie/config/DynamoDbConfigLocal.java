package com.ntt.sample.movie.config;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@EnableConfigurationProperties(DynamoDbConfigProperties.class)
@Configuration
public class DynamoDbConfigLocal {

  private final DynamoDbConfigProperties config;

  public DynamoDbConfigLocal(DynamoDbConfigProperties config) {
    this.config = config;
  }

  @Bean
  public DynamoDbEnhancedClient dynamoDbEnhancedClient() throws URISyntaxException {
    return DynamoDbEnhancedClient.builder().dynamoDbClient(getDynamoDbClient()).build();
  }

  @Bean
  public DynamoDbClient dynamoDbClient() throws URISyntaxException {
    return getDynamoDbClient();
  }

  @Bean
  public DynamoDbWaiter dynamoDbWaiter() throws URISyntaxException {
    return DynamoDbWaiter.builder().client(getDynamoDbClient()).build();
  }

  private DynamoDbClient getDynamoDbClient() throws URISyntaxException {
    return DynamoDbClient.builder()
        .region(Region.of(config.getRegion()))
        .credentialsProvider(
            () ->
                AwsBasicCredentials.create(
                    config.getCredentials().getAccessKey(), config.getCredentials().getSecretKey()))
        .endpointOverride(new URI(config.getUrl()))
        .build();
  }
}
