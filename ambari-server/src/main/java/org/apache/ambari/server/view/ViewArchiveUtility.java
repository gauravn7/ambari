/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.view;

import jline.internal.Log;
import org.apache.ambari.server.api.services.ViewService;
import org.apache.ambari.server.view.configuration.ServiceConfig;
import org.apache.ambari.server.view.configuration.ViewConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

/**
 * Helper class for basic view archive utility.
 */
public class ViewArchiveUtility {

  /**
   * Constants
   */
  private static final String VIEW_XML = "view.xml";
  private static final String SERVICE_XML = "service.xml";
  private static final String WEB_INF_VIEW_XML = "WEB-INF/classes/" + VIEW_XML;
  private static final String WEB_INF_SERVICE_XML = "WEB-INF/classes/" + SERVICE_XML;
  public static final String VIEW_XSD = "view.xsd";
  public static final String SERVCIE_XSD = "view-service.xsd";
  /**
   * The logger.
   */
  protected final static Logger LOG = LoggerFactory.getLogger(ViewArchiveUtility.class);



  // ----- ViewArchiveUtility ------------------------------------------------

  /**
   * Get the view configuration from the given archive file.
   *
   * @param archiveFile  the archive file
   *
   * @return the associated view configuration
   *
   * @throws JAXBException if xml is malformed
   */
  public ViewConfig getViewConfigFromArchive(File archiveFile)
      throws JAXBException, IOException {
    ClassLoader cl = URLClassLoader.newInstance(new URL[]{archiveFile.toURI().toURL()});

    InputStream configStream = cl.getResourceAsStream(VIEW_XML);
    if (configStream == null) {
      configStream = cl.getResourceAsStream(WEB_INF_VIEW_XML);
      if (configStream == null) {
        throw new IllegalStateException(
            String.format("Archive %s doesn't contain a view descriptor.", archiveFile.getAbsolutePath()));
      }
    }

    try {

      JAXBContext jaxbContext       = JAXBContext.newInstance(ViewConfig.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      return (ViewConfig) jaxbUnmarshaller.unmarshal(configStream);
    } finally {
      configStream.close();
    }
  }

  public void extractServiceConfigFromArchive(File archiveFile , final String servicePath) throws IOException{

    Path path = Paths.get(archiveFile.getAbsolutePath());
    URI uri = URI.create("jar:file:" + path.toUri().getPath());
    Map<String, String> env = new HashMap<>();
    FileSystem fs =  FileSystems.newFileSystem(uri, env);

    Files.walkFileTree(fs.getPath("/"),new SimpleFileVisitor<Path>(){
          @Override
          public FileVisitResult visitFile(Path file,BasicFileAttributes attrs) throws IOException {
            Path destFile = Paths.get(servicePath,file.toString());
            if(file.toString().endsWith("-service.xml") && !destFile.toFile().exists()){
              Files.copy(file, destFile);
            }
            return FileVisitResult.CONTINUE;
          }
    });

  }

  /**
   * Get the view configuration from the given archive file.
   *
   * @param servicePath  the archive file
   *
   * @return the associated view configuration
   *
   * @throws JAXBException if xml is malformed
   */
  public List<ServiceConfig> getServiceConfig(String servicePath, boolean validate) {

    List<ServiceConfig> serviceConfigs = new ArrayList<ServiceConfig>();

    File [] serviceFiles = new File(servicePath).listFiles( new FileFilter() {
      public boolean accept( File file ) {
        return file.getName().endsWith("-service.xml");
      }
    });

    for(File file : serviceFiles){
      InputStream configStream = null;
      try {

        configStream = new FileInputStream(file);
        if (validate) {
          validateConfig(new FileInputStream(file),SERVCIE_XSD);
        }

        JAXBContext jaxbContext       = JAXBContext.newInstance(ServiceConfig.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        serviceConfigs.add((ServiceConfig)jaxbUnmarshaller.unmarshal(configStream));
      } catch (Exception e){
        LOG.error("Caught exception reading service :" + file.getName(), e);
      } finally {
        if(configStream != null) try {
          configStream.close();
        } catch (IOException e){
          LOG.error("Error closing stream :" + file.getName(), e);
        }
      }
    }

    return serviceConfigs;

  }

  /**
   * Get the view configuration from the extracted archive file.
   *
   * @param archivePath  path to extracted archive
   * @param validate     indicates whether or not the view configuration should be validated
   *
   * @return the associated view configuration
   *
   * @throws JAXBException if xml is malformed
   * @throws IOException if xml can not be read
   * @throws SAXException if the validation fails
   */
  public ViewConfig getViewConfigFromExtractedArchive(String archivePath, boolean validate)
      throws JAXBException, IOException, SAXException {
    File configFile = new File(archivePath + File.separator + VIEW_XML);

    if (!configFile.exists()) {
      configFile = new File(archivePath + File.separator + WEB_INF_VIEW_XML);
    }

    if (validate) {
      validateConfig(new FileInputStream(configFile),VIEW_XSD);
    }

    InputStream  configStream = new FileInputStream(configFile);
    try {

      JAXBContext  jaxbContext      = JAXBContext.newInstance(ViewConfig.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      return (ViewConfig) jaxbUnmarshaller.unmarshal(configStream);
    } finally {
      configStream.close();
    }
  }

  /**
   * Get a new file instance for the given path.
   *
   * @param path  the path
   *
   * @return a new file instance
   */
  public File getFile(String path) {
    return new File(path);
  }

  /**
   * Get a new file output stream for the given file.
   *
   * @param file  the file
   *
   * @return a new file output stream
   */
  public FileOutputStream getFileOutputStream(File file) throws FileNotFoundException {
    return new FileOutputStream(file);
  }

  /**
   * Get a new jar file stream from the given file.
   *
   * @param file  the file
   *
   * @return a new jar file stream
   */
  public JarInputStream getJarFileStream(File file) throws IOException {
    return new JarInputStream(new FileInputStream(file));
  }


  // ----- helper methods ----------------------------------------------------

  /**
   * Validate the given view descriptor file against the view schema.
   *
   * @param configStream  input stream of view descriptor file to be validated
   *
   * @throws SAXException if the validation fails
   * @throws IOException if the descriptor file can not be read
   */
  protected void validateConfig(InputStream  configStream, String resourceXSD) throws SAXException, IOException {
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    URL schemaUrl = getClass().getClassLoader().getResource(resourceXSD);
    Schema schema = schemaFactory.newSchema(schemaUrl);

    schema.newValidator().validate(new StreamSource(configStream));
  }
}
