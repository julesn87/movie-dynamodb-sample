package com.ntt.sample.movie.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MovieModel {

  public static final String TABLE_NAME = "MovieModel";
  public static final String SEPARATOR = "#";
  public static final String GSI_1 = "gsi1";
  public static final String GSI_2 = "gsi2";

  public enum TypeEnum {
    MOVIE,
    CHARACTER,
    DIRECTOR
  }

  public enum MovieCategoryEnum {
    ACTION
  }

  public enum RoleEnum {
    PROTAGONIST,
    ANTAGONIST
  }

  @DynamoDbIgnore
  public static String createPartionKey(String name) {
    return TypeEnum.MOVIE + SEPARATOR + name;
  }

  @DynamoDbIgnore
  public static String createSortKey(TypeEnum typeEnum, String name) {
    return typeEnum + SEPARATOR + name;
  }

  private String pk;
  private String sk;
  private TypeEnum type;
  private String name;

  private Integer year;
  private Integer budget;

  private RoleEnum role;

  private String playedBy;

  private String nationality;

  private String gsi1Pk;

  private String gsi1Sk;

  private String gsi2Pk;

  private Integer gsi2Sk;

  private MovieCategoryEnum category;

  @DynamoDbPartitionKey
  public String getPk() {
    return pk;
  }

  @DynamoDbSortKey
  public String getSk() {
    return createSortKey(type, name);
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "gsi1")
  public String getGsi1Pk() {
    return createSortKey(type, name);
  }

  @DynamoDbSecondarySortKey(indexNames = "gsi1")
  public String getGsi1Sk() {
    return createPartionKey(name);
  }

  @DynamoDbSecondaryPartitionKey(indexNames = "gsi2")
  public String getGsi2Pk() {
    if (category != null) {
      return category.name();
    }
    return null;
  }

  @DynamoDbSecondarySortKey(indexNames = "gsi2")
  public Integer getGsi2Sk() {
    return year;
  }
}
