package ca.javac.util;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Tuple<T1, T2> {
  private final T1 left;
  private final T2 right;
}
