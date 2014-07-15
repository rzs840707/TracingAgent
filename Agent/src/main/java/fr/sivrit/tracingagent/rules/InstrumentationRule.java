package fr.sivrit.tracingagent.rules;

/**
 *
 */
public interface InstrumentationRule {
   /**
    * @param className
    * @return
    */
   Boolean mustRedefine(String className);
}
