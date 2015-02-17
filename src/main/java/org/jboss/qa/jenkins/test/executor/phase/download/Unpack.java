package org.jboss.qa.jenkins.test.executor.phase.download;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Unpack {

	public boolean unpack() default false;

	public Dst destination() default @Dst;

	public boolean findTargetDestination() default true;
}
