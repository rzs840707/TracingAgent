package fr.sivrit.tracingagent.rules;

public class PrefixRule extends AbstractMatchingRule {
   private final String prefix;

   public PrefixRule(final String prefix, final boolean result) {
      super(result);
      this.prefix = prefix;
   }

   @Override
   protected boolean match(final String className) {
      return className.startsWith(prefix);
   }

   @Override
   public String toString() {
      return "PrefixRule [prefix='" + prefix + "', " + super.toString() + "]";
   }

}
