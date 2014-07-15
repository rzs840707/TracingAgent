package fr.sivrit.tracingagent.rules;

import java.util.regex.Pattern;

public class RegexpRule extends AbstractMatchingRule {
   private final Pattern pattern;

   public RegexpRule(final String regexp, final boolean result) {
      super(result);
      pattern = Pattern.compile(regexp);
   }

   @Override
   protected boolean match(final String className) {
      return pattern.matcher(className).matches();
   }

   @Override
   public String toString() {
      return "RegexpRule [pattern='" + pattern + "', " + super.toString() + "]";
   }

}
