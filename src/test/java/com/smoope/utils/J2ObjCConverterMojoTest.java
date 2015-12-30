package com.smoope.utils;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

public class J2ObjCConverterMojoTest {

    @Rule
    public MojoRule rule = new MojoRule() {

        @Override
        protected void before() throws Throwable {
            super.before();
        }

        @Override
        protected void after() {
            super.after();
        }
    };

    @Rule
    public TestResources resources = new TestResources();

    @Ignore
    @Test
    public void testExecute() throws Exception {
        File project = resources.getBasedir("valid");
        File pom = new File(project, "pom.xml");
        Assert.assertNotNull(pom);
        Assert.assertTrue(pom.exists());

        J2ObjCConverterMojo mojo = (J2ObjCConverterMojo) rule.lookupMojo("convert", pom);
        Assert.assertNotNull(mojo);
        mojo.execute();
    }
}
