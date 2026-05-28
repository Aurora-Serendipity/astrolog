package com.astrolog.dao;

import com.astrolog.model.CelestialBody;
import com.astrolog.model.enums.BodyType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BodyDao extends BaseDao<CelestialBody> {

    public CelestialBody findById(int bodyId) {
        String sql = "SELECT body_id, name, type, constellation, ra_h, ra_m, "
                   + "dec_deg, dec_min, magnitude, distance_ly, messier_number, "
                   + "ngc_number, best_season, description, image_path "
                   + "FROM celestial_bodies WHERE body_id = ?";
        List<CelestialBody> results = executeQuery(sql, new Object[]{bodyId}, this::mapRow);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<CelestialBody> findAll() {
        String sql = "SELECT body_id, name, type, constellation, ra_h, ra_m, "
                   + "dec_deg, dec_min, magnitude, distance_ly, messier_number, "
                   + "ngc_number, best_season, description, image_path "
                   + "FROM celestial_bodies ORDER BY name";
        return executeQuery(sql, null, this::mapRow);
    }

    public int insert(CelestialBody body) {
        String sql = "INSERT INTO celestial_bodies (name, type, constellation, "
                   + "ra_h, ra_m, dec_deg, dec_min, magnitude, distance_ly, "
                   + "messier_number, ngc_number, best_season, description, image_path) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return executeInsert(sql, new Object[]{
            body.getName(),
            body.getType().name().toLowerCase(),
            body.getConstellation(),
            body.getRaH(), body.getRaM(),
            body.getDecDeg(), body.getDecMin(),
            body.getMagnitude(), body.getDistanceLy(),
            body.getMessierNumber(), body.getNgcNumber(),
            body.getBestSeason(), body.getDescription(),
            body.getImagePath()});
    }

    public boolean update(CelestialBody body) {
        String sql = "UPDATE celestial_bodies SET name=?, type=?, constellation=?, "
                   + "ra_h=?, ra_m=?, dec_deg=?, dec_min=?, magnitude=?, "
                   + "distance_ly=?, messier_number=?, ngc_number=?, "
                   + "best_season=?, description=?, image_path=? "
                   + "WHERE body_id=?";
        return executeUpdate(sql, new Object[]{
            body.getName(),
            body.getType().name().toLowerCase(),
            body.getConstellation(),
            body.getRaH(), body.getRaM(),
            body.getDecDeg(), body.getDecMin(),
            body.getMagnitude(), body.getDistanceLy(),
            body.getMessierNumber(), body.getNgcNumber(),
            body.getBestSeason(), body.getDescription(),
            body.getImagePath(),
            body.getBodyId()}) > 0;
    }

    public boolean delete(int bodyId) {
        String delFav = "DELETE FROM user_favorites WHERE body_id = ?";
        executeUpdate(delFav, new Object[]{bodyId});
        String sql = "DELETE FROM celestial_bodies WHERE body_id = ?";
        return executeUpdate(sql, new Object[]{bodyId}) > 0;
    }

    public List<CelestialBody> search(String constellation, String type,
                                       BigDecimal minMag, BigDecimal maxMag,
                                       String season, String keyword) {
        StringBuilder sql = new StringBuilder(
            "SELECT body_id, name, type, constellation, ra_h, ra_m, "
          + "dec_deg, dec_min, magnitude, distance_ly, messier_number, "
          + "ngc_number, best_season, description, image_path "
          + "FROM celestial_bodies WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (constellation != null && !constellation.isEmpty()) {
            sql.append(" AND constellation = ?");
            params.add(constellation);
        }
        if (type != null && !type.isEmpty()) {
            sql.append(" AND type = ?");
            params.add(type.toLowerCase());
        }
        if (minMag != null) {
            sql.append(" AND magnitude >= ?");
            params.add(minMag);
        }
        if (maxMag != null) {
            sql.append(" AND magnitude <= ?");
            params.add(maxMag);
        }
        if (season != null && !season.isEmpty()) {
            sql.append(" AND best_season = ?");
            params.add(season);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ?)");
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
        }

        sql.append(" ORDER BY name");
        return executeQuery(sql.toString(), params.toArray(), this::mapRow);
    }

    public List<CelestialBody> findByPopularity() {
        String sql = "SELECT cb.body_id, cb.name, cb.type, cb.constellation, "
                   + "cb.ra_h, cb.ra_m, cb.dec_deg, cb.dec_min, cb.magnitude, "
                   + "cb.distance_ly, cb.messier_number, cb.ngc_number, "
                   + "cb.best_season, cb.description, cb.image_path, "
                   + "COUNT(uf.body_id) as fav_count "
                   + "FROM celestial_bodies cb "
                   + "LEFT JOIN user_favorites uf ON cb.body_id = uf.body_id "
                   + "GROUP BY cb.body_id "
                   + "ORDER BY fav_count DESC";
        return executeQuery(sql, null, this::mapRow);
    }

    CelestialBody mapRow(ResultSet rs) throws SQLException {
        CelestialBody b = new CelestialBody();
        b.setBodyId(rs.getInt("body_id"));
        b.setName(rs.getString("name"));
        b.setType(BodyType.fromString(rs.getString("type")));
        b.setConstellation(rs.getString("constellation"));
        b.setRaH(rs.getInt("ra_h"));
        b.setRaM(rs.getInt("ra_m"));
        b.setDecDeg(rs.getInt("dec_deg"));
        b.setDecMin(rs.getInt("dec_min"));
        b.setMagnitude(rs.getBigDecimal("magnitude"));
        b.setDistanceLy(rs.getBigDecimal("distance_ly"));
        int mNum = rs.getInt("messier_number");
        b.setMessierNumber(rs.wasNull() ? null : mNum);
        int nNum = rs.getInt("ngc_number");
        b.setNgcNumber(rs.wasNull() ? null : nNum);
        b.setBestSeason(rs.getString("best_season"));
        b.setDescription(rs.getString("description"));
        b.setImagePath(rs.getString("image_path"));
        return b;
    }
}
