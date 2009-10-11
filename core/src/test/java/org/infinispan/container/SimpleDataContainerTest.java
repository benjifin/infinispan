package org.infinispan.container;

import org.infinispan.container.entries.ImmortalCacheEntry;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.container.entries.MortalCacheEntry;
import org.infinispan.container.entries.TransientCacheEntry;
import org.infinispan.container.entries.TransientMortalCacheEntry;
import org.infinispan.test.AbstractInfinispanTest;
import org.infinispan.util.Immutables;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Test(groups = "unit", testName = "container.SimpleDataContainerTest")
public class SimpleDataContainerTest extends AbstractInfinispanTest {
   DataContainer dc;

   @BeforeMethod
   public void setUp() {
      dc = createContainer();
   }

   @AfterMethod
   public void tearDown() {
      dc = null;
   }

   protected DataContainer createContainer() {
      return new SimpleDataContainer(16);
   }

   public void testExpiredData() throws InterruptedException {
      dc.put("k", "v", -1, 6000000);
      Thread.sleep(100);

      InternalCacheEntry entry = dc.get("k");
      assert entry instanceof TransientCacheEntry;
      assert entry.getLastUsed() <= System.currentTimeMillis();
      long entryLastUsed = entry.getLastUsed();
      Thread.sleep(100);
      entry = dc.get("k");
      assert entry.getLastUsed() > entryLastUsed;
      dc.put("k", "v", -1, 0);
      dc.purgeExpired();

      dc.put("k", "v", 6000000, -1);
      Thread.sleep(100);
      assert dc.size() == 1;

      entry = dc.get("k");
      assert entry != null : "Entry should not be null!";
      assert entry instanceof MortalCacheEntry : "Expected MortalCacheEntry, was " + entry.getClass().getSimpleName();
      assert entry.getCreated() <= System.currentTimeMillis();

      dc.put("k", "v", 0, -1);

      assert dc.get("k") == null;
      assert dc.size() == 0;

      dc.put("k", "v", 0, -1);
      Thread.sleep(100);
      assert dc.size() == 1;
      dc.purgeExpired();
      assert dc.size() == 0;
   }

   public void testUpdatingLastUsed() throws Exception {
      long idle = 600000;
      dc.put("k", "v", -1, -1);
      InternalCacheEntry ice = dc.get("k");
      assert ice instanceof ImmortalCacheEntry;
      assert ice.getExpiryTime() == -1;
      assert ice.getLastUsed() == -1;
      assert ice.getCreated() == -1;
      assert ice.getMaxIdle() == -1;
      assert ice.getLifespan() == -1;
      dc.put("k", "v", -1, idle);
      long oldTime = System.currentTimeMillis();
      Thread.sleep(100); // for time calc granularity
      ice = dc.get("k");
      assert ice instanceof TransientCacheEntry;
      assert ice.getExpiryTime() == -1;
      assert ice.getLastUsed() > oldTime;
      Thread.sleep(100); // for time calc granularity
      assert ice.getLastUsed() < System.currentTimeMillis();
      assert ice.getMaxIdle() == idle;
      assert ice.getCreated() == -1;
      assert ice.getLifespan() == -1;

      oldTime = System.currentTimeMillis();
      Thread.sleep(100); // for time calc granularity
      assert dc.get("k") != null;

      // check that the last used stamp has been updated on a get
      assert ice.getLastUsed() > oldTime;
      Thread.sleep(100); // for time calc granularity
      assert ice.getLastUsed() < System.currentTimeMillis();
   }

   public void testExpirableToImmortalAndBack() {
      dc.put("k", "v", 6000000, -1);
      assert dc.containsKey("k");
      assert dc.get("k") instanceof MortalCacheEntry;

      dc.put("k", "v2", -1, -1);
      assert dc.containsKey("k");
      assert dc.get("k") instanceof ImmortalCacheEntry;

      dc.put("k", "v3", -1, 6000000);
      assert dc.containsKey("k");
      assert dc.get("k") instanceof TransientCacheEntry;

      dc.put("k", "v3", 6000000, 6000000);
      assert dc.containsKey("k");
      assert dc.get("k") instanceof TransientMortalCacheEntry;

      dc.put("k", "v", 6000000, -1);
      assert dc.containsKey("k");
      assert dc.get("k") instanceof MortalCacheEntry;
   }

   public void testKeySet() {
      dc.put("k1", "v", 6000000, -1);
      dc.put("k2", "v", -1, -1);
      dc.put("k3", "v", -1, 6000000);
      dc.put("k4", "v", 6000000, 6000000);

      Set expected = new HashSet();
      expected.add("k1");
      expected.add("k2");
      expected.add("k3");
      expected.add("k4");

      for (Object o : dc.keySet()) assert expected.remove(o);

      assert expected.isEmpty() : "Did not see keys " + expected + " in iterator!";
   }

   public void testContainerIteration() {
      dc.put("k1", "v", 6000000, -1);
      dc.put("k2", "v", -1, -1);
      dc.put("k3", "v", -1, 6000000);
      dc.put("k4", "v", 6000000, 6000000);

      Set expected = new HashSet();
      expected.add("k1");
      expected.add("k2");
      expected.add("k3");
      expected.add("k4");

      for (InternalCacheEntry ice : dc) {
         assert expected.remove(ice.getKey());
      }

      assert expected.isEmpty() : "Did not see keys " + expected + " in iterator!";
   }
   
   public void testKeys() {
      dc.put("k1", "v1", 6000000, -1);
      dc.put("k2", "v2", -1, -1);
      dc.put("k3", "v3", -1, 6000000);
      dc.put("k4", "v4", 6000000, 6000000);

      Set expected = new HashSet();
      expected.add("k1");
      expected.add("k2");
      expected.add("k3");
      expected.add("k4");

      for (Object o : dc.keySet()) assert expected.remove(o);

      assert expected.isEmpty() : "Did not see keys " + expected + " in iterator!";
   }

   public void testValues() {
      dc.put("k1", "v1", 6000000, -1);
      dc.put("k2", "v2", -1, -1);
      dc.put("k3", "v3", -1, 6000000);
      dc.put("k4", "v4", 6000000, 6000000);

      Set expected = new HashSet();
      expected.add("v1");
      expected.add("v2");
      expected.add("v3");
      expected.add("v4");

      for (Object o : dc.values()) assert expected.remove(o);

      assert expected.isEmpty() : "Did not see keys " + expected + " in iterator!";
   }

   public void testEntrySet() {
      dc.put("k1", "v1", 6000000, -1);
      dc.put("k2", "v2", -1, -1);
      dc.put("k3", "v3", -1, 6000000);
      dc.put("k4", "v4", 6000000, 6000000);

      Set expected = new HashSet();
      expected.add(Immutables.immutableInternalCacheEntry(dc.get("k1")));
      expected.add(Immutables.immutableInternalCacheEntry(dc.get("k2")));
      expected.add(Immutables.immutableInternalCacheEntry(dc.get("k3")));
      expected.add(Immutables.immutableInternalCacheEntry(dc.get("k4")));

      for (Map.Entry o : dc.entrySet()) assert expected.remove(o);

      assert expected.isEmpty() : "Did not see keys " + expected + " in iterator!";
   }
}
