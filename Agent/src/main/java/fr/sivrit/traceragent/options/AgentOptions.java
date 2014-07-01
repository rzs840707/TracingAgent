package fr.sivrit.traceragent.options;

import static java.lang.Boolean.parseBoolean;

import java.util.Properties;

//TODO javadoc
public class AgentOptions {

   public final static String ENHANCE_OPT = "enhance";

   public final static String LOG_ARGS_OPT = "log-arguments";

   public final static String LOG_OPT = "log";

   public final static String JMX_OPT = "jmx";

   public final static String OUTPUT_OPT = "output";

   public final static String RULES_OPT = "rules";

   public static AgentOptions parseOptions(final String args) {

      final Properties parsedArgs = new Properties();

      if (args != null) {
         final String[] splittedArgs = args.split(",");
         for (final String opt : splittedArgs) {
            if (opt.contains("=")) {
               final int pos = opt.indexOf('=');
               final String key = opt.substring(0, pos);
               final String value = opt.substring(pos + 1);
               parsedArgs.setProperty(key, value);
            } else {
               parsedArgs.setProperty(opt, "");
            }
         }
      }

      final boolean enhanceAtStartup = parseBoolean(parsedArgs
            .getProperty(ENHANCE_OPT));
      final boolean logAtStartup = parseBoolean(parsedArgs.getProperty(LOG_OPT));
      final boolean logArguments = parseBoolean(parsedArgs
            .getProperty(LOG_ARGS_OPT));
      final boolean enableJmx = parseBoolean(parsedArgs.getProperty(JMX_OPT,
            "true"));

      final String outputDir = parsedArgs.getProperty(OUTPUT_OPT,
            System.getProperty("user.home"));
      final String ruleFile = parsedArgs.getProperty(RULES_OPT);

      return new AgentOptions(enhanceAtStartup, logAtStartup, logArguments,
            enableJmx, outputDir, ruleFile);
   }

   private AgentOptions(final boolean enhanceAtStartup,
         final boolean logAtStartup, final boolean logArguments,
         final boolean enableJmx, final String outputDir, final String ruleFile) {
      super();
      this.enhanceAtStartup = enhanceAtStartup;
      this.logAtStartup = logAtStartup;
      this.logArguments = logArguments;
      this.enableJmx = enableJmx;
      this.outputDir = outputDir;
      this.ruleFile = ruleFile;
   }

   // TODO javadoc
   public final boolean enhanceAtStartup;
   public final boolean logAtStartup;
   public final boolean logArguments;
   public final boolean enableJmx;
   public final String outputDir;
   public final String ruleFile;
}
