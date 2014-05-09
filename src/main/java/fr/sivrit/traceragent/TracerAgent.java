package fr.sivrit.traceragent;

import java.lang.instrument.Instrumentation;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

public class TracerAgent {
   public static void premain(final String agentArgs, final Instrumentation inst) {
      System.out.println("hello world");
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
