package com.ntt.sample.movie.misc;

import java.util.List;import java.util.stream.Collectors;import java.util.stream.IntStream;public class Utils {


  public static <T> List<List<T>> chunks(List<T> collection, int batchSize) {
    return IntStream.iterate(0, i -> i < collection.size(), i -> i + batchSize)
        .mapToObj(i -> collection.subList(i, Math.min(i + batchSize, collection.size())))
        .collect(Collectors.toList());
  }
}
