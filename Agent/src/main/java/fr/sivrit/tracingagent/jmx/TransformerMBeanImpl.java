package fr.sivrit.tracingagent.jmx;

import fr.sivrit.tracingagent.transformer.Transformer;

public class TransformerMBeanImpl implements TransformerMBean {
   private final Transformer transformer;

   public TransformerMBeanImpl(final Transformer transformer) {
      super();
      this.transformer = transformer;
   }

   @Override
   public long getCallCounter() {
      return transformer.getCallCounter();
   }

   @Override
   public long getIntrumentedClassesCounter() {
      return transformer.getInstrumentedClassesCounter();
   }

   @Override
   public boolean isActive() {
      return transformer.isActive();
   }

   @Override
   public void setActive(final boolean newValue) {
      transformer.setActive(newValue);
   }

   @Override
   public long getOverheadMilli() {
      return transformer.getOverhead() / 1000000;
   }

   @Override
   public int getCacheSize() {
      return transformer.getCacheSize();
   }

   @Override
   public long getCacheContentSize() {
      return transformer.getCacheContentSize();
   }

   @Override
   public String getRules() {
      return transformer.getRules().toString();
   }

   @Override
   public long getTotalRedefinitionTimeMilli() {
      return transformer.getTotalRedefinitionTime() / 1000000;
   }
}
