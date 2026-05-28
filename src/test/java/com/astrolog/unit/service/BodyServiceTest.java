package com.astrolog.unit.service;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.FavoriteDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.enums.BodyType;
import com.astrolog.service.BodyService;
import com.astrolog.service.ImportResult;
import com.astrolog.service.ServiceResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BodyServiceTest {

    @Mock
    private BodyDao bodyDao;

    @Mock
    private FavoriteDao favoriteDao;

    @InjectMocks
    private BodyService bodyService;

    // BS-001
    @Test
    void addBody_success() {
        CelestialBody body = createValidBody("测试恒星");
        when(bodyDao.insert(any(CelestialBody.class))).thenReturn(1);

        ServiceResult sr = bodyService.addBody(body, 1);
        assertTrue(sr.isSuccess());
        assertTrue(sr.getMessage().contains("ID: 1"));
        verify(bodyDao).insert(any(CelestialBody.class));
    }

    // BS-002
    @Test
    void addBody_emptyName() {
        CelestialBody body = createValidBody("");
        body.setName(null);

        ServiceResult sr = bodyService.addBody(body, 1);
        assertFalse(sr.isSuccess());
        assertTrue(sr.getMessage().contains("名称"));
        verify(bodyDao, never()).insert(any());
    }

    // BS-003
    @Test
    void addBody_invalidRA() {
        CelestialBody body = createValidBody("测试");
        body.setRaH(25);

        ServiceResult sr = bodyService.addBody(body, 1);
        assertFalse(sr.isSuccess());
        assertTrue(sr.getMessage().contains("赤经"));
    }

    // BS-004
    @Test
    void addBody_invalidDec() {
        CelestialBody body = createValidBody("测试");
        body.setDecDeg(95);

        ServiceResult sr = bodyService.addBody(body, 1);
        assertFalse(sr.isSuccess());
        assertTrue(sr.getMessage().contains("赤纬"));
    }

    // BS-005
    @Test
    void updateBody_success() {
        CelestialBody body = createValidBody("更新测试");
        body.setBodyId(1);
        when(bodyDao.update(any(CelestialBody.class))).thenReturn(true);

        ServiceResult sr = bodyService.updateBody(body, 1);
        assertTrue(sr.isSuccess());
        verify(bodyDao).update(any(CelestialBody.class));
    }

    // BS-006
    @Test
    void deleteBody_withFavorites() {
        when(bodyDao.delete(1)).thenReturn(true);

        ServiceResult sr = bodyService.deleteBody(1, 1);
        assertTrue(sr.isSuccess());
        verify(bodyDao).delete(1);
    }

    // BS-007
    @Test
    void toggleFavorite_addThenRemove() {
        when(favoriteDao.exists(1, 10)).thenReturn(false);
        when(favoriteDao.add(1, 10)).thenReturn(true);

        ServiceResult sr = bodyService.toggleFavorite(1, 10);
        assertTrue(sr.isSuccess());
        assertTrue(sr.getMessage().contains("已添加"));
        verify(favoriteDao).add(1, 10);

        when(favoriteDao.exists(1, 10)).thenReturn(true);
        when(favoriteDao.remove(1, 10)).thenReturn(true);

        sr = bodyService.toggleFavorite(1, 10);
        assertTrue(sr.isSuccess());
        assertTrue(sr.getMessage().contains("已取消"));
        verify(favoriteDao).remove(1, 10);
    }

    // BS-008
    @Test
    void search_multipleConditions() {
        when(bodyDao.search(eq("猎户座"), eq("star"), isNull(), isNull(), eq("冬"), isNull()))
            .thenReturn(List.of(createValidBody("M42")));

        List<CelestialBody> results = bodyService.search("猎户座", "star",
            null, null, "冬", null);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    // BS-009
    @Test
    void importCsv_validFile() {
        String csv = "name,type,constellation,ra,dec,mag,season\n"
                   + "测试星,恒星,猎户座,5h35m,-5°23',4.0,冬\n"
                   + "测试星系,星系,仙女座,0h42m,41°16',3.44,秋";
        when(bodyDao.insert(any(CelestialBody.class))).thenReturn(1, 2);

        ImportResult result = bodyService.importCsv(csv, 1);
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getErrorCount());
        verify(bodyDao, times(2)).insert(any(CelestialBody.class));
    }

    // BS-010
    @Test
    void importCsv_invalidFormat() {
        String csv = "name,type,constellation,ra,dec\n"
                   + "行1\n"
                   + "测试,恒星,猎户,invalid,mag";

        ImportResult result = bodyService.importCsv(csv, 1);
        assertTrue(result.hasErrors());
        assertTrue(result.getErrorCount() > 0);
    }

    // BS-011
    @Test
    void isFavorited_returnsCorrectValue() {
        when(favoriteDao.exists(1, 10)).thenReturn(true);
        assertTrue(bodyService.isFavorited(1, 10));

        when(favoriteDao.exists(1, 20)).thenReturn(false);
        assertFalse(bodyService.isFavorited(1, 20));
    }

    // BS-012
    @Test
    void getFavorites_returnsList() {
        when(favoriteDao.findBodiesByUser(1))
            .thenReturn(List.of(createValidBody("收藏星体1"), createValidBody("收藏星体2")));

        List<CelestialBody> favs = bodyService.getFavorites(1);
        assertEquals(2, favs.size());
    }

    // BS-013
    @Test
    void listByPopularity_delegatesToDao() {
        when(bodyDao.findByPopularity())
            .thenReturn(List.of(createValidBody("热门星体")));

        List<CelestialBody> bodies = bodyService.listByPopularity();
        assertEquals(1, bodies.size());
        verify(bodyDao).findByPopularity();
    }

    // BS-014
    @Test
    void importCsv_decimalRADecFormat() {
        String csv = "name,type,constellation,ra,dec,mag\n"
                   + "测试,恒星,猎户座,5.58,-5.38,1.5";
        when(bodyDao.insert(any(CelestialBody.class))).thenReturn(1);

        ImportResult result = bodyService.importCsv(csv, 1);
        assertEquals(1, result.getSuccessCount());
        verify(bodyDao).insert(any(CelestialBody.class));
    }

    // ==================== 辅助方法 ====================

    private CelestialBody createValidBody(String name) {
        CelestialBody body = new CelestialBody();
        body.setName(name);
        body.setType(BodyType.STAR);
        body.setConstellation("猎户座");
        body.setRaH(5);
        body.setRaM(35);
        body.setDecDeg(-5);
        body.setDecMin(23);
        body.setMagnitude(new BigDecimal("4.0"));
        body.setBestSeason("冬");
        return body;
    }
}
