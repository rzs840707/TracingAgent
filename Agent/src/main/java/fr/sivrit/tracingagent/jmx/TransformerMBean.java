package fr.sivrit.tracingagent.jmx;

public interface TransformerMBean {
   boolean isActive();

   void setActive(boolean newValue);

   long getCallCounter();

   long getIntrumentedClassesCounter();

   long getOverheadMilli();

   long getTotalRedefinitionTimeMilli();

   int getCacheSize();

   long getCacheContentSize();

   String getRules();

   boolean isLogging();

   void setLogging(boolean newValue);
}
