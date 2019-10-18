/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.qa.jenkins.test.executor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyncProcessRunner {

	private final List<ProcessBuilderListner> processBuilderListners;

	public SyncProcessRunner() {
		this.processBuilderListners = new ArrayList<>();
	}

	public int run(ProcessBuilder processBuilder) throws IOException, InterruptedException {
		final Process process = processBuilder.start();
		for (ProcessBuilderListner listener : processBuilderListners) {
			listener.onCreateOutputStream(process.getOutputStream());
		}
		final ProcessOutputConsumer out = new ProcessOutputConsumer(process, System.out);
		out.start();
		final ProcessOutputConsumer err = new ProcessOutputConsumer(process, System.err);
		err.start();

		final int result = process.waitFor();
		out.join();
		err.join();
		return result;
	}

	public void addProcessBuilderListener(ProcessBuilderListner listener) {
		processBuilderListners.add(listener);
	}

	public void removeProcessBuilderListener(ProcessBuilderListner listener) {
		processBuilderListners.remove(listener);
	}

	/**
	 * This thread is workaround, because processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT) doesn't work on HPUX.<br>
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

	public interface ProcessBuilderListner {
		void onCreateOutputStream(OutputStream outputStream);
	}
}
