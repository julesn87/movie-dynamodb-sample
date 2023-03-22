package com.ntt.sample.movie.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.javafaker.Faker;
import com.ntt.sample.movie.entity.MovieModel;
import com.ntt.sample.movie.entity.MovieModel.MovieCategoryEnum;
import com.ntt.sample.movie.entity.MovieModel.RoleEnum;
import com.ntt.sample.movie.entity.MovieModel.TypeEnum;
import com.ntt.sample.movie.repository.MovieRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class MovieRepositoryTest {

  @Autowired private MovieRepository movieRepository;

  @PostConstruct
  public void init() {
    generateData();
  }

  @Test
  void getMovieDetailsTest() {

    // given
    String movieName = "DieHard";

    // when
    Optional<MovieModel> movieDetails = movieRepository.getMovieDetails(movieName);
    log.info("Movie Details for DieHard: {}", movieDetails);

    // then
    assertThat(movieDetails).isPresent();
    assertThat(movieDetails.get().getName()).isEqualTo("DieHard");
  }

  @Test
  void getDirectorByMovieTest() {

    // given
    String movieName = "DieHard2";

    // when
    Optional<MovieModel> directorByMovie = movieRepository.getDirectorByMovie(movieName);

    // then
    assertThat(directorByMovie).isPresent();
    assertThat(directorByMovie.get().getName()).isEqualTo("RennyHarlin");
  }

  @Test
  void getDirectorsByNameTest() {

    // given
    String director = "RennyHarlin";

    // when
    List<MovieModel> moviesByDirector = movieRepository.getMoviesByDirector(director);
    log.info("Movies by Director RennyHarlin: {}", moviesByDirector);

    // then
    assertThat(moviesByDirector)
        .hasSize(1)
        .extracting(MovieModel::getPk)
        .containsExactlyInAnyOrder("MOVIE#DieHard2");
  }

  @Test
  void getMoviesByCharacterAndRoleTest() {

    // given
    String character = "JohnMcClane";
    RoleEnum roleEnum = RoleEnum.PROTAGONIST;

    // when
    List<MovieModel> moviesByCharacterAndRole =
        movieRepository.getMoviesByCharacterAndRole(character, roleEnum);
    log.info("Movies by character JohnMcClane: {}", moviesByCharacterAndRole);

    // then
    assertThat(moviesByCharacterAndRole)
        .hasSize(2)
        .extracting(MovieModel::getPk)
        .containsExactlyInAnyOrder("MOVIE#DieHard", "MOVIE#DieHard2");
  }

  @Test
  void getMoviesByCategoryTest() {

    // given
    MovieCategoryEnum movieCategoryEnum = MovieCategoryEnum.ACTION;

    // when
    List<MovieModel> moviesByCategory = movieRepository.getMoviesByCategory(movieCategoryEnum);
    log.info("Movies by category Action: {}", moviesByCategory);

    // then
    assertThat(moviesByCategory)
        .hasSize(2)
        .extracting(MovieModel::getPk)
        .containsExactlyInAnyOrder("MOVIE#DieHard", "MOVIE#DieHard2");
  }

  @Test
  void getActionMoviesBeforeYearTest() {

    // given
    Integer year = 1989;

    // when
    List<MovieModel> actionMoviesBeforeYear =
        movieRepository.getActionMoviesBeforeYear(MovieCategoryEnum.ACTION, year);
    log.info("Action Movies before year 1989: {}", actionMoviesBeforeYear);

    // then
    assertThat(actionMoviesBeforeYear)
        .hasSize(1)
        .extracting(MovieModel::getPk)
        .containsExactlyInAnyOrder("MOVIE#DieHard");
  }

  private void generateData() {

    String movieName = "DieHard";
    String partionKey = MovieModel.createPartionKey(movieName);

    List<MovieModel> movies = new ArrayList<>();

    var movie =
        MovieModel.builder()
            .pk(partionKey)
            .name(movieName)
            .budget(35)
            .year(1988)
            .type(TypeEnum.MOVIE)
            .category(MovieCategoryEnum.ACTION)
            .build();

    movies.add(movie);

    var character1 =
        MovieModel.builder()
            .pk(partionKey)
            .name("JohnMcClane")
            .type(TypeEnum.CHARACTER)
            .role(RoleEnum.PROTAGONIST)
            .nationality("Irish-American")
            .build();

    movies.add(character1);

    var character2 =
        MovieModel.builder()
            .pk(partionKey)
            .name("HansGruber")
            .type(TypeEnum.CHARACTER)
            .role(RoleEnum.ANTAGONIST)
            .nationality("German")
            .build();

    movies.add(character2);

    var director =
        MovieModel.builder().pk(partionKey).name("JohnMcTiernan").type(TypeEnum.DIRECTOR).build();

    movies.add(director);

    String movieName2 = "DieHard2";
    String partionKey2 = MovieModel.createPartionKey(movieName2);

    var movie2 =
        MovieModel.builder()
            .pk(partionKey2)
            .name(movieName2)
            .budget(70)
            .year(1990)
            .type(TypeEnum.MOVIE)
            .category(MovieCategoryEnum.ACTION)
            .build();

    movies.add(movie2);

    var character =
        MovieModel.builder()
            .pk(partionKey2)
            .name("JohnMcClane")
            .type(TypeEnum.CHARACTER)
            .nationality("Irish-American")
            .role(RoleEnum.PROTAGONIST)
            .build();

    movies.add(character);

    var director2 =
        MovieModel.builder().pk(partionKey2).name("RennyHarlin").type(TypeEnum.DIRECTOR).build();

    movies.add(director2);

    //    for (int x = 0; x < 500; x++) {
    //      MovieModel movieModel = generateRandomCharacter(partionKey);
    //      movies.add(movieModel);
    //    }

    movieRepository.batchWrite(movies);
  }

  private MovieModel generateRandomCharacter(String pk) {

    List<MovieModel> characters = new ArrayList<>();
    Faker faker = new Faker();
    var name = faker.name().fullName().replaceAll("\\s", "");
    var nationality = faker.nation().nationality();
    return MovieModel.builder()
        .pk(pk)
        .name(name)
        .type(TypeEnum.CHARACTER)
        .nationality(nationality)
        .build();
  }
}
