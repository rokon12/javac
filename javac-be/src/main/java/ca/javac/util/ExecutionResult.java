package ca.javac.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionResult {

  private final String result;
  private final long executionTime;
  private final ExecutionStatus status;
}
