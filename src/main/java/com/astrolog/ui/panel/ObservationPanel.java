package com.astrolog.ui.panel;

import com.astrolog.dao.BodyDao;
import com.astrolog.dao.EquipDao;
import com.astrolog.dao.SiteDao;
import com.astrolog.dao.TagDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.Equipment;
import com.astrolog.model.Observation;
import com.astrolog.model.ObservationSite;
import com.astrolog.model.ObservationTag;
import com.astrolog.model.User;
import com.astrolog.model.enums.MoonPhase;
import com.astrolog.service.ObsService;
import com.astrolog.service.ServiceResult;
import com.astrolog.ui.dialog.AddObsDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObservationPanel extends JPanel {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] COLUMNS = {
        "日期", "星体", "星座", "地点", "天气", "视宁度", "月相", "器材", "标签", "笔记"
    };
    private static final String[] WEATHERS = {"", "晴", "多云", "阴", "小雨", "雾", "大风"};
    private static final String[] SEEING_OPTIONS = {"", "1", "2", "3", "4", "5"};
    private static final String[] MOON_PHASE_OPTIONS;

    static {
        MoonPhase[] phases = MoonPhase.values();
        MOON_PHASE_OPTIONS = new String[phases.length + 1];
        MOON_PHASE_OPTIONS[0] = "";
        for (int i = 0; i < phases.length; i++) {
            MOON_PHASE_OPTIONS[i + 1] = phases[i].getDisplayName();
        }
    }

    private final User currentUser;
    private final ObsService obsService;
    private final BodyDao bodyDao;
    private final EquipDao equipDao;
    private final SiteDao siteDao;
    private final TagDao tagDao;

    private JTable obsTable;
    private ObsTableModel tableModel;
    private JLabel countLabel;

    private JTextField startYearField, startMinuteField;
    private JComboBox<String> startMonthCombo, startDayCombo, startHourCombo;
    private JTextField endYearField, endMinuteField;
    private JComboBox<String> endMonthCombo, endDayCombo, endHourCombo;
    private JTextField starSearchField;
    private JComboBox<String> siteFilterCombo;
    private JComboBox<String> weatherFilterCombo;
    private JComboBox<String> seeingFilterCombo;
    private JComboBox<String> moonPhaseFilterCombo;
    private JTextField keywordField;

    private List<Observation> obsList;
    private Map<Integer, CelestialBody> bodyMap;
    private Map<Integer, ObservationSite> siteMap;
    private Map<Integer, Equipment> equipMap;
    private Map<Integer, List<ObservationTag>> tagMap;

    public ObservationPanel(User currentUser) {
        this.currentUser = currentUser;
        this.obsService = new ObsService();
        this.bodyDao = new BodyDao();
        this.equipDao = new EquipDao();
        this.siteDao = new SiteDao();
        this.tagDao = new TagDao();
        this.obsList = new ArrayList<>();
        this.bodyMap = new HashMap<>();
        this.siteMap = new HashMap<>();
        this.equipMap = new HashMap<>();
        this.tagMap = new HashMap<>();

        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ========== 筛选栏 (NORTH) ==========
        JPanel filterContainer = new JPanel(new GridLayout(3, 1, 0, 2));
        filterContainer.setBorder(BorderFactory.createTitledBorder("筛选条件"));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));

        // 起始时间
        row1.add(new JLabel("起:"));
        startYearField = new JTextField(4);
        row1.add(startYearField);
        row1.add(new JLabel("年"));
        startMonthCombo = createMonthCombo();
        row1.add(startMonthCombo);
        row1.add(new JLabel("月"));
        startDayCombo = createDayCombo();
        row1.add(startDayCombo);
        row1.add(new JLabel("日"));
        startHourCombo = createHourCombo();
        row1.add(startHourCombo);
        row1.add(new JLabel("时"));
        startMinuteField = new JTextField(3);
        row1.add(startMinuteField);
        row1.add(new JLabel("分"));

        // 截止时间
        row1.add(new JLabel(" 至:"));
        endYearField = new JTextField(4);
        row1.add(endYearField);
        row1.add(new JLabel("年"));
        endMonthCombo = createMonthCombo();
        row1.add(endMonthCombo);
        row1.add(new JLabel("月"));
        endDayCombo = createDayCombo();
        row1.add(endDayCombo);
        row1.add(new JLabel("日"));
        endHourCombo = createHourCombo();
        row1.add(endHourCombo);
        row1.add(new JLabel("时"));
        endMinuteField = new JTextField(3);
        row1.add(endMinuteField);
        row1.add(new JLabel("分"));

        filterContainer.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        row2.add(new JLabel("星体:"));
        starSearchField = new JTextField(8);
        row2.add(starSearchField);

        row2.add(new JLabel("地点:"));
        siteFilterCombo = new JComboBox<>();
        row2.add(siteFilterCombo);

        row2.add(new JLabel("天气:"));
        weatherFilterCombo = new JComboBox<>(WEATHERS);
        row2.add(weatherFilterCombo);

        row2.add(new JLabel("视宁度:"));
        seeingFilterCombo = new JComboBox<>(SEEING_OPTIONS);
        row2.add(seeingFilterCombo);

        row2.add(new JLabel("月相:"));
        moonPhaseFilterCombo = new JComboBox<>(MOON_PHASE_OPTIONS);
        row2.add(moonPhaseFilterCombo);

        filterContainer.add(row2);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        row3.add(new JLabel("关键词:"));
        keywordField = new JTextField(8);
        row3.add(keywordField);

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> performSearch());
        row3.add(searchBtn);

        JButton clearBtn = new JButton("清除");
        clearBtn.addActionListener(e -> clearFilters());
        row3.add(clearBtn);

        filterContainer.add(row3);
        add(filterContainer, BorderLayout.NORTH);

        // ========== 数据表格 (CENTER) ==========
        tableModel = new ObsTableModel();
        obsTable = new JTable(tableModel);
        obsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        obsTable.setRowHeight(24);
        obsTable.getTableHeader().setReorderingAllowed(false);
        obsTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        obsTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        obsTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        obsTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        obsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        obsTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        obsTable.getColumnModel().getColumn(4).setPreferredWidth(40);
        obsTable.getColumnModel().getColumn(5).setPreferredWidth(45);
        obsTable.getColumnModel().getColumn(6).setPreferredWidth(55);
        obsTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        obsTable.getColumnModel().getColumn(8).setPreferredWidth(80);
        obsTable.getColumnModel().getColumn(9).setPreferredWidth(200);

        obsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = obsTable.rowAtPoint(e.getPoint());
                    int col = obsTable.columnAtPoint(e.getPoint());
                    if (row >= 0 && (col == 1 || col == 3 || col == 7 || col == 8 || col == 9)) {
                        String full = getFullContent(row, col);
                        if (full != null && !full.isEmpty()) {
                            JTextArea textArea = new JTextArea(full);
                            textArea.setEditable(false);
                            textArea.setLineWrap(true);
                            textArea.setWrapStyleWord(true);
                            textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                            JScrollPane scrollPane = new JScrollPane(textArea);
                            scrollPane.setPreferredSize(new java.awt.Dimension(420, 200));
                            JOptionPane.showMessageDialog(ObservationPanel.this,
                                scrollPane, COLUMNS[col] + " — 完整内容",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        add(new JScrollPane(obsTable), BorderLayout.CENTER);

        // ========== 操作栏 (SOUTH) ==========
        JPanel actionPanel = new JPanel(new BorderLayout(10, 5));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton addBtn = new JButton("添加观测");
        addBtn.addActionListener(e -> openAddDialog(null));
        btnPanel.add(addBtn);

        JButton editBtn = new JButton("编辑");
        editBtn.addActionListener(e -> {
            int row = obsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选择一条观测记录",
                    "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            openAddDialog(obsList.get(row));
        });
        btnPanel.add(editBtn);

        JButton deleteBtn = new JButton("删除");
        deleteBtn.addActionListener(e -> handleDelete());
        btnPanel.add(deleteBtn);

        actionPanel.add(btnPanel, BorderLayout.WEST);

        countLabel = new JLabel();
        countLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        actionPanel.add(countLabel, BorderLayout.EAST);

        add(actionPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        obsList = obsService.listByUser(currentUser.getUserId());
        refreshCache();
        tableModel.fireTableDataChanged();
        updateCount();
    }

    private JComboBox<String> createMonthCombo() {
        String[] months = new String[13];
        months[0] = "";
        for (int i = 1; i <= 12; i++) months[i] = String.valueOf(i);
        return new JComboBox<>(months);
    }

    private JComboBox<String> createDayCombo() {
        String[] days = new String[32];
        days[0] = "";
        for (int i = 1; i <= 31; i++) days[i] = String.valueOf(i);
        return new JComboBox<>(days);
    }

    private JComboBox<String> createHourCombo() {
        String[] hours = new String[25];
        hours[0] = "";
        for (int i = 0; i <= 23; i++) hours[i + 1] = String.format("%02d", i);
        return new JComboBox<>(hours);
    }

    private LocalDateTime parseTimeRange(JTextField yearField, JComboBox<String> monthCombo,
                                          JComboBox<String> dayCombo, JComboBox<String> hourCombo,
                                          JTextField minuteField) {
        String yStr = yearField.getText().trim();
        String minStr = minuteField.getText().trim();
        String mStr = (String) monthCombo.getSelectedItem();
        String dStr = (String) dayCombo.getSelectedItem();
        String hStr = (String) hourCombo.getSelectedItem();

        if (yStr.isEmpty() && mStr.isEmpty() && dStr.isEmpty()
            && hStr.isEmpty() && minStr.isEmpty()) {
            return null;
        }

        try {
            int year = yStr.isEmpty() ? 2000 : Integer.parseInt(yStr);
            if (year < 1900 || year > 2100) {
                JOptionPane.showMessageDialog(this, "年份必须在 1900-2100 之间",
                    "提示", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            int month = mStr.isEmpty() ? 1 : Integer.parseInt(mStr);
            int day = dStr.isEmpty() ? 1 : Integer.parseInt(dStr);
            int hour = hStr.isEmpty() ? 0 : Integer.parseInt(hStr);
            int minute = minStr.isEmpty() ? 0 : Integer.parseInt(minStr);
            if (minute < 0 || minute > 59) {
                JOptionPane.showMessageDialog(this, "分钟必须在 0-59 之间",
                    "提示", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return LocalDateTime.of(year, month, day, hour, minute);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "年份/分钟请输入有效数字",
                "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        } catch (java.time.DateTimeException e) {
            JOptionPane.showMessageDialog(this, "日期无效: " + e.getMessage(),
                "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

    private void performSearch() {
        LocalDateTime start = parseTimeRange(startYearField, startMonthCombo,
            startDayCombo, startHourCombo, startMinuteField);
        LocalDateTime end = parseTimeRange(endYearField, endMonthCombo,
            endDayCombo, endHourCombo, endMinuteField);

        if ((startYearField.getText().trim().isEmpty() && startMinuteField.getText().trim().isEmpty()
             && startMonthCombo.getSelectedIndex() == 0 && startDayCombo.getSelectedIndex() == 0
             && startHourCombo.getSelectedIndex() == 0) || start != null) {
            // start is valid or no filter
        } else {
            return;
        }
        if ((endYearField.getText().trim().isEmpty() && endMinuteField.getText().trim().isEmpty()
             && endMonthCombo.getSelectedIndex() == 0 && endDayCombo.getSelectedIndex() == 0
             && endHourCombo.getSelectedIndex() == 0) || end != null) {
            // end is valid or no filter
        } else {
            return;
        }

        Set<Integer> matchingBodyIds = null;
        String starText = starSearchField.getText().trim();
        if (!starText.isEmpty()) {
            matchingBodyIds = new HashSet<>();
            String lower = starText.toLowerCase();
            for (CelestialBody b : bodyMap.values()) {
                if (b.getName().toLowerCase().contains(lower)) {
                    matchingBodyIds.add(b.getBodyId());
                }
            }
            if (matchingBodyIds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "未找到包含 \"" + starText + "\" 的星体",
                    "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Integer siteId = null;
        int siteIdx = siteFilterCombo.getSelectedIndex();
        if (siteIdx > 0) {
            List<ObservationSite> sites = new ArrayList<>(siteMap.values());
            if (siteIdx - 1 < sites.size()) {
                siteId = sites.get(siteIdx - 1).getSiteId();
            }
        }

        String weather = (String) weatherFilterCombo.getSelectedItem();
        if (weather != null && weather.isEmpty()) weather = null;

        Integer seeing = null;
        String seeingStr = (String) seeingFilterCombo.getSelectedItem();
        if (seeingStr != null && !seeingStr.isEmpty()) seeing = Integer.parseInt(seeingStr);

        String moonPhase = null;
        String mpStr = (String) moonPhaseFilterCombo.getSelectedItem();
        if (mpStr != null && !mpStr.isEmpty()) {
            moonPhase = MoonPhase.fromString(mpStr).name().toLowerCase();
        }

        String keyword = keywordField.getText().trim();
        if (keyword.isEmpty()) keyword = null;

        obsList = obsService.search(currentUser.getUserId(), start, end,
            null, siteId, weather, seeing, moonPhase, keyword);

        if (matchingBodyIds != null) {
            List<Observation> filtered = new ArrayList<>();
            for (Observation o : obsList) {
                if (matchingBodyIds.contains(o.getBodyId())) {
                    filtered.add(o);
                }
            }
            obsList = filtered;
        }

        refreshCache();
        tableModel.fireTableDataChanged();
        updateCount();
    }

    private void clearFilters() {
        startYearField.setText("");
        startMonthCombo.setSelectedIndex(0);
        startDayCombo.setSelectedIndex(0);
        startHourCombo.setSelectedIndex(0);
        startMinuteField.setText("");
        endYearField.setText("");
        endMonthCombo.setSelectedIndex(0);
        endDayCombo.setSelectedIndex(0);
        endHourCombo.setSelectedIndex(0);
        endMinuteField.setText("");
        starSearchField.setText("");
        siteFilterCombo.setSelectedIndex(0);
        weatherFilterCombo.setSelectedIndex(0);
        seeingFilterCombo.setSelectedIndex(0);
        moonPhaseFilterCombo.setSelectedIndex(0);
        keywordField.setText("");
        loadData();
    }

    private void openAddDialog(Observation existing) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        new AddObsDialog(frame, currentUser, existing, this::loadData);
    }

    public void showAddDialogForBody(CelestialBody body) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        new AddObsDialog(frame, currentUser, body, this::loadData);
    }

    private void handleDelete() {
        int row = obsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条观测记录",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Observation obs = obsList.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除这条观测记录吗？", "确认删除",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            ServiceResult result =
                obsService.deleteObservation(obs.getObsId(), currentUser.getUserId());
            if (result.isSuccess()) {
                loadData();
            }
            JOptionPane.showMessageDialog(this, result.getMessage(),
                result.isSuccess() ? "提示" : "错误",
                result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshCache() {
        bodyMap.clear();
        for (CelestialBody b : bodyDao.findAll()) {
            bodyMap.put(b.getBodyId(), b);
        }

        siteMap.clear();
        for (ObservationSite s : siteDao.findAllByUserId(currentUser.getUserId())) {
            siteMap.put(s.getSiteId(), s);
        }

        equipMap.clear();
        for (Equipment e : equipDao.findAllByUserId(currentUser.getUserId())) {
            equipMap.put(e.getEquipId(), e);
        }

        tagMap.clear();
        for (Observation o : obsList) {
            List<ObservationTag> tags = tagDao.findByObsId(o.getObsId());
            tagMap.put(o.getObsId(), tags);
        }

        // 刷新筛选下拉
        siteFilterCombo.removeAllItems();
        siteFilterCombo.addItem("");
        for (ObservationSite s : siteMap.values()) {
            siteFilterCombo.addItem(s.getName());
        }
    }

    private void updateCount() {
        countLabel.setText("共 " + obsList.size() + " 条记录");
    }

    private String getFullContent(int row, int col) {
        if (row < 0 || row >= obsList.size()) return "";
        Observation o = obsList.get(row);
        switch (col) {
            case 1: {
                CelestialBody body = bodyMap.get(o.getBodyId());
                if (body == null) return "(星体已删除)";
                return "名称: " + body.getName()
                    + "\n类型: " + body.getType().getDisplayName()
                    + "\n星座: " + body.getConstellation()
                    + "\n赤经: " + body.getRaH() + "h " + body.getRaM() + "m"
                    + "\n赤纬: " + body.getDecDeg() + "° " + body.getDecMin() + "'"
                    + "\n视星等: " + body.getMagnitude()
                    + (body.getDistanceLy() != null
                        ? "\n距离: " + body.getDistanceLy() + " 光年" : "")
                    + (body.getMessierNumber() != null
                        ? "\n梅西耶编号: M" + body.getMessierNumber() : "")
                    + (body.getNgcNumber() != null
                        ? "\nNGC 编号: NGC" + body.getNgcNumber() : "")
                    + (body.getBestSeason() != null
                        ? "\n最佳观测季节: " + body.getBestSeason() : "")
                    + (body.getDescription() != null && !body.getDescription().isEmpty()
                        ? "\n\n描述: " + body.getDescription() : "");
            }
            case 3: {
                Integer sid = o.getSiteId();
                if (sid == null) return "(无地点)";
                ObservationSite site = siteMap.get(sid);
                if (site == null) return "(地点已删除)";
                return "名称: " + site.getName()
                    + "\n纬度: " + site.getLatitude()
                    + "\n经度: " + site.getLongitude()
                    + "\n海拔: " + site.getAltitude() + " m"
                    + "\n波特尔等级: " + site.getBortleScale()
                    + (site.getBestTime() != null && !site.getBestTime().isEmpty()
                        ? "\n最佳时间: " + site.getBestTime() : "");
            }
            case 7: {
                List<Integer> eqIds = obsService.getEquipmentIds(o.getObsId());
                StringBuilder sb = new StringBuilder();
                for (int eid : eqIds) {
                    Equipment eq = equipMap.get(eid);
                    if (eq != null) {
                        if (sb.length() > 0) sb.append("\n");
                        sb.append("  • ").append(eq.getName());
                    }
                }
                return sb.length() > 0 ? sb.toString() : "(无器材)";
            }
            case 8: {
                List<ObservationTag> tags = tagMap.get(o.getObsId());
                if (tags == null || tags.isEmpty()) return "(无标签)";
                StringBuilder sb = new StringBuilder();
                for (ObservationTag t : tags) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("  • ").append(t.getName());
                }
                return sb.toString();
            }
            case 9: {
                String note = o.getNote();
                return (note == null || note.isEmpty()) ? "(无笔记)" : note;
            }
            default:
                return "";
        }
    }

    private class ObsTableModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return obsList.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Observation o = obsList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return o.getObsTime() != null ? o.getObsTime().format(DT_FMT) : "";
                case 1: {
                    CelestialBody body = bodyMap.get(o.getBodyId());
                    return body != null ? body.getName() : "";
                }
                case 2: {
                    CelestialBody body = bodyMap.get(o.getBodyId());
                    return body != null ? body.getConstellation() : "";
                }
                case 3: {
                    Integer sid = o.getSiteId();
                    if (sid == null) return "";
                    ObservationSite site = siteMap.get(sid);
                    return site != null ? site.getName() : "";
                }
                case 4:
                    return o.getWeather() != null ? o.getWeather() : "";
                case 5:
                    return String.valueOf(o.getSeeing());
                case 6:
                    return o.getMoonPhase() != null ? o.getMoonPhase().getDisplayName() : "";
                case 7: {
                    List<Integer> eqIds = obsService.getEquipmentIds(o.getObsId());
                    StringBuilder sb = new StringBuilder();
                    for (int eid : eqIds) {
                        Equipment eq = equipMap.get(eid);
                        if (eq != null) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(eq.getName());
                        }
                    }
                    return sb.toString();
                }
                case 8: {
                    List<ObservationTag> tags = tagMap.get(o.getObsId());
                    if (tags == null) return "";
                    StringBuilder sb = new StringBuilder();
                    for (ObservationTag t : tags) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(t.getName());
                    }
                    return sb.toString();
                }
                case 9: {
                    String note = o.getNote();
                    if (note == null || note.isEmpty()) return "";
                    return note.length() > 30 ? note.substring(0, 30) + "..." : note;
                }
                default:
                    return "";
            }
        }
    }
}
