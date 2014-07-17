package fr.sivrit.tracingagent.tracer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import fr.sivrit.tracingagent.probe.NoopTracer;
import fr.sivrit.tracingagent.probe.Tracer;
import fr.sivrit.tracingagent.probe.TracerHolder;

public class FileTracer implements Tracer {
   public static void setup(final String outputFile)
         throws FileNotFoundException, IOException {
      FILE_TRACER = new FileTracer(outputFile);
   }

   public volatile static Tracer FILE_TRACER = NoopTracer.INSTANCE;

   public static boolean isLogging = false;

   public static synchronized boolean isLogging() {
      return isLogging;
   }

   public static synchronized void setLogging(final boolean isLogging) {
      FileTracer.isLogging = isLogging;
      if (isLogging) {
         TracerHolder.set(FILE_TRACER);
      } else {
         TracerHolder.set(NoopTracer.INSTANCE);
      }
   }

   private final PrintWriter output;

   public FileTracer(final OutputStream output) {
      super();
      this.output = new PrintWriter(output);
   }

   private FileTracer(final String fileName) throws FileNotFoundException,
         IOException {
      super();

      // Create out output
      if (fileName.endsWith(".gz")) {
         output = new PrintWriter(new GZIPOutputStream(new FileOutputStream(
               fileName), 8 * 1024));
      } else {
         output = new PrintWriter(fileName);
      }

      // Make sure we flush when the JVM exits
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            output.flush();
            output.close();
         }
      });
   }

   @Override
   public void traceOut() {
      final String threadName = Thread.currentThread().getName();
      output.println(threadName + " <-");
   }

   @Override
   public void traceIn(String methodName) {
      final String threadName = Thread.currentThread().getName();
      output.println(threadName + " -> " + methodName);
   }
}
