package com.astrolog.unit.service;

import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;
import com.astrolog.service.ExportService;
import com.astrolog.service.MessierService;
import com.astrolog.service.StatsService;
import com.astrolog.util.ExportUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    private static Path tempDir;

    @BeforeAll
    static void initTempDir() throws IOException {
        tempDir = Files.createTempDirectory("astrolog_test_");
        System.setProperty("astrolog.reports.dir", tempDir.toString());
    }

    @AfterAll
    static void cleanTempDir() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            try (Stream<Path> files = Files.walk(tempDir)) {
                files.sorted(Comparator.reverseOrder()).forEach(f -> {
                    try { Files.deleteIfExists(f); } catch (IOException ignored) {}
                });
            }
        }
    }

    @Mock
    private StatsService statsService;

    @Mock
    private MessierService messierService;

    private ExportService exportService;
    private User testUser;

    @BeforeEach
    void setUp() {
        exportService = new ExportService();
        injectMocks();

        testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("test_observer");
        testUser.setRole(UserRole.OBSERVER);

        lenient().when(statsService.totalObservations(anyInt())).thenReturn(10L);
        lenient().when(statsService.distinctBodiesObserved(anyInt())).thenReturn(5L);
        lenient().when(statsService.totalEquipmentUsed(anyInt())).thenReturn(3L);

        Map<Object, Long> typeDist = new LinkedHashMap<>();
        typeDist.put("恒星", 4L);
        typeDist.put("行星", 3L);
        typeDist.put("星云", 2L);
        lenient().when(statsService.countByBodyType(anyInt())).thenReturn((Map) typeDist);

        lenient().when(statsService.topEquipment(anyInt()))
            .thenReturn(List.of(
                new AbstractMap.SimpleEntry<>("Celestron C8", 5L),
                new AbstractMap.SimpleEntry<>("Nikon 10x50", 3L)));
    }

    private void injectMocks() {
        try {
            var statsField = ExportService.class.getDeclaredField("statsService");
            statsField.setAccessible(true);
            statsField.set(exportService, statsService);

            var messierField = ExportService.class.getDeclaredField("messierService");
            messierField.setAccessible(true);
            messierField.set(exportService, messierService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ES-001: generateHtmlReport → 文件创建且非空
    @Test
    void testGenerateHtmlReport_FileCreated() throws IOException {
        String path = exportService.generateHtmlReport(testUser, 2026);

        assertNotNull(path);
        Path file = Paths.get(path);
        assertTrue(Files.exists(file));
        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.length() > 0);
    }

    // ES-002: generateHtmlReport → HTML 包含用户名
    @Test
    void testGenerateHtmlReport_ContainsUsername() throws IOException {
        String path = exportService.generateHtmlReport(testUser, 2026);
        String content = Files.readString(Paths.get(path), StandardCharsets.UTF_8);

        assertTrue(content.contains("test_observer"));
    }

    // ES-003: generateHtmlReport → HTML 包含观测次数
    @Test
    void testGenerateHtmlReport_ContainsObsCount() throws IOException {
        String path = exportService.generateHtmlReport(testUser, 2026);
        String content = Files.readString(Paths.get(path), StandardCharsets.UTF_8);

        assertTrue(content.contains("10"));
        assertTrue(content.contains("5"));
        assertTrue(content.contains("3"));
    }

    // ES-004: generatePdfReport → Mock ExportUtil 避免真实 OpenPDF 写入
    @Test
    void testGeneratePdfReport_FileCreated() throws Exception {
        try (MockedStatic<ExportUtil> mockedExportUtil = mockStatic(ExportUtil.class)) {
            mockedExportUtil.when(() -> ExportUtil.exportPdfReport(any(), anyString()))
                .thenAnswer(invocation -> null);

            assertDoesNotThrow(() -> exportService.generatePdfReport(testUser, 2026));
        }
    }

    // ES-005: generateMessierCert_success → isCertEligible true → 不抛异常
    @Test
    void testGenerateMessierCert_Success() throws Exception {
        Set<Integer> allObserved = new HashSet<>();
        for (int i = 1; i <= 110; i++) {
            allObserved.add(i);
        }
        when(messierService.getObservedNumbers(anyInt())).thenReturn(allObserved);
        when(messierService.isCertEligible(allObserved)).thenReturn(true);

        try (MockedStatic<ExportUtil> mockedExportUtil = mockStatic(ExportUtil.class)) {
            mockedExportUtil.when(() -> ExportUtil.exportPdfCert(any(), anyString()))
                .thenAnswer(invocation -> null);

            assertDoesNotThrow(() -> exportService.generateMessierCert(testUser));
        }
    }

    // ES-006: generateMessierCert_notEligible → isCertEligible false → 抛出异常
    @Test
    void testGenerateMessierCert_NotEligible() {
        Set<Integer> partial = new HashSet<>();
        partial.add(1);
        partial.add(2);
        when(messierService.getObservedNumbers(anyInt())).thenReturn(partial);
        when(messierService.isCertEligible(partial)).thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> exportService.generateMessierCert(testUser));
    }

    // ES-007: generateHtmlReport_emptyData → 观测次数 0 → HTML 仍正常生成
    @Test
    void testGenerateHtmlReport_EmptyData() throws IOException {
        when(statsService.totalObservations(anyInt())).thenReturn(0L);
        when(statsService.distinctBodiesObserved(anyInt())).thenReturn(0L);
        when(statsService.totalEquipmentUsed(anyInt())).thenReturn(0L);
        when(statsService.countByBodyType(anyInt())).thenReturn(Map.of());

        String path = exportService.generateHtmlReport(testUser, 2026);
        String content = Files.readString(Paths.get(path), StandardCharsets.UTF_8);

        assertTrue(content.contains("0"));
        assertTrue(content.contains("AstroLog"));
    }

    // ES-008: 输出目录自动创建
    @Test
    void testOutputDirectoryAutoCreated() throws IOException {
        Path reportsDir = ExportService.getReportsDir();
        if (Files.exists(reportsDir)) {
            try (var files = Files.list(reportsDir)) {
                files.forEach(f -> {
                    try { Files.deleteIfExists(f); } catch (IOException ignored) {}
                });
            }
            Files.deleteIfExists(reportsDir);
        }

        exportService.generateHtmlReport(testUser, 2026);

        assertTrue(Files.exists(reportsDir));
        assertTrue(Files.isDirectory(reportsDir));
    }
}
