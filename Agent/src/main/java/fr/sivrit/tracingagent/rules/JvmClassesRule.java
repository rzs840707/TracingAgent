package fr.sivrit.tracingagent.rules;

public class JvmClassesRule extends AbstractMatchingRule {

   public JvmClassesRule(final boolean result) {
      super(result);
   }

   @Override
   public boolean match(final String className) {
      if (className.startsWith("java.")) {
         return true;
      }
      if (className.startsWith("com.sun")) {
         return true;
      }
      if (className.startsWith("sun.")) {
         return true;
      }
      if (className.startsWith("sunw.")) {
         return true;
      }

      return false;
   }

   @Override
   public String toString() {
      return "JvmClassesRule [" + super.toString() + "]";
   }
}
