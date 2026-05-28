package com.astrolog.ui.panel;

import com.astrolog.config.ThemeConfig;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.ConstellationInfo;
import com.astrolog.model.User;
import com.astrolog.service.ConstellationService;
import com.astrolog.ui.component.StarMapCanvas;
import com.astrolog.ui.component.ThemeManager;
import com.astrolog.util.JsonDataLoader;

import static java.awt.Component.CENTER_ALIGNMENT;
import static java.awt.Component.LEFT_ALIGNMENT;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConstellationPanel extends JPanel {

    private static final String[] COLUMNS = {"星座", "缩写", "面积(平方度)", "最亮星", "最佳季节"};

    private final User currentUser;
    private final ConstellationService constellationService;
    private final List<ConstellationInfo> allConstellations;

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private JComboBox<String> seasonCombo;
    private JPanel centerPanel;
    private CardLayout centerLayout;
    private JPanel detailPanel;
    private JLabel detailTitle;
    private JTextArea detailArea;
    private JButton starMapBtn;
    private ConstellationInfo selectedConstellation;
    private boolean gridMode;
    private final ThemeConfig.Theme theme;

    public ConstellationPanel(User currentUser) {
        this.currentUser = currentUser;
        this.constellationService = new ConstellationService();
        this.allConstellations = constellationService.getAll();
        this.theme = ThemeManager.getInstance().getCurrentTheme();

        setLayout(new BorderLayout(12, 12));
        setBackground(theme.bg());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildSearchPanel(), BorderLayout.NORTH);
        centerLayout = new CardLayout();
        centerPanel = new JPanel(centerLayout);
        centerPanel.setOpaque(false);
        centerPanel.add(buildListView(), "list");
        centerPanel.add(buildGridView(), "grid");
        add(centerPanel, BorderLayout.CENTER);
        add(buildDetailPanel(), BorderLayout.EAST);

        refreshView();
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        panel.setOpaque(false);

        JLabel sl = new JLabel("搜索:"); sl.setForeground(theme.fg()); panel.add(sl);
        searchField = new JTextField(15);
        panel.add(searchField);

        JLabel sel = new JLabel("季节:"); sel.setForeground(theme.fg()); panel.add(sel);
        seasonCombo = new JComboBox<>(new String[]{"全部", "春", "夏", "秋", "冬"});
        panel.add(seasonCombo);

        JButton searchBtn = new JButton("搜索");
        panel.add(searchBtn);

        JButton listBtn = new JButton("列表视图");
        JButton gridBtn = new JButton("网格视图");
        panel.add(listBtn);
        panel.add(gridBtn);

        searchBtn.addActionListener(e -> refreshView());
        searchField.addActionListener(e -> refreshView());
        seasonCombo.addActionListener(e -> refreshView());

        listBtn.addActionListener(e -> {
            gridMode = false;
            centerLayout.show(centerPanel, "list");
            refreshView();
        });

        gridBtn.addActionListener(e -> {
            gridMode = true;
            centerLayout.show(centerPanel, "grid");
            refreshView();
        });

        return panel;
    }

    private JScrollPane buildListView() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                String name = (String) tableModel.getValueAt(modelRow, 0);
                showDetail(name);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("星座列表"));
        scrollPane.setPreferredSize(new Dimension(480, 420));
        return scrollPane;
    }

    private JScrollPane buildGridView() {
        JPanel grid = new JPanel(new GridLayout(11, 8, 6, 6));
        grid.setBackground(theme.bg());

        allConstellations.forEach(c -> {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            card.setBackground(theme.tableBg());
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel nameLabel = new JLabel(c.getName()); nameLabel.setForeground(theme.fg());
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            nameLabel.setAlignmentX(CENTER_ALIGNMENT);

            JLabel abbrLabel = new JLabel(c.getAbbreviation());
            abbrLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            abbrLabel.setForeground(Color.GRAY);
            abbrLabel.setAlignmentX(CENTER_ALIGNMENT);

            card.add(Box.createVerticalGlue());
            card.add(nameLabel);
            card.add(abbrLabel);
            card.add(Box.createVerticalGlue());

            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showDetail(c.getName());
                }
            });

            grid.add(card);
        });

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(BorderFactory.createTitledBorder("星座网格"));
        scrollPane.setPreferredSize(new Dimension(480, 420));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        return scrollPane;
    }

    private JPanel buildDetailPanel() {
        detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setPreferredSize(new Dimension(280, 420));
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("星座详情"),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        detailPanel.setBackground(theme.tableBg());

        detailTitle = new JLabel("请选择一个星座");
        detailTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        detailTitle.setForeground(theme.fg());
        detailTitle.setAlignmentX(LEFT_ALIGNMENT);

        detailArea = new JTextArea();
        detailArea.setWrapStyleWord(true);
        detailArea.setLineWrap(true);
        detailArea.setEditable(false);
        detailArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        detailArea.setBackground(theme.tableBg());
        detailArea.setForeground(theme.fg());
        detailArea.setAlignmentX(LEFT_ALIGNMENT);

        starMapBtn = new JButton("在星图中查看");
        starMapBtn.setAlignmentX(LEFT_ALIGNMENT);
        starMapBtn.setEnabled(false);
        starMapBtn.addActionListener(e -> openStarMap());

        detailPanel.add(detailTitle);
        detailPanel.add(Box.createVerticalStrut(12));
        detailPanel.add(detailArea);
        detailPanel.add(Box.createVerticalStrut(12));
        detailPanel.add(starMapBtn);
        detailPanel.add(Box.createVerticalGlue());

        return detailPanel;
    }

    private void refreshView() {
        String keyword = searchField.getText().trim();
        String season = (String) seasonCombo.getSelectedItem();

        List<ConstellationInfo> filtered = allConstellations.stream()
            .filter(c -> {
                if (season != null && !"全部".equals(season) && !c.getSeason().equals(season)) {
                    return false;
                }
                if (!keyword.isEmpty()) {
                    return c.getName().contains(keyword)
                        || c.getAbbreviation().toLowerCase().contains(keyword.toLowerCase())
                        || c.getBrightestStar().contains(keyword);
                }
                return true;
            })
            .collect(Collectors.toList());

        if (gridMode) {
            refreshGridView(filtered);
        } else {
            tableModel.setRowCount(0);
            for (ConstellationInfo c : filtered) {
                tableModel.addRow(new Object[]{
                    c.getName(), c.getAbbreviation(), c.getArea(),
                    c.getBrightestStar(), c.getSeason()
                });
            }
        }
    }

    private void refreshGridView(List<ConstellationInfo> filtered) {
        JPanel oldGrid = (JPanel) ((JScrollPane) centerPanel.getComponent(1))
            .getViewport().getView();
        oldGrid.removeAll();

        filtered.forEach(c -> {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            card.setBackground(theme.tableBg());
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel nameLabel = new JLabel(c.getName()); nameLabel.setForeground(theme.fg());
            nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            nameLabel.setAlignmentX(CENTER_ALIGNMENT);

            JLabel abbrLabel = new JLabel(c.getAbbreviation());
            abbrLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            abbrLabel.setForeground(Color.GRAY);
            abbrLabel.setAlignmentX(CENTER_ALIGNMENT);

            card.add(Box.createVerticalGlue());
            card.add(nameLabel);
            card.add(abbrLabel);
            card.add(Box.createVerticalGlue());

            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showDetail(c.getName());
                }
            });

            oldGrid.add(card);
        });

        oldGrid.revalidate();
        oldGrid.repaint();
    }

    private void showDetail(String name) {
        ConstellationInfo c = constellationService.getByName(name);
        if (c == null) return;

        selectedConstellation = c;
        detailTitle.setText(c.getName() + " (" + c.getAbbreviation() + ")");

        StringBuilder sb = new StringBuilder();
        sb.append("面积: ").append(c.getArea()).append(" 平方度\n");
        sb.append("最亮星: ").append(c.getBrightestStar()).append("\n");
        sb.append("最佳观测季节: ").append(c.getSeason()).append("\n");
        sb.append("\n").append(c.getMythology());

        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
        starMapBtn.setEnabled(true);
    }

    private void openStarMap() {
        List<CelestialBody> allBodies = constellationService.getAllBodies();

        ConstellationInfo c = selectedConstellation;
        JFrame starFrame = new JFrame("星图 — " + c.getName());
        starFrame.setSize(640, 680);
        starFrame.setLocationRelativeTo(this);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(10, 10, 40));

        JLabel titleLabel = new JLabel(c.getName() + " (" + c.getAbbreviation() + ")", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setForeground(new Color(200, 200, 255));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        titleLabel.setBackground(new Color(10, 10, 40));
        titleLabel.setOpaque(true);
        container.add(titleLabel, BorderLayout.NORTH);

        List<CelestialBody> emptyObserved = Collections.emptyList();
        List<ConstellationInfo> allConstellations = JsonDataLoader.loadList(
            "data/constellations.json", ConstellationInfo[].class);
        StarMapCanvas canvas = new StarMapCanvas(emptyObserved, allBodies, allConstellations);
        canvas.setHighlightedConstellation(c.getName());
        container.add(canvas, BorderLayout.CENTER);

        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> starFrame.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(10, 10, 40));
        btnPanel.add(closeBtn);
        container.add(btnPanel, BorderLayout.SOUTH);

        starFrame.add(container);
        starFrame.setVisible(true);
    }
}
