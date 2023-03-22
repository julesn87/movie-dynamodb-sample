package com.ntt.sample.movie.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.dynamodb")
@Getter
@Setter
public class DynamoDbConfigProperties {
  private Credentials credentials;
  private String url;
  private String region;

  @Getter
  @Setter
  public static class Credentials {
    private String accessKey;
    private String secretKey;
  }
}
