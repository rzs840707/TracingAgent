package fr.sivrit.tracingagent.rules;

import java.util.Collection;

public class RuleSequence implements InstrumentationRule {
   private final InstrumentationRule[] rules;

   public RuleSequence(final Collection<InstrumentationRule> rules) {
      super();
      this.rules = rules.toArray(new InstrumentationRule[rules.size()]);
   }

   @Override
   public Boolean mustRedefine(final String className) {
      for (final InstrumentationRule rule : rules) {
         final Boolean result = rule.mustRedefine(className);
         if (result != null) {
            return result;
         }
      }

      return null;
   }

   @Override
   public String toString() {
      final StringBuilder result = new StringBuilder("RuleSequence:\n");
      for (final InstrumentationRule rule : rules) {
         result.append(" - ");
         result.append(rule);
         result.append('\n');
      }
      return result.toString();
   }

}
