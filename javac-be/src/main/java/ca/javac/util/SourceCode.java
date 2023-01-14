package ca.javac.util;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class SourceCode {

  private String code;
  private String input;
}
