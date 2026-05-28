package com.astrolog.dao;

import com.astrolog.model.ObservationTag;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TagDao extends BaseDao<ObservationTag> {

    public List<ObservationTag> findAll() {
        String sql = "SELECT tag_id, name, color FROM observation_tags ORDER BY name";
        return executeQuery(sql, null, this::mapRow);
    }

    public ObservationTag findByName(String name) {
        String sql = "SELECT tag_id, name, color FROM observation_tags WHERE name = ?";
        List<ObservationTag> results = executeQuery(sql, new Object[]{name}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public int getOrCreate(String name, String color) {
        ObservationTag existing = findByName(name);
        if (existing != null) {
            return existing.getTagId();
        }
        String sql = "INSERT INTO observation_tags (name, color) VALUES (?, ?)";
        return executeInsert(sql, new Object[]{name, color});
    }

    public boolean update(int tagId, String name, String color) {
        String sql = "UPDATE observation_tags SET name=?, color=? WHERE tag_id=?";
        return executeUpdate(sql, new Object[]{name, color, tagId}) > 0;
    }

    public boolean delete(int tagId) {
        executeUpdate("DELETE FROM obs_tag_relation WHERE tag_id = ?",
            new Object[]{tagId});
        String sql = "DELETE FROM observation_tags WHERE tag_id = ?";
        return executeUpdate(sql, new Object[]{tagId}) > 0;
    }

    public List<ObservationTag> findByObsId(int obsId) {
        String sql = "SELECT ot.tag_id, ot.name, ot.color "
                   + "FROM observation_tags ot "
                   + "JOIN obs_tag_relation otr ON ot.tag_id = otr.tag_id "
                   + "WHERE otr.obs_id = ? ORDER BY ot.name";
        return executeQuery(sql, new Object[]{obsId}, this::mapRow);
    }

    private ObservationTag mapRow(ResultSet rs) throws SQLException {
        ObservationTag t = new ObservationTag();
        t.setTagId(rs.getInt("tag_id"));
        t.setName(rs.getString("name"));
        t.setColor(rs.getString("color"));
        return t;
    }
}
