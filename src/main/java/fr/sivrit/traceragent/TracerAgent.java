package fr.sivrit.traceragent;

import java.lang.instrument.Instrumentation;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import fr.sivrit.traceragent.options.AgentOptions;

public class TracerAgent {
   public static void premain(final String agentArgs, final Instrumentation inst) {

      final AgentOptions options = AgentOptions.parseOptions(agentArgs);

   }

   public static void premain(final String agentArgs) {
      // not called
   }

   public static void agentmain(final String agentArgs,
         final Instrumentation inst) throws InstanceAlreadyExistsException,
         MBeanRegistrationException, NotCompliantMBeanException,
         MalformedObjectNameException {

      // Not really supported.
      // Call premain to do whatever we can
      premain(agentArgs, inst);
   }

}
