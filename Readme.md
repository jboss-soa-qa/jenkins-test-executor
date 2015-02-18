Jenkins Test Executor
=====================

Running:
--------

    mvn clean compile exec:java -Djob=org.jboss.qa.jenkins.test.executor.jobs.CamelFuseJob

Contribution:
-------------

Before you submit a pull request, please ensure that:

 * New issue is created for your task.
 * `mvn checkstyle:check` is passing.
 * There are no blank spaces.
 * There is new line at end of each file.
 * Commit messages start with '**Issue #ID**', are in the imperative and well formed.
     * See: [A Note About Git Commit Messages](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html)
