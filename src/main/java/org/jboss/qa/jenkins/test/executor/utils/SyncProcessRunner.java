package org.jboss.qa.jenkins.test.executor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyncProcessRunner {

	private final ProcessBuilder processBuilder;

	public static int run(ProcessBuilder processBuilder) throws IOException, InterruptedException {
		return new SyncProcessRunner(processBuilder).run();
	}

	public SyncProcessRunner(ProcessBuilder processBuilder) {
		this.processBuilder = processBuilder;
	}

	public int run() throws IOException, InterruptedException {
		final Process process = processBuilder.start();
		final ProcessOutputConsumer out = new ProcessOutputConsumer(process, System.out);
		out.start();
		final ProcessOutputConsumer err = new ProcessOutputConsumer(process, System.err);
		err.start();

		final int result = process.waitFor();
		out.join();
		err.join();
		return result;
	}

	/**
	 * This thread is workaround, because processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT) doesn't work on HPUX.<br/>
	 * Please, don't try to use processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT) until it works on HPUX.
	 */
	public class ProcessOutputConsumer extends Thread {

		private final boolean stderr;
		private final Process process;
		private final PrintStream out;

		public ProcessOutputConsumer(final Process process, final PrintStream out) {
			this(process, out, out == System.err);
		}

		public ProcessOutputConsumer(final Process process, final PrintStream out, final boolean stderr) {
			this.process = process;
			this.out = out;
			this.stderr = stderr;
			this.setDaemon(true);
		}

		@Override
		public void run() {
			try (BufferedReader r = new BufferedReader(new InputStreamReader(stderr ? process.getErrorStream() : process.getInputStream()))) {
				doRedirect(r);
			} catch (IOException e) {
				log.error("Problem while processing input stream.", e);
			}
		}

		private void doRedirect(BufferedReader reader) throws IOException {
			String line;
			while ((line = reader.readLine()) != null) {
				out.println(line);
				if (Thread.interrupted()) {
					return;
				}
			}
		}
	}
}
