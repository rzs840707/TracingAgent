package fr.sivrit.tracingagent.transformer;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import fr.sivrit.tracingagent.rules.InstrumentationRule;

/**
 * This class is the actual agent that handle bytecode enhancement
 */
public class Transformer implements ClassFileTransformer {
   private final static Logger LOGGER = Logger.getLogger(Transformer.class
         .getName());

   private final Instrumentation inst;

   /** when <code>true</code>, we enhance the bytecode of classes being loaded */
   private boolean isActive = false;

   /**
    * Number of intercepted classes
    */
   private final AtomicLong callCounter = new AtomicLong(0);

   /**
    * Total number of classes that we have actually changed
    */
   private final AtomicLong instrumentedClassesCounter = new AtomicLong(0);

   /**
    * Cumulated time (in ns) we spent enhancing classes
    */
   private final AtomicLong estimatedOverHead = new AtomicLong(0);

   /**
    * Cumulated time (in ns) we spent redefining classes
    */
   private final AtomicLong totalRedefinitionTime = new AtomicLong(0);

   /** Tells us which classes to enhance */
   private final InstrumentationRule rules;

   /**
    * Cache of clean bytecode for all classes designated by {@link #rules}.
    * Having the clean bytecode allows us to:
    * <ul>
    * <li>delay the enhancement</li>
    * <li>undo the enhancement</li>
    * </ul>
    */
   private final ConcurrentHashMap<ClassKey, byte[]> originalDefinitions = new ConcurrentHashMap<ClassKey, byte[]>();

   public Transformer(final Instrumentation inst,
         final InstrumentationRule rules) {
      super();
      this.inst = inst;
      this.rules = rules;
   }

   /**
    * @param loader
    * @param className
    * @return <code>true</code> if the described class, from the given
    *         classloader, is a candidate for enhancement
    */
   private boolean mustRedefine(final ClassLoader loader, final String className) {
      if (loader == null) {
         // Do not touch the bootstrap classloader stuff
         return false;
      }
      final Boolean mustRedefine = rules.mustRedefine(className.replaceAll("/",
            "."));
      return Boolean.TRUE.equals(mustRedefine);
   }

   @Override
   public byte[] transform(final ClassLoader loader, final String className,
         final Class<?> classBeingRedefined,
         final ProtectionDomain protectionDomain, final byte[] classfileBuffer)
         throws IllegalClassFormatException {

      if (LOGGER.isLoggable(Level.FINEST)) {
         final String name = classBeingRedefined == null ? null
               : classBeingRedefined.getName();
         final Class<? extends ClassLoader> loaderClass = loader == null ? null
               : loader.getClass();
         final int size = classfileBuffer == null ? -1 : classfileBuffer.length;
         LOGGER.finest(String.format(
               "transform class: %s, loader: %s, redefined: %s size: %s",
               className, loaderClass, name, size));
      }

      final long t0 = System.nanoTime();
      final byte[] result;

      callCounter.incrementAndGet();
      if (mustRedefine(loader, className)) {
         // Class is interesting
         final ClassKey key = new ClassKey(loader, className);
         if (classBeingRedefined == null) {
            // Class is being loaded, so we cache its bytecode
            originalDefinitions.putIfAbsent(key, classfileBuffer);
         } else {
            // Class is being redefined. We should have it in our cache.
            assert originalDefinitions.contains(key) : className;
         }

         if (isActive()) {
            result = enhance(classfileBuffer);
         } else {
            // Either load the class or restores it
            result = originalDefinitions.get(key);
         }

         instrumentedClassesCounter.incrementAndGet();
      } else {
         result = null;
      }

      estimatedOverHead.addAndGet(System.nanoTime() - t0);
      return result;
   }

   /**
    * The ASM magic
    * 
    * @param byteCode
    * @return the transformed bytecode
    */
   private byte[] enhance(final byte[] byteCode) {
      final ClassReader cr = new ClassReader(byteCode);
      final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES
            | ClassWriter.COMPUTE_MAXS);

