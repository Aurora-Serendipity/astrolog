package com.astrolog.integration.dao;

import com.astrolog.dao.TagDao;
import com.astrolog.model.ObservationTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagDaoTest {

    private TagDao tagDao;
    private int tagId;

    @BeforeEach
    void setUp() {
        tagDao = new TagDao();
    }

    @AfterEach
    void tearDown() {
        if (tagId > 0) {
            tagDao.delete(tagId);
        }
    }

    // IT-TD-001
    @Test
    void getOrCreate_newTag_createsAndReturnsId() {
        tagId = tagDao.getOrCreate("集成测试标签", "#00FF00");
        assertTrue(tagId > 0);

        ObservationTag tag = tagDao.findByName("集成测试标签");
        assertNotNull(tag);
        assertEquals("集成测试标签", tag.getName());
        assertEquals("#00FF00", tag.getColor());
    }

    // IT-TD-002
    @Test
    void getOrCreate_existingTag_returnsExistingId() {
        tagId = tagDao.getOrCreate("重复标签", "#0000FF");
        int sameId = tagDao.getOrCreate("重复标签", "#FF0000");

        assertEquals(tagId, sameId);

        ObservationTag tag = tagDao.findByName("重复标签");
        assertEquals("#0000FF", tag.getColor());
    }

    // IT-TD-003
    @Test
    void delete_removesTag() {
        tagId = tagDao.getOrCreate("待删除标签", "#3366CC");
        assertTrue(tagDao.delete(tagId));

        ObservationTag deleted = tagDao.findByName("待删除标签");
        assertNull(deleted);
        tagId = 0;
    }
}
