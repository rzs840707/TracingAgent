package fr.sivrit.tracingagent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import fr.sivrit.tracingagent.options.AgentOptions;

public class TracerAgent {
   /**
    * Location of the embedded Jar with the code for the probe.
    */
   private final static String PROBE_RESOURCE = "lib/probe.jar";

   /**
    * Extracts the embedded Jar of the probe into its own temporary file, so
    * that it can be added to the classpath.
    * 
    * @return the {@link JarFile} corresponding to the extracted temporary Jar
    * @throws IOException
    */
   private static JarFile extractProbe() throws IOException {
      final File probeTempFile = File.createTempFile("probe", ".jar");

      final InputStream stream = TracerAgent.class.getClassLoader()
            .getResourceAsStream(PROBE_RESOURCE);
      try {
         Files.copy(stream, probeTempFile.toPath(),
               StandardCopyOption.REPLACE_EXISTING);
      } finally {
         stream.close();
      }

      return new JarFile(probeTempFile);
   }

   public static void premain(final String agentArgs, final Instrumentation inst)
         throws IOException {
      // The (few) classes from the probe need to be visible by all the classes
      // on the JVM, so we add it to the bootstrap classLoader.
      final JarFile probeJarFile = extractProbe();
      inst.appendToBootstrapClassLoaderSearch(probeJarFile);

      final AgentOptions options = AgentOptions.parseOptions(agentArgs);
   }

   public static void premain(final String agentArgs) {
      // not called
   }

   public static void agentmain(final String agentArgs,
         final Instrumentation inst) throws InstanceAlreadyExistsException,
         MBeanRegistrationException, NotCompliantMBeanException,
         MalformedObjectNameException, IOException {

      // Not really supported.
      // Call premain to do whatever we can
      premain(agentArgs, inst);
   }

}
