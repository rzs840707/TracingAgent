package fr.sivrit.traceragent.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AgentOptionsTest {
   private void assertDefaults(final AgentOptions options) {
      assertFalse(options.enhanceAtStartup);
      assertFalse(options.logAtStartup);
      assertFalse(options.logArguments);

      assertTrue(options.enableJmx);

      assertEquals(System.getProperty("user.home"), options.outputDir);
      assertNull(options.ruleFile);
   }

   @Test
   public void noArgsMeansDefaultValues() {
      final AgentOptions opt = AgentOptions.parseOptions("");
      assertDefaults(opt);
   }

   @Test
   public void nullArgsMeansDefaultValues() {
      final AgentOptions opt = AgentOptions.parseOptions(null);
      assertDefaults(opt);
   }

   @Test
   public void unknownArgsAreTolerated() {
      final AgentOptions opt = AgentOptions
            .parseOptions("strangeArg,moar=args,and=mo-re");
      assertDefaults(opt);
   }

   @Test
   public void emtyArgsAreTolerated() {
      final AgentOptions opt = AgentOptions.parseOptions(",,,");
      assertDefaults(opt);
   }

   @Test
   public void setEnhanceToTrue() {
      final AgentOptions opt = AgentOptions.parseOptions("enhance=true");
      assertTrue(opt.enhanceAtStartup);
   }

   @Test
   public void setEnhanceToFalse() {
      final AgentOptions opt = AgentOptions.parseOptions("enhance=false");
      assertFalse(opt.enhanceAtStartup);
   }

   @Test
   public void setLogToTrue() {
      final AgentOptions opt = AgentOptions.parseOptions("log=true");
      assertTrue(opt.logAtStartup);
   }

   @Test
   public void setLogToFalse() {
      final AgentOptions opt = AgentOptions.parseOptions("log=false");
      assertFalse(opt.logAtStartup);
   }

   @Test
   public void setLogArgsToTrue() {
      final AgentOptions opt = AgentOptions.parseOptions("log-arguments=true");
      assertTrue(opt.logArguments);
   }

   @Test
   public void setLogArgsToFalse() {
      final AgentOptions opt = AgentOptions.parseOptions("log-arguments=false");
      assertFalse(opt.logArguments);
   }

   @Test
   public void setJmxToTrue() {
      final AgentOptions opt = AgentOptions.parseOptions("jmx=true");
      assertTrue(opt.enableJmx);
   }

   @Test
   public void setJmxToFalse() {
      final AgentOptions opt = AgentOptions.parseOptions("jmx=false");
      assertFalse(opt.enableJmx);
   }

   @Test
   public void setOutputDir() {
      final AgentOptions opt = AgentOptions.parseOptions("output=./somedir");
      assertEquals("./somedir", opt.outputDir);
   }

   @Test
   public void setRulesFile() {
      final AgentOptions opt = AgentOptions.parseOptions("rules=./rules.txt");
      assertEquals("./rules.txt", opt.ruleFile);
   }

   @Test
   public void setMultipleOptions() {
      final AgentOptions opt = AgentOptions
            .parseOptions("log=true,output=./somedir,rules=./rules.txt,jmx=true,");

      assertEquals("./somedir", opt.outputDir);
      assertEquals("./rules.txt", opt.ruleFile);
      assertTrue(opt.enableJmx);
      assertTrue(opt.logAtStartup);
      assertFalse(opt.logArguments);
   }
}
