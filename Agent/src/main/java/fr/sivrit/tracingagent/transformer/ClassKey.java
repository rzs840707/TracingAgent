package fr.sivrit.tracingagent.transformer;

/**
 * Keys for the bytecode cache
 */
public final class ClassKey {
   private final ClassLoader cl;

   private final String className;

   public ClassKey(ClassLoader cl, String className) {
      super();
      this.cl = cl;
      this.className = className;
   }

   public ClassLoader getCl() {
      return cl;
   }

   public String getClassName() {
      return className;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((cl == null) ? 0 : cl.hashCode());
      result = prime * result
            + ((className == null) ? 0 : className.hashCode());
      return result;
   }

   @Override
   public boolean equals(final Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final ClassKey other = (ClassKey) obj;
      if (cl == null) {
         if (other.cl != null)
            return false;
      } else if (!cl.equals(other.cl))
         return false;
      if (className == null) {
         if (other.className != null)
            return false;
      } else if (!className.equals(other.className))
         return false;
      return true;
   }
}
