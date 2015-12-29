/*
 * Copyright 2015 smoope GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smoope.utils;

import com.jcabi.aether.Aether;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.JavaScopes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Mojo(name = "convert", defaultPhase = LifecyclePhase.PACKAGE)
public class J2ObjCConverterMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession session;

    @Parameter(required = true)
    private String j2objcPath;

    @Parameter
    private List<Dependency> dependencies;

    @Parameter(defaultValue = "${project.build.directory}/j2objc")
    private String d;

    @Parameter(defaultValue = "${project.basedir}/src/main/java")
    private String sourcePath;

    @Parameter(defaultValue = "UTF-8")
    private String encoding;

    @Parameter(defaultValue = "false")
    private Boolean g;

    @Parameter(defaultValue = "false")
    private Boolean quiet;

    @Parameter(defaultValue = "false")
    private Boolean verbose;

    @Parameter(defaultValue = "false")
    private Boolean werror;

    @Parameter(defaultValue = "0")
    private Integer batchTranslateMax;

    @Parameter(defaultValue = "false")
    private Boolean buildClosure;

    @Parameter(defaultValue = "")
    private String deadCodeReport;

    @Parameter(defaultValue = "false")
    private Boolean docComments;

    @Parameter(defaultValue = "false")
    private Boolean extractUnsequenced;

    @Parameter(defaultValue = "false")
    private Boolean generateDeprecated;

    @Parameter(defaultValue = "")
    private String mapping;

    @Parameter(defaultValue = "false")
    private Boolean noClassMethods;

    @Parameter(defaultValue = "false")
    private Boolean noFinalMethodsFunctions;

    @Parameter(defaultValue = "false")
    private Boolean noHidePrivateMembers;

    @Parameter(defaultValue = "false")
    private Boolean noPackageDirectories;

    @Parameter(defaultValue = "")
    private String prefix;

    @Parameter(defaultValue = "")
    private String prefixes;

    @Parameter(defaultValue = "false")
    private Boolean preserveFullPaths;

    @Parameter(defaultValue = "false")
    private Boolean stripGwtIncompatible;

    @Parameter(defaultValue = "false")
    private Boolean stripReflection;

    @Parameter(defaultValue = "false")
    private Boolean segmentedHeaders;

    @Parameter(defaultValue = "false")
    private Boolean timingInfo;

    @Parameter(defaultValue = "false")
    private Boolean useArc;

    @Parameter(defaultValue = "false")
    private Boolean useReferenceCounting;

    @Parameter(defaultValue = "false")
    private Boolean version;

    @Parameter(defaultValue = "objective-c")
    private String x;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            executeCommand(
                    buildCommand(
                            sourcePath,
                            resolveDependencies(new DefaultArtifact(
                                    mavenProject.getGroupId(),
                                    mavenProject.getArtifactId(),
                                    "",
                                    mavenProject.getPackaging(),
                                    mavenProject.getVersion()
                            )),
                            getSourceFiles(),
                            ""
                    )
            );
            if (dependencies != null && !dependencies.isEmpty()) {
                for (Dependency dependency: dependencies) {
                    Artifact artifact = new DefaultArtifact(
                            dependency.getGroupId(),
                            dependency.getArtifactId(),
                            "sources",
                            "jar",
                            dependency.getVersion()
                    );
                    String path = extractJar(dependency.getArtifactId(),
                            new File(session.getLocalRepository().getBasedir(), session.getLocalRepositoryManager().getPathForLocalArtifact(artifact))
                    );
                    executeCommand(
                            buildCommand(
                                    path,
                                    resolveDependencies(artifact),
                                    Arrays.asList(FileUtils.getFilesFromExtension(path, new String[]{"java"})),
                                    dependency.getArtifactId()
                            )
                    );
                }
            }
        } catch (DependencyResolutionException e) {
            getLog().error(e);
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    private void executeCommand(final String command) {
        getLog().info(String.format("Executing: %s", command));

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader result = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));
            BufferedReader errors = new BufferedReader(new
                    InputStreamReader(process.getErrorStream()));

            String output = null;
            while ((output = result.readLine()) != null) {
                getLog().info(output);
            }
            while ((output = errors.readLine()) != null) {
                getLog().error(output);
            }

        }  catch (IOException e) {
            getLog().error(e);
        }
    }

    private List<String> getSourceFiles() {
        File sources = new File(sourcePath);

        if (sources.isDirectory() == false){
            throw new RuntimeException(String.format("%s is not directory", sourcePath));
        }

        String[] list = FileUtils.getFilesFromExtension(sourcePath, new String[] { "java" });
        return Arrays.asList(list);
    }

    private String buildCommand(final String sourcePath, final Collection<Artifact> classPathDependencies,
                                final List<String> file, final String output) throws DependencyResolutionException {
        List<String> result = new ArrayList<String>();

        result.add(j2objcPath);
        if (StringUtils.isNotBlank(encoding)) {
            result.add(String.format("-encoding %s", encoding));
        }
        if (g) {
            result.add("-g");
        }
        if (quiet) {
            result.add("--quiet");
        }
        if (verbose) {
            result.add("--verbose");
        }
        if (werror) {
            result.add("-Werror");
        }
        if (batchTranslateMax > 0) {
            result.add(String.format("--batch-translate-max=%d", batchTranslateMax));
        }
        if (buildClosure) {
            result.add("--build-closure");
        }
        if (StringUtils.isNotBlank(deadCodeReport)) {
            result.add(String.format("--dead-code-report %s", deadCodeReport));
        }
        if (docComments) {
            result.add("--doc-comments");
        }
        if (extractUnsequenced) {
            result.add("--extract-unsequenced");
        }
        if (generateDeprecated) {
            result.add("--generate-deprecated");
        }
        if (StringUtils.isNotBlank(mapping)) {
            result.add(String.format("--mapping %s", mapping));
        }
        if (noClassMethods) {
            result.add("--no-class-methods");
        }
        if (noFinalMethodsFunctions) {
            result.add("--no-final-methods-functions");
        }
        if (noHidePrivateMembers) {
            result.add("--no-hide-private-members");
        }
        if (noPackageDirectories) {
            result.add("--no-package-directories");
        }
        if (StringUtils.isNotBlank(prefix)) {
            result.add(String.format("--prefix %s", prefix));
        }
        if (StringUtils.isNotBlank(prefixes)) {
            result.add(String.format("--prefixes %s", prefixes));
        }
        if (preserveFullPaths) {
            result.add("--preserve-full-paths");
        }
        if (stripGwtIncompatible) {
            result.add("--strip-gwt-incompatible");
        }
        if (stripReflection) {
            result.add("--strip-reflection");
        }
        if (segmentedHeaders) {
            result.add("--segmented-headers");
        }
        if (timingInfo) {
            result.add("--timing-info");
        }
        if (useArc) {
            result.add("-use-arc");
        }
        if (useReferenceCounting) {
            result.add("-use-reference-counting");
        }
        if (version) {
            result.add("-version");
        }
        if (StringUtils.isNotBlank(x)) {
            result.add(String.format("-x %s", x));
        }

        File outputDirectory = new File(d, output);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        result.add(String.format(" -sourcepath %s", sourcePath));
        result.add(String.format(" -d %s", outputDirectory.getAbsolutePath()));
        result.add(String.format(" -classpath %s", StringUtils.join(resolveClassPath(classPathDependencies).iterator(), ":")));
        result.add(String.format(" %s", StringUtils.join(file.iterator(), " ")));

        return StringUtils.join(result.iterator(), " ");
    }

    private List<String> resolveClassPath(Collection<Artifact> classPathDependencies) throws DependencyResolutionException {
        List<String> classPath = new ArrayList<String>();

        for (Artifact dep: classPathDependencies) {
            classPath.add(dep.getFile().getAbsolutePath());
        }

        return classPath;
    }

    private Collection<Artifact> resolveDependencies(final Artifact artifact) throws DependencyResolutionException {
        return new Aether(mavenProject, session.getLocalRepository().getBasedir()).resolve(
                artifact,
                JavaScopes.COMPILE
        );
    }

    private String extractJar(final String artifactId, final File file) throws IOException {
        File output = new File(mavenProject.getBuild().getOutputDirectory(), artifactId);
        if (!output.exists()) {
            output.mkdirs();
        }

        JarFile jarFile = new JarFile(file);
        Enumeration enumEntries = jarFile.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumEntries.nextElement();
            File f = new File(output, jarEntry.getName());
            if (jarEntry.isDirectory()) {
                f.mkdir();
            } else {
                InputStream is = jarFile.getInputStream(jarEntry);
                FileOutputStream fos = new FileOutputStream(f);
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
        }

        return output.getAbsolutePath();
    }
}
