package com.astrolog.unit.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.ObsDao;
import com.astrolog.model.MessierObject;
import com.astrolog.service.MessierService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MessierServiceTest {

    @Mock
    private BodyDao bodyDao;

    @Mock
    private ObsDao obsDao;

    private MessierService messierService;

    @BeforeEach
    void setUp() {
        messierService = new MessierService();
        injectMocks();
    }

    private void injectMocks() {
        try {
            var bodyDaoField = MessierService.class.getDeclaredField("bodyDao");
            bodyDaoField.setAccessible(true);
            bodyDaoField.set(messierService, bodyDao);

            var obsDaoField = MessierService.class.getDeclaredField("obsDao");
            obsDaoField.setAccessible(true);
            obsDaoField.set(messierService, obsDao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetFullCatalog() {
        List<MessierObject> catalog = messierService.getFullCatalog();
        assertEquals(110, catalog.size(), "应返回全部110个梅西耶天体");

        Set<Integer> numbers = new HashSet<>();
        for (MessierObject m : catalog) {
            assertTrue(m.getMessierNumber() >= 1 && m.getMessierNumber() <= 110,
                "编号应在1-110范围内");
            assertNotNull(m.getName());
            assertNotNull(m.getType());
            assertNotNull(m.getConstellation());
            assertNotNull(m.getMagnitude());
            assertNotNull(m.getSeason());
            assertNotNull(m.getDescription());
            numbers.add(m.getMessierNumber());
        }
        assertEquals(110, numbers.size(), "应有110个不重复编号");
    }

    @Test
    void testFilterByTypeNebula() {
        List<MessierObject> nebulae = messierService.filterByType("星云");
        assertFalse(nebulae.isEmpty(), "应有星云类型天体");
        for (MessierObject m : nebulae) {
            assertEquals("星云", m.getType());
        }
    }

    @Test
    void testFilterBySeasonWinter() {
        List<MessierObject> winter = messierService.filterBySeason("冬");
        assertFalse(winter.isEmpty(), "应有冬季天体");
        for (MessierObject m : winter) {
            assertEquals("冬", m.getSeason());
        }
    }

    @Test
    void testGetProgress() {
        Set<Integer> observed = new HashSet<>(Arrays.asList(1, 31, 42, 45));
        double progress = messierService.getProgress(observed);
        assertEquals(4.0 / 110.0 * 100.0, progress, 0.01);
    }

    @Test
    void testIsCertEligible() {
        Set<Integer> allObserved = new HashSet<>();
        for (int i = 1; i <= 110; i++) {
            allObserved.add(i);
        }
        assertTrue(messierService.isCertEligible(allObserved));

        Set<Integer> partial = new HashSet<>(Arrays.asList(1, 2, 3));
        assertFalse(messierService.isCertEligible(partial));
    }
}
