package fr.sivrit.traceragent.rules;

import java.io.IOException;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import fr.sivrit.tracingagent.rules.InstrumentationRule;
import fr.sivrit.tracingagent.rules.RuleParser;

public class RulesTests {
   public static class RuleMatcher extends BaseMatcher<InstrumentationRule>
         implements Matcher<InstrumentationRule> {
      private final Class<?> expectedTarget;
      private final Boolean expectedOutcome;

      public RuleMatcher(Class<?> expectedTarget, Boolean expectedOutcome) {
         super();
         this.expectedTarget = expectedTarget;
         this.expectedOutcome = expectedOutcome;
      }

      @Override
      public boolean matches(final Object rule) {
         final Boolean result = ((InstrumentationRule) rule)
               .mustRedefine(expectedTarget.getName());

         if (expectedOutcome == null)
            return result == null;
         else
            return expectedOutcome.equals(result);
      }

      @Override
      public void describeTo(final Description desc) {
         desc.appendText(expectedTarget.getName());
         desc.appendText(" should have been ");
         if (expectedOutcome == null) {
            desc.appendText("ignored");
         } else if (expectedOutcome == true) {
            desc.appendText("accepted");
         } else if (expectedOutcome == false) {
            desc.appendText("refused");
         }
      }
   }

   public static Matcher<InstrumentationRule> accepts(
         final Class<?> expectedTarget) {
      return new RuleMatcher(expectedTarget, true);
   }

   public static Matcher<InstrumentationRule> refuses(
         final Class<?> expectedTarget) {
      return new RuleMatcher(expectedTarget, false);
   }

   public static Matcher<InstrumentationRule> ignores(
         final Class<?> expectedTarget) {
      return new RuleMatcher(expectedTarget, null);
   }

   @Test
   public void acceptAllRule() throws IOException {
      final InstrumentationRule rule = RuleParser.parseString("\n + \n");

      Assert.assertThat(rule, accepts(String.class));
      Assert.assertThat(rule, accepts(RulesTests.class));
   }

   @Test
   public void refuseAllRule() throws IOException {
      final InstrumentationRule rule = RuleParser.parseString("- ");

      Assert.assertThat(rule, refuses(String.class));
      Assert.assertThat(rule, refuses(RulesTests.class));
   }

   @Test
   public void acceptWithPrefixRule() throws IOException {
      final InstrumentationRule rule = RuleParser
            .parseString("+ java.lang.C* ");

      Assert.assertThat(rule, ignores(String.class));
      Assert.assertThat(rule, ignores(RulesTests.class));
      Assert.assertThat(rule, accepts(Class.class));
      Assert.assertThat(rule, accepts(Cloneable.class));
   }

   @Test
   public void exactRules() throws IOException {
      final InstrumentationRule rule = RuleParser
            .parseString("+ java.lang.Boolean \n" + "-java.lang.Class\n"
                  + " +java.lang.Double\n" + " - java.lang.String \n"
                  + " + java.lang.Long \n");

      Assert.assertThat(rule, ignores(Cloneable.class));
      Assert.assertThat(rule, ignores(Float.class));
      
      Assert.assertThat(rule, accepts(Boolean.class));
      Assert.assertThat(rule, accepts(Double.class));
      Assert.assertThat(rule, accepts(Long.class));
      
      Assert.assertThat(rule, refuses(String.class));
      Assert.assertThat(rule, refuses(Class.class));
   }
}
