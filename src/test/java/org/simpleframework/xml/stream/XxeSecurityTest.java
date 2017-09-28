package org.simpleframework.xml.stream;

import org.junit.Assert;
import org.junit.Test;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.core.ValueRequiredException;
import org.xmlpull.v1.XmlPullParserException;

public class XxeSecurityTest {

    @Test
    public void when_using_general_persister_should_fail() throws Exception {
        Serializer serializer = new Persister();

        try {
            serializer.read(MyObject.class, XxeSecurityTest.class.getResourceAsStream("xxeSecurityTest.xml"));
            Assert.fail("An exception should be thrown");
        } catch (Exception e) {
            // fine, test is ok
        }
    }

    @Test(expected = ValueRequiredException.class)
    public void when_using_streamProvider_should_fail() throws Exception {
        checkProvider(new StreamProvider());
    }

    @Test(expected = XmlPullParserException.class)
    public void when_using_pullProvider_should_fail() throws Exception {
        checkProvider(new PullProvider());
    }

    @Test(expected = RuntimeException.class)
    public void when_using_documentProvider_should_fail() throws Exception {
        checkProvider(new DocumentProvider());
    }

    private void checkProvider(Provider provider) throws Exception {
        Serializer serializer = new Persister();

        EventReader eventReader = provider.provide(XxeSecurityTest.class.getResourceAsStream("xxeSecurityTest.xml"));
        InputNode inputNode = new NodeReader(eventReader).readRoot();

        serializer.read(MyObject.class, inputNode);
    }

    @Root
    public static class MyObject {

        @Element
        public String message;

        public MyObject() {
            this.message = "aaa";
        }

        public MyObject(String message) {
            this.message = message;
        }
    }
}
