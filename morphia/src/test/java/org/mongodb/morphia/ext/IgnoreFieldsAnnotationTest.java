/**
 * Copyright (C) 2010 Olafur Gauti Gudmundsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.mongodb.morphia.ext;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.TestBase;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.mapping.MappedClass;
import org.mongodb.morphia.mapping.MappedField;


/**
 * @author Scott Hernandez
 */
public class IgnoreFieldsAnnotationTest extends TestBase {

  Transient transAnn;

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.TYPE })
  @interface IgnoreFields {
    String value();
  }

  @Entity
  @IgnoreFields("ignored")
  static class User {
    @Id ObjectId id;
    String email;
    String ignored = "never, never";
  }

  @Override
  @Before
  public void setUp() {
    super.setUp();
    MappedClass.interestingAnnotations.add(IgnoreFields.class);
    morphia.map(User.class);
    processIgnoreFieldsAnnotations();
  }

  //remove any MappedField specified in @IgnoreFields on the class.
  void processIgnoreFieldsAnnotations() {
    final DatastoreImpl dsi = (DatastoreImpl) ds;
    for (final MappedClass mc : dsi.getMapper().getMappedClasses()) {
      final IgnoreFields ignores = (IgnoreFields) mc.getAnnotation(IgnoreFields.class);
      if (ignores != null) {
        for (final String field : ignores.value().split(",")) {
          final MappedField mf = mc.getMappedFieldByJavaField(field);
          mc.getPersistenceFields().remove(mf);
        }
      }
    }
  }

  @Test
  public void testIt() {
    final User u = new User();
    u.email = "ScottHernandez@gmail.com";
    u.ignored = "test";
    ds.save(u);

    final User uLoaded = ds.find(User.class).get();
    Assert.assertEquals("never, never", uLoaded.ignored);
  }
}
