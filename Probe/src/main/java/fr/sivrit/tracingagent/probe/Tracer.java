package fr.sivrit.tracingagent.probe;

/**
 * Interface for the component that will be called by instrumented bytecode to
 * trace stuff.
 */
public interface Tracer {
	/**
	 * Traces the act of entering a method
	 * 
	 * @param methodName
	 */
	void traceIn(String methodName);

	/**
	 * Traces the act of exiting a method
	 */
	void traceOut();
}
