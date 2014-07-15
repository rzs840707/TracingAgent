package fr.sivrit.tracingagent.rules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class RuleParser {
   private RuleParser() {
      throw new UnsupportedOperationException();
   }

   public static InstrumentationRule parseFile(final String fileName)
         throws IOException {
      final File file = new File(fileName);
      if (!file.isFile()) {
         throw new IllegalArgumentException("Not a valid file: " + fileName);
      }

      final Charset charset = Charset.forName("UTF8");
      final Path path = file.toPath();
      try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
         return parseFile(reader);
      }
   }

   public static InstrumentationRule parseString(final String rules)
         throws IOException {
      try (Reader reader = new StringReader(rules);
            BufferedReader buffered = new BufferedReader(reader)) {
         return parseFile(buffered);
      }
   }

   public static InstrumentationRule parseFile(final BufferedReader reader)
         throws IOException {
      final Collection<InstrumentationRule> rules = new ArrayList<>();

      String line;
      while ((line = reader.readLine()) != null) {
         final String trimmedLine = line.trim();
         final boolean sign;

         if (trimmedLine.isEmpty()) {
            continue;
         } else if (trimmedLine.startsWith("+")) {
            sign = true;
         } else if (trimmedLine.startsWith("-")) {
            sign = false;
         } else {
            throw new IllegalArgumentException("Invalid rule: " + line);
         }

         final int length = trimmedLine.length();
         if (length == 1) {
            // Line is either "+" or "-"
            rules.add(new MatchAllRule(sign));
         } else if (trimmedLine.endsWith("*")) {
            final String prefix = trimmedLine.substring(1, length - 1).trim();
            rules.add(new PrefixRule(prefix, sign));
         } else if (trimmedLine.endsWith("/")) {
            final int start = trimmedLine.indexOf('/');
            final int end = trimmedLine.lastIndexOf('/');

            final String regexp = trimmedLine.substring(start + 1, end);
            rules.add(new RegexpRule(regexp, sign));
         } else if (trimmedLine.endsWith("]")) {
            final int start = trimmedLine.indexOf('[');
            final int end = trimmedLine.lastIndexOf(']');

            final String classClass = trimmedLine.substring(start + 1, end);
            if ("java".equalsIgnoreCase(classClass)) {
               rules.add(new JvmClassesRule(sign));
            } else {
               throw new IllegalArgumentException(String.format(
                     "Unrecognized class '%s' in rule '%s'", classClass, line));
            }
         } else {
            final String className = trimmedLine.substring(1).trim();
            rules.add(new ExactRule(className, sign));
         }
      }

      return new RuleSequence(rules);
   }
}
