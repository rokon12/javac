package ca.javac.web;

import ca.javac.service.CompileAndCodeRunnerService;
import ca.javac.util.ExecutionStatus;
import ca.javac.util.SourceCode;
import ca.javac.util.ExecutionResult;
import ca.javac.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class JavaCompilerController {

  private final CompileAndCodeRunnerService compileAndCodeRunnerService;

  @CrossOrigin(origins = "http://localhost:3000")
  @PostMapping("/java/run-code")
  public ExecutionResult runCode(@RequestBody SourceCode sourceCode) {
    log.info("source: {}", sourceCode);

    if (ObjectUtils.isEmpty(sourceCode.getCode())) {
      return ExecutionResult.builder()
          .result("Empty editor, please enter some code and try again.")
          .status(ExecutionStatus.FAILED)
          .build();
    }
    if (StringUtils.validClassNameExists(sourceCode.getCode()).isEmpty()) {
      return ExecutionResult.builder()
          .result("No valid class found, please enter valid code and try again.")
          .status(ExecutionStatus.FAILED)
          .build();
    }

    return compileAndCodeRunnerService.execute(sourceCode.getCode(),
        sourceCode.getInput());
  }
}
