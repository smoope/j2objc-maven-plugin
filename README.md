Maven plugin for J2ObjC library
==========

[![Build Status](https://travis-ci.org/smoope/j2objc-maven-plugin.svg?branch=master)](https://travis-ci.org/smoope/j2objc-maven-plugin)

# Installation

Add the following plugin entry into your pom's plugins section:

```
<plugin>
    <groupId>com.smoope.utils</groupId>
    <artifactId>j2objc-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <j2objcPath>path/to/j2objc</j2objcPath>
    </configuration>
</plugin>
```

In order to run the plugin, execute the following command: `mvn j2objc:convert`.

Once you want to try a snapshot version, don't forget to add the following section as well:

```
<pluginRepositories>
    <pluginRepository>
        <id>sonatype-nexus-snapshots</id>
        <name>sonatype-nexus-snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </pluginRepository>
</pluginRepositories>
```

# Usage

After you added the plugin to project, the only required parameter you have to specify is `j2objcPath`, which 
is path to J2ObjC's executable file. All the other plugin's parameters are proxies of J2ObjC's original parameters.

```
<plugin>
    <groupId>com.smoope.utils</groupId>
    <artifactId>j2objc-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <j2objcPath>${J2OBJC_DISTRIBUTION}/j2objc</j2objcPath>
        <useArc>true</useArc>
        <noPackageDirectories>true</noPackageDirectories>
        <!-- default value -->
        <sourcePath>${project.basedir}/src/main/java</sourcePath>
        <!-- default value -->
        <d>${project.build.directory}/j2objc</d>
    </configuration>
</plugin>
```

Full list of parameters you can find at [J2ObjC library home page](http://j2objc.org/docs/j2objc.html).

The example below shows how you can externalize the path to J2ObjC library. Don't forget to add the parameter 
definition to call: `mvn j2objc:convert -DJ2OBJC_DISTRIBUTION=/folder/of/j2obc`.

Sometimes, the code you want to convert to Objective-C contains dependencies, which should be converted as well. 
In this case you have to add all those dependencies into appropriate configuration section:

```
<plugin>
    <groupId>com.smoope.utils</groupId>
    <artifactId>j2objc-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <j2objcPath>${J2OBJC_DISTRIBUTION}/j2objc</j2objcPath>
        <useArc>true</useArc>
        <noPackageDirectories>true</noPackageDirectories>
        <dependencies>
            <dependency>
                <groupId>com.google.code.gson</groupId>
                <artifactId>gson</artifactId>
                <version>2.3.1</version>
            </dependency>
            <dependency>
                <groupId>joda-time</groupId>
                <artifactId>joda-time</artifactId>
                <version>2.8.1</version>
            </dependency>
        </dependencies>
    </configuration>
</plugin>
```

This mechanism doesn't support nested dependencies recognition which means you have to specify every nested dependency 
separately. For example, `joda-time` library depends on `joda-convert`, so in order to have all the necessary dependencies in 
your Xcode project, you have to specify it as well:

```
<plugin>
    <groupId>com.smoope.utils</groupId>
    <artifactId>j2objc-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <j2objcPath>${J2OBJC_DISTRIBUTION}/j2objc</j2objcPath>
        <useArc>true</useArc>
        <noPackageDirectories>true</noPackageDirectories>
        <dependencies>
            <dependency>
                <groupId>com.google.code.gson</groupId>
                <artifactId>gson</artifactId>
                <version>2.3.1</version>
            </dependency>
            <dependency>
                <groupId>org.joda</groupId>
                <artifactId>joda-convert</artifactId>
                <version>1.8</version>
            </dependency>
            <dependency>
                <groupId>joda-time</groupId>
                <artifactId>joda-time</artifactId>
                <version>2.8.1</version>
            </dependency>
        </dependencies>
    </configuration>
</plugin>
```

_Please note, that in order to make it work your local repository must contain the sources version of every dependency._