#region Using directives
using SimpleFramework.Xml.Core;
using SimpleFramework.Xml.Util;
using SimpleFramework.Xml;
using System.Collections.Generic;
using System;
#endregion
namespace SimpleFramework.Xml.Core {
   public class ScatterTest : ValidationTestCase {
      private const String INLINE_LIST =
      "<test version='ONE'>\n"+
      "   <text name='a' version='ONE'>Example 1</text>\r\n"+
      "   <message>Some example message</message>\r\n"+
      "   <text name='b' version='TWO'>Example 2</text>\r\n"+
      "   <double>1.0</double>\n" +
      "   <double>2.0</double>\n"+
      "   <text name='c' version='THREE'>Example 3</text>\r\n"+
      "   <double>3.0</double>\n"+
      "</test>";
      private const String INLINE_PRIMITIVE_LIST =
      "<test version='ONE'>\n"+
      "   <string>Example 1</string>\r\n"+
      "   <message>Some example message</message>\r\n"+
      "   <string>Example 2</string>\r\n"+
      "   <string>Example 3</string>\r\n"+
      "</test>";
      private const String INLINE_NAMED_LIST =
      "<test version='ONE'>\n"+
      "   <include name='1' file='1.txt'/>\r\n"+
      "   <exclude name='2' file='2.txt'/>\r\n"+
      "   <exclude name='3' file='3.txt'/>\r\n"+
      "   <include name='4' file='4.txt'/>\r\n"+
      "   <exclude name='5' file='5.txt'/>\r\n"+
      "</test>";
      [Root(Name="test")]
      private static class InlineTextList {
         [Element]
         private String message;
         [ElementList(Inline=true)]
         private List<Double> numbers;
         [Transient]
         private List<TextEntry> list;
         [Attribute]
         private Version version;
         public List<Double> Numbers {
            get {
               return numbers;
            }
         }
         //public List<Double> GetNumbers() {
         //   return numbers;
         //}
         public List<TextEntry> List {
            get {
               return list;
            }
            set {
               this.list = new ArrayList<TextEntry>(value); // ensure only set when fully read
            }
         }
         //public void SetList(List<TextEntry> list) {
         //   this.list = new ArrayList<TextEntry>(list); // ensure only set when fully read
         //}
         //public List<TextEntry> GetList() {
         //   return list;
         //}
            return list.Get(index);
         }
      }
      [Root(Name="test")]
      private static class InlinePrimitiveList {
         [Element]
         private String message;
         [ElementList(Inline=true)]
         private List<String> list;
         [Attribute]
         private Version version;
         public String Get(int index) {
            return list.Get(index);
         }
      }
      [Root(Name="text")]
      private static class TextEntry {
         [Attribute]
         private String name;
         [Attribute]
         private Version version;
         [Text]
         private String text;
      }
      [Root]
      private static class SimpleInlineList {
         [ElementList(Inline=true)]
         private ArrayList<SimpleEntry> list = new ArrayList<SimpleEntry>();
      }
      [Root]
      private static class SimpleEntry {
         [Attribute]
         private String content;
      }
      [Root]
      private static class SimplePrimitiveInlineList {
         [ElementList(Inline=true)]
         private ArrayList<String> list = new ArrayList<String>();
      }
      [Root(Name="test")]
      private static class InlineNamedList {
         [ElementList(Inline=true, Entry="include")]
         private Dictionary<FileMatch> includeList;
         [ElementList(Inline=true, Entry="exclude")]
         private Dictionary<FileMatch> excludeList;
         [Attribute]
         private Version version;
         public String GetInclude(String name) {
            FileMatch match = includeList.Get(name);
            if(match != null) {
               return match.file;
            }
            return null;
         }
         public String GetExclude(String name) {
            FileMatch match = excludeList.Get(name);
            if(match != null) {
               return match.file;
            }
            return null;
         }
      }
      [Root]
      private static class FileMatch : Entry {
         [Attribute]
         private String file;
         [Attribute]
         private String name;
         public String Name {
            get {
               return name;
            }
         }
         //public String GetName() {
         //   return name;
         //}
      private static enum Version {
         ONE,
         TWO,
         THREE
      }
      private Persister persister;
      public void SetUp() {
         persister = new Persister();
      }
      public void TestList() {
         InlineTextList list = persister.Read(InlineTextList.class, INLINE_LIST);
         AssertEquals(list.version, Version.ONE);
         AssertEquals(list.message, "Some example message");
         AssertEquals(list.Get(0).version, Version.ONE);
         AssertEquals(list.Get(0).name, "a");
         AssertEquals(list.Get(0).text, "Example 1");
         AssertEquals(list.Get(1).version, Version.TWO);
         AssertEquals(list.Get(1).name, "b");
         AssertEquals(list.Get(1).text, "Example 2");
         AssertEquals(list.Get(2).version, Version.THREE);
         AssertEquals(list.Get(2).name, "c");
         AssertEquals(list.Get(2).text, "Example 3");
         assertTrue(list.Numbers.contains(1.0));
         assertTrue(list.Numbers.contains(2.0));
         assertTrue(list.Numbers.contains(3.0));
         StringWriter buffer = new StringWriter();
         persister.Write(list, buffer);
         Validate(list, persister);
         list = persister.Read(InlineTextList.class, buffer.toString());
         AssertEquals(list.version, Version.ONE);
         AssertEquals(list.message, "Some example message");
         AssertEquals(list.Get(0).version, Version.ONE);
         AssertEquals(list.Get(0).name, "a");
         AssertEquals(list.Get(0).text, "Example 1");
         AssertEquals(list.Get(1).version, Version.TWO);
         AssertEquals(list.Get(1).name, "b");
         AssertEquals(list.Get(1).text, "Example 2");
         AssertEquals(list.Get(2).version, Version.THREE);
         AssertEquals(list.Get(2).name, "c");
         AssertEquals(list.Get(2).text, "Example 3");
         Validate(list, persister);
      }
      public void TestPrimitiveList() {
         InlinePrimitiveList list = persister.Read(InlinePrimitiveList.class, INLINE_PRIMITIVE_LIST);
         AssertEquals(list.version, Version.ONE);
         AssertEquals(list.message, "Some example message");
         AssertEquals(list.Get(0), "Example 1");
         AssertEquals(list.Get(1), "Example 2");
         AssertEquals(list.Get(2), "Example 3");
         StringWriter buffer = new StringWriter();
         persister.Write(list, buffer);
         Validate(list, persister);
         list = persister.Read(InlinePrimitiveList.class, buffer.toString());
         AssertEquals(list.Get(0), "Example 1");
         AssertEquals(list.Get(1), "Example 2");
         AssertEquals(list.Get(2), "Example 3");
         Validate(list, persister);
      }
      public void TestInlineNamedList() {
         InlineNamedList list = persister.Read(InlineNamedList.class, INLINE_NAMED_LIST);
         AssertEquals(list.GetInclude("1"), "1.txt");
         AssertEquals(list.GetInclude("2"), null);
         AssertEquals(list.GetInclude("3"), null);
         AssertEquals(list.GetInclude("4"), "4.txt");
         AssertEquals(list.GetInclude("5"), null);
         AssertEquals(list.GetExclude("1"), null);
         AssertEquals(list.GetExclude("2"), "2.txt");
         AssertEquals(list.GetExclude("3"), "3.txt");
         AssertEquals(list.GetExclude("4"), null);
         AssertEquals(list.GetExclude("5"), "5.txt");
         Validate(list, persister);
      }
      public void TestSimpleList() {
         SimpleInlineList list = new SimpleInlineList();
         SimpleEntry entry = new SimpleEntry();
         entry.content = "test";
         list.list.add(entry);
         Validate(list, persister);
      }
      public void TestSimplePrimitiveList() {
         SimplePrimitiveInlineList list = new SimplePrimitiveInlineList();
         list.list.add("test");
         Validate(list, persister);
      }
   }
}
