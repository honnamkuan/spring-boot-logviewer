package com.hnkuan.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controller for allow log files access.
 */
@RestController
@RequestMapping("logs")
public class LogController {

  private String directory;

  /**
   * Constructor for {@link LogController}
   *
   * @param pDirectory The application log directory.
   */
  public LogController(@Value("${log.directory}") final String pDirectory) {
    directory = pDirectory;
  }

  /**
   * Download log file.
   *
   * @param pFileName The log file name.
   * @return The log file.
   */
  @GetMapping(value = "{file_name:.+}", produces = MediaType.ALL_VALUE)
  public ResponseEntity<FileSystemResource> downloadFile(
      @PathVariable("file_name") String pFileName) {
    final Path filePath = Paths.get(directory, pFileName);
    final FileSystemResource fileSystemResource = new FileSystemResource(filePath.toFile());
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pFileName + "\"")
        .cacheControl(CacheControl.noCache())
        .body(fileSystemResource);
  }

  /**
   * List files available in log directory.
   *
   * @return A unique list of files in log directory.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Set<URI> listDirectory() throws IOException {
    final Path filePath = Paths.get(directory);

    final Function<Path, URI> filePathToHttpURI = path -> {
      final String fileName = path.getFileName().toString();
      return ServletUriComponentsBuilder.fromCurrentRequestUri()
          .pathSegment(fileName)
          .build()
          .toUri();
    };

    return Files.list(filePath)
        .map(filePathToHttpURI)
        .collect(Collectors.toCollection(TreeSet::new));
  }
}
