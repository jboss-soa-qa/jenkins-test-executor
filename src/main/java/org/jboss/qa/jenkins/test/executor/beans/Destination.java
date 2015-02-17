package org.jboss.qa.jenkins.test.executor.beans;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class Destination {

	@Getter @Setter private File destination;
}
