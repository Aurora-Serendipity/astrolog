package com.astrolog.ui.dialog;

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
import com.astrolog.service.SiteService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddObsDialog extends JDialog {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] WEATHERS = {"晴", "多云", "阴", "小雨", "雾", "大风"};
    private static final String[] SEEING_ITEMS = {"1(极好)", "2(好)", "3(一般)", "4(较差)", "5(差)"};

    private final User currentUser;
    private final Observation existing;
    private final Runnable onSaved;

    private final ObsService obsService;
    private final BodyDao bodyDao;
    private final EquipDao equipDao;
    private final SiteDao siteDao;
    private final TagDao tagDao;

    private JTextField starField;
    private int selectedBodyId;
    private JTextField timeField;
    private JComboBox<String> siteCombo;
    private List<ObservationSite> siteList;
    private JComboBox<String> weatherCombo;
    private JComboBox<String> seeingCombo;
    private JComboBox<String> moonPhaseCombo;
    private List<JCheckBox> equipCheckBoxes;
    private List<Equipment> equipmentData;
    private JPanel tagPanel;
    private List<JToggleButton> tagButtons;
    private JTextField newTagField;
    private JTextArea noteArea;

    public AddObsDialog(JFrame parent, User currentUser, Observation existing,
                         Runnable onSaved) {
        super(parent, existing == null ? "添加观测记录" : "编辑观测记录", true);
        this.currentUser = currentUser;
        this.existing = existing;
        this.onSaved = onSaved;
        this.obsService = new ObsService();
        this.bodyDao = new BodyDao();
        this.equipDao = new EquipDao();
        this.siteDao = new SiteDao();
        this.tagDao = new TagDao();
        this.tagButtons = new ArrayList<>();

        initComponents();
        if (existing != null) {
            prefillForm();
        }
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public AddObsDialog(JFrame parent, User currentUser, CelestialBody preselectedBody,
                         Runnable onSaved) {
        super(parent, "添加观测记录", true);
        this.currentUser = currentUser;
        this.existing = null;
        this.onSaved = onSaved;
        this.obsService = new ObsService();
        this.bodyDao = new BodyDao();
        this.equipDao = new EquipDao();
        this.siteDao = new SiteDao();
        this.tagDao = new TagDao();
        this.tagButtons = new ArrayList<>();

        initComponents();
        if (preselectedBody != null) {
            starField.setText(preselectedBody.getName());
            selectedBodyId = preselectedBody.getBodyId();
        }
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // 观测星体 *
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("观测星体 *:"), gbc);
        starField = new JTextField(25);
        gbc.gridx = 1;
        form.add(starField, gbc);
        initStarSearch();
        row++;

        // 观测时间 *
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("观测时间 * (yyyy-MM-dd HH:mm):"), gbc);
        timeField = new JTextField(LocalDateTime.now().format(DT_FMT), 25);
        gbc.gridx = 1;
        form.add(timeField, gbc);
        row++;

        // 观测地点
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("观测地点:"), gbc);
        JPanel siteRow = new JPanel(new BorderLayout(5, 0));
        siteCombo = new JComboBox<>();
        refreshSiteCombo();
        siteRow.add(siteCombo, BorderLayout.CENTER);
        JButton newSiteBtn = new JButton("新建地点");
        newSiteBtn.addActionListener(e -> showNewSiteDialog());
        siteRow.add(newSiteBtn, BorderLayout.EAST);
        gbc.gridx = 1;
        form.add(siteRow, gbc);
        row++;

        // 天气
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("天气:"), gbc);
        weatherCombo = new JComboBox<>(WEATHERS);
        weatherCombo.insertItemAt("", 0);
        weatherCombo.setSelectedIndex(0);
        gbc.gridx = 1;
        form.add(weatherCombo, gbc);
        row++;

        // 视宁度 *
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("视宁度 *:"), gbc);
        seeingCombo = new JComboBox<>(SEEING_ITEMS);
        seeingCombo.setSelectedIndex(2);
        gbc.gridx = 1;
        form.add(seeingCombo, gbc);
        row++;

        // 月相
        gbc.gridx = 0; gbc.gridy = row;
        form.add(new JLabel("月相:"), gbc);
        MoonPhase[] phases = MoonPhase.values();
        String[] phaseNames = new String[phases.length + 1];
        phaseNames[0] = "";
        for (int i = 0; i < phases.length; i++) {
            phaseNames[i + 1] = phases[i].getDisplayName();
        }
        moonPhaseCombo = new JComboBox<>(phaseNames);
        gbc.gridx = 1;
        form.add(moonPhaseCombo, gbc);
        row++;

        // 所用器材
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("所用器材:"), gbc);
        equipmentData = equipDao.findAllByUserId(currentUser.getUserId());
        JPanel equipPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        equipCheckBoxes = new ArrayList<>();
        for (Equipment eq : equipmentData) {
            JCheckBox cb = new JCheckBox(eq.getName());
            cb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            equipCheckBoxes.add(cb);
            equipPanel.add(cb);
        }
        JScrollPane equipScroll = new JScrollPane(equipPanel);
        equipScroll.setPreferredSize(new Dimension(250, 130));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        form.add(equipScroll, gbc);
        row++;

        // 标签
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("标签:"), gbc);
        tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        refreshTagButtons(null);
        JPanel tagContainer = new JPanel(new BorderLayout(5, 5));
        tagContainer.add(tagPanel, BorderLayout.CENTER);
        JPanel newTagRow = new JPanel(new BorderLayout(5, 0));
        newTagField = new JTextField(10);
        newTagRow.add(newTagField, BorderLayout.CENTER);
        JButton addTagBtn = new JButton("添加");
        addTagBtn.addActionListener(e -> addNewTag());
        newTagRow.add(addTagBtn, BorderLayout.EAST);
        tagContainer.add(newTagRow, BorderLayout.SOUTH);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        form.add(tagContainer, gbc);
        row++;

        // 观测笔记
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("观测笔记:"), gbc);
        noteArea = new JTextArea(5, 25);
        noteArea.setLineWrap(true);
        JScrollPane noteScroll = new JScrollPane(noteArea);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        form.add(noteScroll, gbc);

        add(form, BorderLayout.CENTER);

        // 底部按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton saveBtn = new JButton("保存");
        saveBtn.addActionListener(e -> handleSave());
        btnPanel.add(saveBtn);
        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void initStarSearch() {
        JPopupMenu popup = new JPopupMenu();
        JList<String> suggestionList = new JList<>();
        JScrollPane popupScroll = new JScrollPane(suggestionList);
        popupScroll.setPreferredSize(new Dimension(250, 150));
        popup.add(popupScroll);

        List<CelestialBody> allBodies = bodyDao.findAll();
        selectedBodyId = 0;

        starField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String text = starField.getText().trim().toLowerCase();
                DefaultListModel<String> model = new DefaultListModel<>();
                for (CelestialBody b : allBodies) {
                    if (text.isEmpty() || b.getName().toLowerCase().contains(text)) {
                        model.addElement(b.getName() + " (" + b.getConstellation() + ")");
                    }
                }
                suggestionList.setModel(model);
                if (model.size() > 0 && !popup.isVisible()) {
                    popup.show(starField, 0, starField.getHeight());
                    starField.requestFocus();
                }
                if (model.size() == 0) {
                    popup.setVisible(false);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) { SwingUtilities.invokeLater(this::update); }
            @Override
            public void removeUpdate(DocumentEvent e) { SwingUtilities.invokeLater(this::update); }
            @Override
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(this::update); }
        });

        suggestionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    String name = selected.substring(0, selected.indexOf(" ("));
                    starField.setText(name);
                    for (CelestialBody b : allBodies) {
                        if (b.getName().equals(name)) {
                            selectedBodyId = b.getBodyId();
                            break;
                        }
                    }
                    popup.setVisible(false);
                }
            }
        });
    }

    private void refreshSiteCombo() {
        siteCombo.removeAllItems();
        siteCombo.addItem("(不选择)");
        siteList = siteDao.findAllByUserId(currentUser.getUserId());
        for (ObservationSite s : siteList) {
            siteCombo.addItem(s.getName());
        }
    }

    private void refreshTagButtons(Set<String> preselected) {
        tagPanel.removeAll();
        tagButtons.clear();
        List<ObservationTag> allTags = tagDao.findAll();
        for (ObservationTag t : allTags) {
            JToggleButton btn = new JToggleButton(t.getName());
            btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            if (t.getColor() != null && !t.getColor().isEmpty()) {
                try {
                    btn.setForeground(Color.decode(t.getColor()));
                } catch (NumberFormatException ignored) {
                }
            }
            if (preselected != null && preselected.contains(t.getName())) {
                btn.setSelected(true);
            }
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showTagContextMenu(e.getComponent(), t);
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        showTagContextMenu(e.getComponent(), t);
                    }
                }
            });
            tagButtons.add(btn);
            tagPanel.add(btn);
        }
        tagPanel.revalidate();
        tagPanel.repaint();
    }

    private void showTagContextMenu(java.awt.Component comp, ObservationTag tag) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem deleteItem = new JMenuItem("删除标签: " + tag.getName());
        deleteItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除标签 \"" + tag.getName() + "\" 吗？\n如果该标签已关联观测记录，关联关系将被清除。",
                "确认删除标签", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                tagDao.delete(tag.getTagId());
                refreshTagButtons(null);
            }
        });
        menu.add(deleteItem);

        JMenuItem colorItem = new JMenuItem("修改颜色: " + tag.getName());
        colorItem.addActionListener(e -> {
            String newColor = JOptionPane.showInputDialog(this,
                "输入新颜色值 (如 #FF0000):", tag.getColor());
            if (newColor != null && !newColor.trim().isEmpty()) {
                try {
                    String trimmed = newColor.trim();
                    Color.decode(trimmed);
                    tagDao.update(tag.getTagId(), tag.getName(), trimmed);
                    refreshTagButtons(null);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "无效的颜色值: " + newColor, "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(colorItem);

        menu.show(comp, 0, comp.getHeight());
    }

    private void addNewTag() {
        String name = newTagField.getText().trim();
        if (name.isEmpty()) {
            return;
        }
        for (JToggleButton btn : tagButtons) {
            if (btn.getText().equals(name)) {
                btn.setSelected(true);
                newTagField.setText("");
                return;
            }
        }
        int tagId = tagDao.getOrCreate(name, "#3366CC");
        if (tagId > 0) {
            refreshTagButtons(Set.of(name));
            for (JToggleButton btn : tagButtons) {
                if (btn.getText().equals(name)) {
                    btn.setSelected(true);
                    break;
                }
            }
        }
        newTagField.setText("");
    }

    private void showNewSiteDialog() {
        JTextField nameField = new JTextField(15);
        JTextField latField = new JTextField(
            currentUser.getDefaultLat() != null
                ? currentUser.getDefaultLat().toString() : "", 15);
        JTextField lonField = new JTextField(
            currentUser.getDefaultLon() != null
                ? currentUser.getDefaultLon().toString() : "", 15);
        JTextField altField = new JTextField("0", 15);
        JComboBox<String> bortleCombo = new JComboBox<>();
        for (int i = 1; i <= 9; i++) {
            bortleCombo.addItem(String.valueOf(i));
        }
        bortleCombo.setSelectedIndex(3);
        JTextField bestTimeField = new JTextField(15);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("名称:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("纬度(-90~90):"), gbc);
        gbc.gridx = 1;
        panel.add(latField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("经度(-180~180):"), gbc);
        gbc.gridx = 1;
        panel.add(lonField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("海拔(m):"), gbc);
        gbc.gridx = 1;
        panel.add(altField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("波特尔等级(1-9):"), gbc);
        gbc.gridx = 1;
        panel.add(bortleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("最佳观测时间:"), gbc);
        gbc.gridx = 1;
        panel.add(bestTimeField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "新建观测地点", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "地点名称不能为空",
                    "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                ObservationSite site = new ObservationSite();
                site.setUserId(currentUser.getUserId());
                site.setName(name);
                String latStr = latField.getText().trim();
                if (!latStr.isEmpty()) {
                    site.setLatitude(new BigDecimal(latStr));
                }
                String lonStr = lonField.getText().trim();
                if (!lonStr.isEmpty()) {
                    site.setLongitude(new BigDecimal(lonStr));
                }
                String altStr = altField.getText().trim();
                if (!altStr.isEmpty()) {
                    site.setAltitude(Integer.parseInt(altStr));
                }
                site.setBortleScale(Integer.parseInt(
                    (String) bortleCombo.getSelectedItem()));
                site.setBestTime(bestTimeField.getText().trim());
                SiteService siteService = new SiteService();
                ServiceResult sr = siteService.addSite(site);
                if (sr.isSuccess()) {
                    refreshSiteCombo();
                    siteCombo.setSelectedItem(name);
                } else {
                    JOptionPane.showMessageDialog(this, sr.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数字",
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void prefillForm() {
        for (CelestialBody b : bodyDao.findAll()) {
            if (b.getBodyId() == existing.getBodyId()) {
                starField.setText(b.getName());
                selectedBodyId = b.getBodyId();
                break;
            }
        }
        timeField.setText(existing.getObsTime().format(DT_FMT));
        if (existing.getSiteId() != null) {
            for (int i = 0; i < siteList.size(); i++) {
                if (siteList.get(i).getSiteId() == existing.getSiteId()) {
                    siteCombo.setSelectedIndex(i + 1);
                    break;
                }
            }
        }
        weatherCombo.setSelectedItem(existing.getWeather());
        seeingCombo.setSelectedIndex(existing.getSeeing() - 1);
        if (existing.getMoonPhase() != null) {
            moonPhaseCombo.setSelectedItem(existing.getMoonPhase().getDisplayName());
        }
        List<Integer> linkedEquipIds = obsService.getEquipmentIds(existing.getObsId());
        for (int i = 0; i < equipmentData.size(); i++) {
            equipCheckBoxes.get(i).setSelected(
                linkedEquipIds.contains(equipmentData.get(i).getEquipId()));
        }
        Set<String> existingTagNames = new HashSet<>(obsService.getTagNames(existing.getObsId()));
        refreshTagButtons(existingTagNames);
        noteArea.setText(existing.getNote() != null ? existing.getNote() : "");
    }

    private void handleSave() {
        if (selectedBodyId <= 0) {
            JOptionPane.showMessageDialog(this, "请选择观测星体",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDateTime obsTime;
        try {
            obsTime = LocalDateTime.parse(timeField.getText().trim(), DT_FMT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "时间格式错误，请使用 yyyy-MM-dd HH:mm",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Observation obs;
        if (existing != null) {
            obs = existing;
        } else {
            obs = new Observation();
            obs.setUserId(currentUser.getUserId());
        }
        obs.setBodyId(selectedBodyId);
        obs.setObsTime(obsTime);

        int siteIdx = siteCombo.getSelectedIndex();
        if (siteIdx > 0 && siteIdx - 1 < siteList.size()) {
            obs.setSiteId(siteList.get(siteIdx - 1).getSiteId());
        } else {
            obs.setSiteId(null);
        }

        String weather = (String) weatherCombo.getSelectedItem();
        obs.setWeather(weather != null && !weather.isEmpty() ? weather : null);
        obs.setSeeing(seeingCombo.getSelectedIndex() + 1);

        String mpDisplay = (String) moonPhaseCombo.getSelectedItem();
        if (mpDisplay != null && !mpDisplay.isEmpty()) {
            obs.setMoonPhase(MoonPhase.fromString(mpDisplay));
        } else {
            obs.setMoonPhase(null);
        }

        obs.setNote(noteArea.getText().trim().isEmpty() ? null : noteArea.getText().trim());

        BigDecimal userLat = currentUser.getDefaultLat();
        BigDecimal userLon = currentUser.getDefaultLon();
        obs.setLocationLat(userLat);
        obs.setLocationLon(userLon);

        List<Integer> equipIds = new ArrayList<>();
        for (int i = 0; i < equipCheckBoxes.size(); i++) {
            if (equipCheckBoxes.get(i).isSelected()) {
                equipIds.add(equipmentData.get(i).getEquipId());
            }
        }

        List<String> tagNames = new ArrayList<>();
        for (JToggleButton btn : tagButtons) {
            if (btn.isSelected()) {
                tagNames.add(btn.getText());
            }
        }

        ServiceResult result;
        if (existing == null) {
            result = obsService.addObservation(obs, equipIds, tagNames);
        } else {
            result = obsService.updateObservation(obs, equipIds, tagNames);
        }

        if (result.isSuccess()) {
            if (onSaved != null) {
                onSaved.run();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
