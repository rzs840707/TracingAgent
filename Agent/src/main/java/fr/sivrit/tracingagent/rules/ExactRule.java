package fr.sivrit.tracingagent.rules;

public class ExactRule extends AbstractMatchingRule {
   private final String className;

   public ExactRule(final String className, final boolean result) {
      super(result);
      this.className = className;
   }

   @Override
   protected boolean match(final String className) {
      return this.className.equals(className);
   }

   @Override
   public String toString() {
      return "ExactRule [className='" + className + "', " + super.toString()
            + "]";
   }
}
