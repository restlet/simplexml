package org.simpleframework.xml.stream;

import junit.framework.TestCase;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class XxeSecurityTest extends TestCase {

    public void test_when_using_persister_should_fail() throws Exception {
        Serializer serializer = new Persister();

        try {
            serializer.read(MyObject.class, XxeSecurityTest.class.getResourceAsStream("xxeSecurityTest.xml"));
            fail("An exception should be thrown");
        } catch (Exception e) {
            // fine, test is ok
        }
    }
    
    public void test_when_using_streamProvider_should_fail() throws Exception {
        Serializer serializer = new Persister();
        
        try {
            Provider provider = new StreamProvider();
            EventReader eventReader = provider.provide(XxeSecurityTest.class.getResourceAsStream("xxeSecurityTest.xml"));
            InputNode inputNode = new NodeReader(eventReader).readRoot();

            serializer.read(MyObject.class, inputNode);
            fail("An exception should be thrown");
        } catch (Exception e) {
            // fine, test is ok
        }
    }
    

    public void test_when_using_pullProvider_should_fail() throws Exception {
        Serializer serializer = new Persister();

        try {
            Provider provider = new PullProvider();
            EventReader eventReader = provider.provide(XxeSecurityTest.class.getResourceAsStream("xxeSecurityTest.xml"));
            InputNode inputNode = new NodeReader(eventReader).readRoot();

            serializer.read(MyObject.class, inputNode);
            fail("An exception should be thrown");
        } catch (Exception e) {
            // fine, test is ok
        }
    }

    public void test_when_using_documentProvider_should_fail() throws Exception {
        Serializer serializer = new Persister();
        
        try {
            Provider provider = new DocumentProvider();
            EventReader eventReader = provider.provide(XxeSecurityTest.class.getResourceAsStream("xxeSecurityTest.xml"));
            InputNode inputNode = new NodeReader(eventReader).readRoot();

            serializer.read(MyObject.class, inputNode);
            fail("An exception should be thrown");
        } catch (Exception e) {
            // fine, test is ok
        }
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
