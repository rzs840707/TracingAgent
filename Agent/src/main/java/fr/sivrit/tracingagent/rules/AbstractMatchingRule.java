package fr.sivrit.tracingagent.rules;

public abstract class AbstractMatchingRule implements InstrumentationRule {
   private final boolean result;

   public AbstractMatchingRule(final boolean result) {
      super();
      this.result = result;
   }

   protected abstract boolean match(final String className);

   @Override
   public final Boolean mustRedefine(final String className) {
      return match(className) ? result : null;
   }

   @Override
   public String toString() {
      return "AbstractMatchingRule [result=" + result + "]";
   }
}
