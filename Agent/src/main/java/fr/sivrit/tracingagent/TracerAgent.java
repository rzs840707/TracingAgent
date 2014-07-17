package fr.sivrit.tracingagent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import fr.sivrit.tracingagent.jmx.TransformerMBean;
import fr.sivrit.tracingagent.jmx.TransformerMBeanImpl;
import fr.sivrit.tracingagent.options.AgentOptions;
import fr.sivrit.tracingagent.rules.InstrumentationRule;
import fr.sivrit.tracingagent.rules.JvmClassesRule;
import fr.sivrit.tracingagent.rules.MatchAllRule;
import fr.sivrit.tracingagent.rules.RuleParser;
import fr.sivrit.tracingagent.rules.RuleSequence;
import fr.sivrit.tracingagent.tracer.FileTracer;
import fr.sivrit.tracingagent.transformer.Transformer;

public class TracerAgent {
   private final static Logger LOGGER = Logger.getLogger(TracerAgent.class
         .getName());

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
         throws IOException, MalformedObjectNameException,
         InstanceAlreadyExistsException, MBeanRegistrationException,
         NotCompliantMBeanException {
      // The (few) classes from the probe need to be visible by all the classes
      // on the JVM, so we add it to the bootstrap classLoader.
      final JarFile probeJarFile = extractProbe();
      inst.appendToBootstrapClassLoaderSearch(probeJarFile);

      final AgentOptions options = AgentOptions.parseOptions(agentArgs);

      LOGGER.info("Rules file: " + options.ruleFile);
      final InstrumentationRule rules;
      if (options.ruleFile == null || options.ruleFile.isEmpty()) {
         // Default: instrument everything but internal classes
         rules = new RuleSequence(new JvmClassesRule(false), new MatchAllRule(
               true));
      } else {
         rules = RuleParser.parseFile(options.ruleFile);
      }
      LOGGER.fine("Actual rules: " + rules);

      LOGGER.fine("Output: " + options.outputFile);
      if (options.outputFile != null && !options.outputFile.trim().isEmpty()) {
         FileTracer.setup(options.outputFile);
      }

      if (options.logAtStartup) {
         FileTracer.setLogging(true);
      }

      final Transformer transformer = new Transformer(inst, rules);
      transformer.setActive(options.enhanceAtStartup);
      inst.addTransformer(transformer);

      if (options.enableJmx) {
         final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         final ObjectName name = new ObjectName(
               "fr.sivrit.tracingagent:type=TransformerMBean");
         final TransformerMBeanImpl trMBean = new TransformerMBeanImpl(
               transformer);
         mbs.registerMBean(new StandardMBean(trMBean, TransformerMBean.class),
               name);
      }
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
