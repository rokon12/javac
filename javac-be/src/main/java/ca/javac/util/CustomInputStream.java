package ca.javac.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomInputStream extends InputStream{
	public final static ThreadLocal<InputStream> HOLD_INPUT_STREAM = new ThreadLocal<>();

	@Override
	public int read() throws IOException {
		return 0;
	}

	public InputStream get() {
		return HOLD_INPUT_STREAM.get();
	}

	public void set(String systemIn) {
		HOLD_INPUT_STREAM.set(new ByteArrayInputStream(systemIn.getBytes()));
	}

	@Override
	public void close() {
		HOLD_INPUT_STREAM.remove();
	}
}