      final ClassVisitor adapter = new ClassAdapter(cw) {
         @Override
         public MethodVisitor visitMethod(int access, String name, String desc,
               String signature, String[] exceptions) {
            return new MethodAdapter(super.visitMethod(access, name, desc,
                  signature, exceptions)) {

               @Override
               public void visitMethodInsn(int opcode, String owner,
                     String name, String desc) {
                  // Trace the method entry
                  super.visitLdcInsn(owner + "." + name + desc);
                  super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "fr/sivrit/tracingagent/probe/TracerHolder", "traceIn",
                        "(Ljava/lang/String;)V");

                  // The method call
                  super.visitMethodInsn(opcode, owner, name, desc);

                  // Trace the method exit. We will pretend exceptions do not
                  // exists.
                  // TODO we pretend exceptions do not exists
                  super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "fr/sivrit/tracingagent/probe/TracerHolder",
                        "traceOut", "()V");
               }
            };
         }

      };
      cr.accept(adapter, 0);

      return cw.toByteArray();
   }

   public synchronized boolean isActive() {
      return isActive;
   }

   public void setActive(final boolean isActive) {
      synchronized (this) {
         if (this.isActive != isActive) {
            this.isActive = isActive;
         }
      }

      try {
         LOGGER.info("start redefineAllClasses");
         final long t0 = System.nanoTime();

         // Redefining classes means they will go through transform(...) again,
         // with the new value of isActive. This will result in them being
         // (un)enhanced.
         redefineAllClasses();

         totalRedefinitionTime.addAndGet(System.nanoTime() - t0);
         LOGGER.info("end redefineAllClasses");
      } catch (final Exception e) {
         LOGGER.log(Level.SEVERE, "redefineAllClasses failed", e);
      }
   }

   /**
    * Ask the {@link Instrumentation} to redefine all the classes we have in our
    * cache
    * 
    * @throws UnmodifiableClassException
    * @throws ClassNotFoundException
    */
   private void redefineAllClasses() throws UnmodifiableClassException,
         ClassNotFoundException {
      final Collection<ClassDefinition> redefinitions = new ArrayList<ClassDefinition>(
            originalDefinitions.size());
      for (final Entry<ClassKey, byte[]> entry : originalDefinitions.entrySet()) {
         final ClassKey classKey = entry.getKey();
         final ClassLoader classLoader = classKey.getCl();
         if (classLoader == null) {
            continue;
         }

         final String name = classKey.getClassName().replaceAll("/", ".");
         final Class<?> clazz;
         try {
            clazz = classLoader.loadClass(name);
         } catch (final ClassNotFoundException e) {
            LOGGER.warning("Could not find class for redefinition: " + name);
            continue;
         }

         if (!inst.isModifiableClass(clazz)) {
            LOGGER.warning("!isModifiableClass: " + name);
            continue;
         }

         if (entry.getValue() == null) {
            LOGGER.warning("no data! : " + name);
            continue;
         }

         final ClassDefinition cd = new ClassDefinition(clazz, entry.getValue());
         redefinitions.add(cd);
      }

      inst.redefineClasses(redefinitions
            .toArray(new ClassDefinition[redefinitions.size()]));
   }

   // Information for JMX
   public InstrumentationRule getRules() {
      return rules;
   }

   public long getCallCounter() {
      return callCounter.get();
   }

   public long getInstrumentedClassesCounter() {
      return instrumentedClassesCounter.get();
   }

   public long getOverhead() {
      return estimatedOverHead.get();
   }

   public long getTotalRedefinitionTime() {
      return totalRedefinitionTime.get();
   }

   public int getCacheSize() {
      return originalDefinitions.size();
   }

   public long getCacheContentSize() {
      long result = 0;
      for (final byte[] bytecode : originalDefinitions.values()) {
         if (bytecode != null) {
            result += bytecode.length;
         }
      }
      return result;
   }
}
