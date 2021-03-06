package org.mongodb.morphia.callbacks;


import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.mongodb.morphia.TestBase;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PostPersist;
import org.mongodb.morphia.annotations.Transient;


public class TestMultipleCallbacksPerMethod extends TestBase {
  abstract static class CallbackAbstractEntity {
    @Id
    private final String _id = new ObjectId().toStringMongod();

    public String getId() {
      return _id;
    }

    @Transient
    private boolean persistentMarker;

    public boolean isPersistent() {
      return persistentMarker;
    }

    @PostPersist @PostLoad void markPersistent() {
      persistentMarker = true;
    }
  }

  static class SomeEntity extends CallbackAbstractEntity {

  }

  @Test
  public void testMultipleCallbackAnnotation() throws Exception {
    final SomeEntity entity = new SomeEntity();
    Assert.assertFalse(entity.isPersistent());
    ds.save(entity);
    Assert.assertTrue(entity.isPersistent());
    final SomeEntity reloaded = ds.find(SomeEntity.class, "_id", entity.getId()).get();
    Assert.assertTrue(reloaded.isPersistent());
  }
}