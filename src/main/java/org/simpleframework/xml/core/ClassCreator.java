package org.simpleframework.xml.core;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

class ClassCreator implements Creator {
   
   private final ParameterMap map;
   private final List<Builder> list;
   private final Builder primary;
   
   public ClassCreator(List<Builder> list, ParameterMap map, Builder primary) {
      this.primary = primary;
      this.list = list;
      this.map = map;
   }
   
   public Object getInstance() throws Exception {
      return primary.getDefault();
   }

   public Object getInstance(Criteria criteria) throws Exception {
      return getBuilder(criteria).build(criteria);
   }
   
   public boolean isDefault() {
      return primary != null;
   }
   
   /**
    * This is used to acquire the <code>Parameter</code> object that
    * has the provided name. This is used to validate the annotated
    * methods and fields against the annotated constructor parameters.
    * 
    * @return this returns the parameter for the specified name
    */
   public Parameter getParameter(String name) {
      return map.get(name);
   }
   
   /**
    * This is used to acquire a <code>Builder</code> which is used
    * to instantiate the object. If there is no match for the builder
    * then the default constructor is provided.
    * 
    * @param names the names of the parameters to be matched
    * 
    * @return this returns the builder that has been matched
    */
   private Builder getBuilder(Criteria criteria) {
      PriorityQueue<Rank> queue = new PriorityQueue<Rank>();
      
      for(Builder builder : list) {
         queue.add(new Rank(criteria, builder));
      }
      return  queue.remove().getBuilder();
   }

   /**
    * This is used to acquire all of the builders for the class. It
    * is used to validate the schema and ensure that the annotations
    * describe a fully serializable and deserializable class.
    * 
    * @return this returns the builders for this class schema
    */
   public List<Builder> getBuilders() {
      return list;
   }
   
   private class Rank implements Comparable<Rank> {
      
      private final Criteria criteria;
      private final Builder builder;
      
      public Rank(Criteria criteria, Builder builder) {
         this.builder = builder;
         this.criteria = criteria;
      }
      
      public int compareTo(Rank rank) {
         try {
            return rank.builder.score(criteria) - builder.score(criteria) ;
         } catch(Exception e) {
            throw new IllegalStateException(e);
         }
      }
      
      public Builder getBuilder() {
         return builder;
      }
   }

}
