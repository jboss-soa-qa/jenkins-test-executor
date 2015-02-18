package org.jboss.qa.jenkins.test.executor.utils.unpack;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public final class UnPackerRegistry {

	private static Map<String, UnPacker> unPackers;

	public static UnPacker get(String type) {
		if (unPackers == null) {
			unPackers = new HashMap<>();
			ServiceLoader<UnPacker> unPackerServiceLoader = ServiceLoader.load(UnPacker.class);
			for (UnPacker unPacker : unPackerServiceLoader) {
				unPackers.put(unPacker.type().toLowerCase(), unPacker);
			}
		}
		return unPackers.get(type);
	}
}
