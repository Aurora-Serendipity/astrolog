package com.astrolog.service;

import com.astrolog.model.User;
import com.astrolog.util.ExportUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ExportService {

    private final StatsService statsService;
    private final MessierService messierService;

    private static final Path REPORTS_DIR = initReportsDir();

    private static Path initReportsDir() {
        String override = System.getProperty("astrolog.reports.dir");
        if (override != null) return Paths.get(override);
        return Paths.get(System.getProperty("user.home"), "AstroLog", "reports");
    }

    public static Path getReportsDir() { return REPORTS_DIR; }

    public ExportService() {
        this.statsService = new StatsService();
        this.messierService = new MessierService();
    }

    ExportService(StatsService statsService, MessierService messierService) {
        this.statsService = statsService;
        this.messierService = messierService;
    }

    public String buildHtmlContent(User user, int year) {
        int userId = user.getUserId();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
           .append("<title>AstroLog 年度观测报告 ").append(year).append("</title>")
           .append("<style>body{font-family:'Microsoft YaHei',sans-serif;")
           .append("max-width:800px;margin:40px auto;color:#333;}")
           .append("h1{color:#1a5276;}h2{color:#2e86c1;margin-top:24px;}")
           .append("table{border-collapse:collapse;width:100%;margin:16px 0;}")
           .append("th,td{border:1px solid #ccc;padding:8px 12px;text-align:left;}")
           .append("th{background:#f0f4f8;}.stat{font-size:24px;font-weight:bold;color:#2e86c1;}")
           .append("</style></head><body>");

        html.append("<h1>AstroLog 年度观测报告</h1>")
           .append("<p>观测者: ").append(escapeHtml(user.getUsername()))
           .append(" | 年度: ").append(year)
           .append(" | 生成时间: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
           .append("</p><hr>");

        long total = statsService.totalObservations(userId);
        long distinctBodies = statsService.distinctBodiesObserved(userId);
        long totalEquip = statsService.totalEquipmentUsed(userId);

        html.append("<h2>年度摘要</h2>")
           .append("<p><span class='stat'>").append(total).append("</span> 次观测</p>")
           .append("<p><span class='stat'>").append(distinctBodies).append("</span> 个星体</p>")
           .append("<p><span class='stat'>").append(totalEquip).append("</span> 件器材</p>");

        Map<?, Long> typeRaw = statsService.countByBodyType(userId);
        Map<String, Long> typeDist = new LinkedHashMap<>();
        typeRaw.forEach((k, v) -> typeDist.put(k.toString(), v));
        html.append("<h2>星体类型分布</h2><table><tr><th>类型</th><th>次数</th></tr>");
        for (var e : typeDist.entrySet()) {
            html.append("<tr><td>").append(escapeHtml(e.getKey()))
                .append("</td><td>").append(e.getValue()).append("</td></tr>");
        }
        html.append("</table>");

        var topEquip = statsService.topEquipment(userId);
        html.append("<h2>器材使用排行</h2><table><tr><th>器材</th><th>使用次数</th></tr>");
        for (var e : topEquip) {
            html.append("<tr><td>").append(escapeHtml(e.getKey()))
                .append("</td><td>").append(e.getValue()).append("</td></tr>");
        }
        html.append("</table>");

        html.append("<hr><p style='color:#999;font-size:12px;'>")
           .append("由 AstroLog v1.0 自动生成</p></body></html>");

        return html.toString();
    }

    public String generateHtmlReport(User user, int year) throws IOException {
        String html = buildHtmlContent(user, year);
        Path outputDir = REPORTS_DIR;
        Files.createDirectories(outputDir);
        Path file = outputDir.resolve("annual_report_" + year + ".html");
        Files.writeString(file, html, StandardCharsets.UTF_8);
        return file.toAbsolutePath().toString();
    }

    public String generatePdfReport(User user, int year) throws Exception {
        int userId = user.getUserId();
        Map<String, Object> params = new HashMap<>();
        params.put("USERNAME", user.getUsername());
        params.put("YEAR", String.valueOf(year));
        params.put("TOTAL_OBS", statsService.totalObservations(userId));
        params.put("DISTINCT_BODIES", statsService.distinctBodiesObserved(userId));
        params.put("TOTAL_EQUIP", statsService.totalEquipmentUsed(userId));
        params.put("GENERATED_AT", LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // Type distribution data
        Map<?, Long> typeRaw = statsService.countByBodyType(userId);
        Map<String, Long> typeDist = new LinkedHashMap<>();
        typeRaw.forEach((k, v) -> typeDist.put(k.toString(), v));
        params.put("TYPE_DIST", typeDist);

        // Equipment ranking data
        params.put("TOP_EQUIP", statsService.topEquipment(userId));

        Path outputDir = REPORTS_DIR;
        Files.createDirectories(outputDir);
        String outputPath = outputDir.resolve("annual_report_" + year + ".pdf")
            .toAbsolutePath().toString();

        ExportUtil.exportPdfReport(params, outputPath);
        return outputPath;
    }

    public String generateMessierCert(User user) throws Exception {
        int userId = user.getUserId();
        Set<Integer> observed = messierService.getObservedNumbers(userId);

        if (!messierService.isCertEligible(observed)) {
            throw new IllegalStateException("未完成全部 110 个梅西耶天体观测");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("USERNAME", user.getUsername());
        params.put("COMPLETED_COUNT", observed.size());
        params.put("TOTAL_COUNT", 110);
        params.put("COMPLETED_AT", LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        Path outputDir = REPORTS_DIR;
        Files.createDirectories(outputDir);
        String outputPath = outputDir.resolve("messier_cert.pdf")
            .toAbsolutePath().toString();

        ExportUtil.exportPdfCert(params, outputPath);
        return outputPath;
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
