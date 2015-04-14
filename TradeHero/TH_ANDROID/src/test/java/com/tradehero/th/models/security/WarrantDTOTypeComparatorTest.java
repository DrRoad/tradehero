package com.tradehero.th.models.security;

import com.tradehero.THRobolectricTestRunner;
import com.tradehero.th.BuildConfig;
import com.tradehero.th.api.security.compact.WarrantDTO;
import java.util.Iterator;
import java.util.TreeSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(THRobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class WarrantDTOTypeComparatorTest
{
    private TreeSet<WarrantDTO> treeSet;

    @Before public void setUp()
    {
        treeSet = new TreeSet<>(new WarrantDTOTypeComparator());
    }

    @After public void tearDown()
    {
    }

    @Test public void nullAtTheEnd1()
    {
        treeSet.add(null);
        treeSet.add(new WarrantDTO());

        Iterator<WarrantDTO> iterator = treeSet.iterator();
        assertNotNull(iterator.next());
        assertNull(iterator.next());
    }

    @Test public void nullAtTheEnd2()
    {
        treeSet.add(new WarrantDTO());
        treeSet.add(null);

        Iterator<WarrantDTO> iterator = treeSet.iterator();
        assertNotNull(iterator.next());
        assertNull(iterator.next());
    }

    @Test public void nullTypeAtTheEnd1()
    {
        WarrantDTO nullType = new WarrantDTO();
        WarrantDTO hasType = new WarrantDTO();
        hasType.warrantType = "C";

        treeSet.add(nullType);
        treeSet.add(hasType);

        Iterator<WarrantDTO> iterator = treeSet.iterator();
        assertEquals("C", iterator.next().warrantType);
        assertNull(iterator.next().warrantType);
    }

    @Test public void nullTypeAtTheEnd2()
    {
        WarrantDTO nullType = new WarrantDTO();
        WarrantDTO hasType = new WarrantDTO();
        hasType.warrantType = "C";

        treeSet.add(hasType);
        treeSet.add(nullType);

        Iterator<WarrantDTO> iterator = treeSet.iterator();
        assertEquals("C", iterator.next().warrantType);
        assertNull(iterator.next().warrantType);
    }

    @Test public void typeAlphaUp1()
    {
        WarrantDTO type1 = new WarrantDTO();
        type1.warrantType = "C";
        WarrantDTO type2 = new WarrantDTO();
        type2.warrantType = "P";

        treeSet.add(type1);
        treeSet.add(type2);

        Iterator<WarrantDTO> iterator = treeSet.iterator();
        assertEquals("C", iterator.next().warrantType);
        assertEquals("P", iterator.next().warrantType);
    }

    @Test public void typeAlphaUp2()
    {
        WarrantDTO type1 = new WarrantDTO();
        type1.warrantType = "C";
        WarrantDTO type2 = new WarrantDTO();
        type2.warrantType = "P";

        treeSet.add(type2);
        treeSet.add(type1);

        Iterator<WarrantDTO> iterator = treeSet.iterator();
        assertEquals("C", iterator.next().warrantType);
        assertEquals("P", iterator.next().warrantType);
    }
}
