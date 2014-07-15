package fr.sivrit.tracingagent.rules;

public class MatchAllRule extends AbstractMatchingRule {
   public MatchAllRule(final boolean result) {
      super(result);
   }

   @Override
   protected boolean match(final String className) {
      return true;
   }

   @Override
   public String toString() {
      return "MatchAllRule [" + super.toString() + "]";
   }
}
