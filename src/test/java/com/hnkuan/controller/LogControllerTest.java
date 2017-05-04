package com.hnkuan.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Created on 5/5/2017
 *
 * @author honnamkuan
 */
public class LogControllerTest {

  private static final String HOST = "http://localhost/";
  LogController controller;

  TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void setUp() throws IOException {

    folder.create();
    controller = new LogController(folder.getRoot().toString());
    folder.newFile("fileA.log");
    folder.newFile("fileB.log");
    folder.newFolder("directoryA");

    HttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), "/");
    ServletRequestAttributes attributes = new ServletRequestAttributes(mockRequest);

    RequestContextHolder.setRequestAttributes(attributes);
  }

  @Test
  public void testListDirectory() throws Exception {

    Set<String> URLs = controller.listDirectory();
    assertThat(URLs, contains(HOST.concat("fileA.log"), HOST.concat("fileB.log")));
  }

  @Test
  public void testDownloadFile() throws Exception {
    String pFileName = "fileB.log";
    ResponseEntity<FileSystemResource> response = controller.downloadFile(pFileName);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION),
        contains("attachment; filename=\"fileB.log\""));
    assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL),
        contains(CacheControl.noCache().getHeaderValue()));

    Path filePath = Paths.get(folder.getRoot().toString(), pFileName);
    FileSystemResource expected = new FileSystemResource(filePath.toFile());
    assertThat(response.getBody(), is(expected));
  }
}