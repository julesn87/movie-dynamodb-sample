package com.ntt.sample.movie.repository;

import com.ntt.sample.movie.entity.MovieModel;
import com.ntt.sample.movie.entity.MovieModel.MovieCategoryEnum;
import com.ntt.sample.movie.entity.MovieModel.RoleEnum;
import com.ntt.sample.movie.misc.Utils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch.Builder;

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

    // TODO

    return Optional.empty();
  }

  public Optional<MovieModel> getDirectorByMovie(String movieName) {

    // TODO

    return Optional.empty();
  }

  public List<MovieModel> getCharactersByMovie(String movieName) {

    // TODO

    return Collections.emptyList();
  }

  public List<MovieModel> getMoviesByDirector(String director) {

    // TODO

    return Collections.emptyList();
  }

  public List<MovieModel> getMoviesByCharacterAndRole(String characterName, RoleEnum roleEnum) {

    // TODO

    return Collections.emptyList();
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
