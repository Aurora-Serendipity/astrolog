package com.astrolog.dao;

import com.astrolog.model.ObservationSite;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SiteDao extends BaseDao<ObservationSite> {

    public ObservationSite findById(int siteId) {
        String sql = "SELECT site_id, user_id, name, latitude, longitude, "
                   + "altitude, bortle_scale, best_time "
                   + "FROM observation_sites WHERE site_id = ?";
        List<ObservationSite> results = executeQuery(sql, new Object[]{siteId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<ObservationSite> findAllByUserId(int userId) {
        String sql = "SELECT site_id, user_id, name, latitude, longitude, "
                   + "altitude, bortle_scale, best_time "
                   + "FROM observation_sites WHERE user_id = ? ORDER BY name";
        return executeQuery(sql, new Object[]{userId}, this::mapRow);
    }

    public int insert(ObservationSite site) {
        String sql = "INSERT INTO observation_sites (user_id, name, latitude, "
                   + "longitude, altitude, bortle_scale, best_time) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            site.getUserId(), site.getName(),
            site.getLatitude(), site.getLongitude(),
            site.getAltitude(), site.getBortleScale(), site.getBestTime()});
    }

    public boolean update(ObservationSite site) {
        String sql = "UPDATE observation_sites SET name=?, latitude=?, longitude=?, "
                   + "altitude=?, bortle_scale=?, best_time=? WHERE site_id=?";
        return executeUpdate(sql, new Object[]{
            site.getName(), site.getLatitude(), site.getLongitude(),
            site.getAltitude(), site.getBortleScale(), site.getBestTime(),
            site.getSiteId()}) > 0;
    }

    public boolean delete(int siteId) {
        String sql = "DELETE FROM observation_sites WHERE site_id = ?";
        return executeUpdate(sql, new Object[]{siteId}) > 0;
    }

    private ObservationSite mapRow(ResultSet rs) throws SQLException {
        ObservationSite s = new ObservationSite();
        s.setSiteId(rs.getInt("site_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setName(rs.getString("name"));
        s.setLatitude(rs.getBigDecimal("latitude"));
        s.setLongitude(rs.getBigDecimal("longitude"));
        s.setAltitude(rs.getInt("altitude"));
        s.setBortleScale(rs.getInt("bortle_scale"));
        s.setBestTime(rs.getString("best_time"));
        return s;
    }
}
