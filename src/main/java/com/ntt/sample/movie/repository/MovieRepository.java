package com.ntt.sample.movie.repository;

import com.ntt.sample.movie.entity.MovieModel;
import com.ntt.sample.movie.entity.MovieModel.MovieCategoryEnum;
import com.ntt.sample.movie.entity.MovieModel.RoleEnum;
import com.ntt.sample.movie.entity.MovieModel.TypeEnum;
import com.ntt.sample.movie.misc.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch.Builder;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class MovieRepository {

  private final DynamoDbTable<MovieModel> mappedTable;
  private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

  public MovieRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
    this.mappedTable = dynamoDbEnhancedClient.table(MovieModel.TABLE_NAME,
        TableSchema.fromBean(MovieModel.class));
    this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
  }

  public void save(MovieModel movieModel) {
    mappedTable.putItem(movieModel);
  }

  public Optional<MovieModel> getMovieDetails(String movieName) {

    MovieModel movie =
        mappedTable.getItem(
            Key.builder()
                .partitionValue(MovieModel.createPartionKey(movieName))
                .sortValue(MovieModel.createSortKey(TypeEnum.MOVIE, movieName))
                .build());

    return Optional.ofNullable(movie);
  }

  public Optional<MovieModel> getDirectorByMovie(String movieName) {

    // TODO

    return Optional.empty();
  }

  public List<MovieModel> getCharactersByMovie(String movieName) {

    QueryConditional queryConditional =
        QueryConditional.sortBeginsWith(
            Key.builder()
                .partitionValue(MovieModel.createPartionKey(movieName))
                .sortValue(TypeEnum.CHARACTER.name())
                .build());

    return mappedTable.query(queryConditional).items().stream().collect(Collectors.toList());
  }

  public List<MovieModel> getMoviesByDirector(String director) {

    var gsi1 = mappedTable.index(MovieModel.GSI_1);

    QueryConditional queryConditional =
        QueryConditional.sortBeginsWith(
            Key.builder()
                .partitionValue(TypeEnum.DIRECTOR + "#" + director)
                .sortValue(TypeEnum.MOVIE.name())
                .build());

    Iterator<Page<MovieModel>> iterator = gsi1.query(queryConditional).iterator();

    List<MovieModel> movieModels = new ArrayList<>();

    while (iterator.hasNext()) {
      Page<MovieModel> movieModelPage = iterator.next();
      movieModels.addAll(movieModelPage.items());
    }

    return movieModels;
  }

  public List<MovieModel> getMoviesByCharacterAndRole(String characterName, RoleEnum roleEnum) {

    var gsi1 = mappedTable.index(MovieModel.GSI_1);

    AttributeValue attributeValue = AttributeValue.builder().s(roleEnum.name()).build();

    Map<String, AttributeValue> expressionValues = new HashMap<>();
    expressionValues.put(":value", attributeValue);

    Map<String, String> expressionAttributeNames = new HashMap<>();
    expressionAttributeNames.put("#roleName", "role");

    Expression expression =
        Expression.builder()
            .expression("#roleName = :value")
            .expressionValues(expressionValues)
            .expressionNames(expressionAttributeNames)
            .build();

    QueryConditional queryConditional =
        QueryConditional.sortBeginsWith(
            Key.builder()
                .partitionValue(TypeEnum.CHARACTER + "#" + characterName)
                .sortValue(TypeEnum.MOVIE.name())
                .build());

    Iterator<Page<MovieModel>> iterator =
        gsi1.query(r -> r.queryConditional(queryConditional).filterExpression(expression))
            .iterator();

    List<MovieModel> movieModels = new ArrayList<>();

    while (iterator.hasNext()) {
      Page<MovieModel> movieModelPage = iterator.next();
      movieModels.addAll(movieModelPage.items());
    }

    return movieModels;
  }

  public List<MovieModel> getActionMoviesBeforeYear(MovieCategoryEnum movieCategoryEnum,
      Integer year) {

    // TODO

    return Collections.emptyList();
  }

  public List<MovieModel> getMoviesByCategory(MovieCategoryEnum movieCategoryEnum) {

    // TODO

    return Collections.emptyList();
  }

  public void batchWrite(List<MovieModel> movieModels) {

    var chunckedMovies = Utils.chunks(movieModels, 25);

    for (List<MovieModel> movies : chunckedMovies) {

      Builder<MovieModel> movieModelBuilder = WriteBatch.builder(MovieModel.class)
          .mappedTableResource(mappedTable);
      movies.forEach(movieModelBuilder::addPutItem);
      BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
          .writeBatches(movieModelBuilder.build()).build();
      dynamoDbEnhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
    }
  }
}
