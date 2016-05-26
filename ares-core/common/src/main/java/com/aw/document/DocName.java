package com.aw.document;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Links the document name to a property within a body object. e.g.
 *
 * <code><pre>
 *
 * class MyPojo {
 *
 * 		@DocName
 * 		public String getBoundName() { return boundName; }
 * 		public void setBoundName(String docName) { this.boundName = docName; }
 * 		private String boundName;
 *
 * }
 *
 *
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DocName {

}
