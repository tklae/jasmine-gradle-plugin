/*
 * Jasmine gradle plugin - http://purrpkg-gradle.sourceforge.net
 * Copyright (C) 2011 John Charles Roth
 *
 * This is free software, licensed under the terms of the GNU GPL 
 * Version 2 or, at your option, any later version. You should have 
 * received a copy of the license with this file. See the above web address
 * for more information, or contact the Free Software Foundation, Boston, MA. 
 * It is distributed WITHOUT WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.github.dzhaughnroth.jasmine
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskAction
/** If you need to custom configure this task, call initDefaults() first. */

class JasmineRunnerTask extends DefaultTask {

    DefaultValueFactory defaultsFactory = new ProjectValueFactory();

    boolean defaultsInited = false;

    def initDefaults() {
        defaultsFactory.setProject(project);
        parentDir = project
                .file("${project.buildDir}/${defaultsFactory.parentDir}");
        runnerIncludes = defaultsFactory.runnerIncludes;
        runnerExcludes = defaultsFactory.runnerExcludes;
        testIncludes = defaultsFactory.testIncludes;
        testExcludes = defaultsFactory.testExcludes;
        failBuildOnSpecFailure = defaultsFactory.failBuildOnSpecFailure;
        failBuildOnJslintFailure = defaultsFactory.failBuildOnJslintFailure;
        defaultsInited = true;
    }

    File parentDir;
    def runnerIncludes;
    def runnerexcludes;
    boolean failBuildOnSpecFailure;
    boolean failBuildOnJslintFailure;
    def testIncludes;
    def testExcludes;

    @TaskAction
	def findAndRunOnFiles() {
		if ( ! defaultsInited ) { initDefaults() };				
		def tree = project.fileTree {
		    	from parentDir
			include testIncludes
			exclude testExcludes
		}
		tree = tree.matching( {
			include runnerIncludes
			exclude runnerExcludes
		} );
		project.logger.log( LogLevel.INFO, "Running dashboards including ${testIncludes} matching ${runnerIncludes} excluding ${runnerExcludes}" );
		tree.each {
			project.logger.log( LogLevel.INFO, "Found spec runner ${it}" );
		}
		project.logger.log( LogLevel.INFO, "Jasmine-Gradle-Plugin Classpath ${project.configurations.jasmineGradlePlugin.asPath}" );
		project.ant.java( classpath:"${project.configurations.jasmineGradlePlugin.asPath}",
			fork:true, 
			resultProperty:"jasmineResult",
			classname:MultiRunnerMain.class.getName())  {
			arg( value: "${project.name}" );
			arg( value: "build" );
			tree.each {
				def itUri = it.toURI();
				def relative = project.buildDir.toURI().relativize( itUri );
				project.logger.info( "URI ${relative} to ${project.buildDir.toURI()} from ${itUri}" );
				arg( value:"${relative}" )
			}
		}
		project.logger.log( LogLevel.INFO, "Jasmine-gradle-plugin result: " + ant.jasmineResult );
		if ( ant.jasmineResult != "0" ) {
			throw new Exception( "Jasmine test runner process exited with error code: " + ant.jasmineResult );
		}
        File failuresFile = new File( project.buildDir, MultiRunnerMain.FAILURES_FILE_NAME );
		if ( failuresFile.exists() && failBuildOnSpecFailure ) {
            File resultsFile = new File( project.buildDir, MultiRunnerMain.XML_REPORT_FILE_NAME );
            String fileContents = resultsFile.text
            def failures = getFailures(fileContents)
            throw new RuntimeException( "Jasmine Specs Failed; see build/jasmine-summary.html for details\n--> FAILURE: " + failures.join('\n--> FAILURE: '));
		}
        File jslintFailuresFile = new File( project.buildDir, MultiRunnerMain.JSLINT_FAILURES_FILE_NAME );
        if ( jslintFailuresFile.exists() && failBuildOnJslintFailure ) {
            String fileContents = jslintFailuresFile.text
            throw new RuntimeException( "JSLint checks failed; see build/jasmine-summary.html");
        }
	}

    def getFailures(String xmlString) {
        def xml = new XmlSlurper().parseText(xmlString)
        List failureNodes = xml.'**'.grep {
            it.name() == 'failure' && it.parent().name() == 'testcase'
        }
        def failures = []
        failureNodes.each { failures << "${it.parent().'@classname'}.${it.parent().'@name'}: ${it.text()}" }
        return failures
    }
}
