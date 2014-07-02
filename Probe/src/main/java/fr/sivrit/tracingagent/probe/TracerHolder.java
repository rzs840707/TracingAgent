package fr.sivrit.tracingagent.probe;

/**
 * Class holding the static reference to the {@link Tracer} that instrumented
 * bytecode should use.
 */
public final class TracerHolder {
	/**
	 * The current {@link Tracer} in use. The agent is expected to inject a real
	 * one when tracing is activated. Never null.
	 */
	private static volatile Tracer CURRENT = NoopTracer.INSTANCE;

	private TracerHolder() {
		throw new UnsupportedOperationException();
	}

	public static Tracer get() {
		return CURRENT;
	}

	public static void set(final Tracer tracer) {
		if (tracer == null) {
			throw new IllegalArgumentException();
		}
		CURRENT = tracer;
	}
}
