package fr.sivrit.tracingagent.probe;

/**
 * Default implementation of {@link Tracer} that does nothing.
 */
public final class NoopTracer implements Tracer {
	/** No state: we will only ever need one instance of this class */
	public static final Tracer INSTANCE = new NoopTracer();

	private NoopTracer() {
		super();
	}

	@Override
	public void traceIn(String methodName) {
	}

	@Override
	public void traceOut() {
	}
}
