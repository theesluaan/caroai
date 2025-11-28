package com.rianta9.caro.view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.rianta9.caro.bean.RoundedBorder;
import com.rianta9.caro.bean.Setting;
import com.rianta9.caro.bo.CaroAI;
import com.rianta9.caro.dao.SettingDao;
import com.rianta9.caro.dao.ActivityLog;
import com.rianta9.caro.values.Value;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Paths;

import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.ButtonGroup;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JComboBox;

/**
 * @author rianta9
 */
public class App extends JFrame implements MouseListener{

	private JPanel contentPane;
	private JPanel TableCells; // Panel chứa ma trận cells
	private Border cellBorder; // tạo đường viền của mỗi cell
	private JLabel[][] cell; // Ma trận cells
	private JLabel userClickedCell; // cell được user click chọn
	private JLabel aiClickedCell; // cell được AI click chọn
	private JLabel lblUserScore; // điểm của User
	private JLabel lblAIScore; // điểm của AI
	private JButton btnUndo; // nút undo
	private JButton btnRedo; // nút redo
	private JComboBox<String> cmbDifficulty; // chọn mức độ khó
	private JButton btnStatistics; // nút xem thống kê
	private JButton btnHistory; // nút xem lịch sử
	
	private CaroAI caro;
	private Setting setting;
	private Notification notification;
	private long gameStartTime; // thời gian bắt đầu game
	
	public static final int TEXT_CELL_SIZE = Value.TEXT_CELL_SIZE; // cỡ chữ trong mỗi cell
	
	private String currentPath; // đường dẫn hiện tại của project
	
	private final ButtonGroup buttonGroup = new ButtonGroup();
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App frame = new App();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Tạo game mới, clear màn chơi cũ
	 */
	public void newGame() {
		setting = SettingDao.LoadSettingInfo();
		caro = new CaroAI(setting.getMode(), setting.getDifficulty());
		userClickedCell = null;
		aiClickedCell = null;
		gameStartTime = System.currentTimeMillis(); // ghi nhận thời gian bắt đầu
		
		for (int i = 0; i < Value.SIZE; i++) {
			for (int j = 0; j < Value.SIZE; j++) {
				cell[i][j].setBackground(setting.getCellColor());
				cell[i][j].setForeground(setting.getxColor());
				cell[i][j].setText("");
			}
		}
		
		// Cập nhật trạng thái nút undo/redo
		updateUndoRedoButtons();
		
		if(setting.getMode() == 1) {
			// cập nhật nước đi của AI
			int x = caro.getNextX();
			int y = caro.getNextY();
			updateTableCells(x, y, Value.AI_VALUE);
		}
	}
	
	public void updateTableCells(int x, int y, int player) {
		if(player == Value.AI_VALUE) {
			if(aiClickedCell != null) {
				aiClickedCell.setBackground(setting.getCellColor()); // đặt lại màu clickedCell cũ
			}
			aiClickedCell = cell[x][y];
			aiClickedCell.setForeground(setting.getoColor());
			aiClickedCell.setText("O");
			aiClickedCell.setBackground(Value.CLICK_CELL_COLOR); // làm nổi bật cell được AI chọn
		}
		else {
			cell[x][y].setBackground(setting.getCellColor()); // đặt lại màu clickedCell cũ
			cell[x][y].setText("X");
		}
	}
	
	public Notification getNotificationInstance() {
		if(notification == null) notification = new Notification();
		return notification;
	}
	
	/**
	 * Cập nhật trạng thái nút undo/redo
	 */
	public void updateUndoRedoButtons() {
		if(btnUndo != null) btnUndo.setEnabled(caro.canUndo());
		if(btnRedo != null) btnRedo.setEnabled(caro.canRedo());
	}
	
	/**
	 * Lưu kết quả game
	 */
	private void saveGameResult(int winner) {
		long gameTime = (System.currentTimeMillis() - gameStartTime) / 1000;
		int userMoves = caro.getUserMoveCount();
		int aiMoves = caro.getAIMoveCount();
		ActivityLog.saveGameResult(winner, userMoves, aiMoves, gameTime);
	}
	
	/**
	 * Create the frame.
	 */
	public App() {
		/*--------------Set các giá trị mặc định--------------*/
		setResizable(true); // Cho phép thay đổi kích thước cửa sổ
		currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
		setIconImage(Toolkit.getDefaultToolkit().getImage(currentPath+"\\file\\img\\icon.png"));
		setTitle("Cờ Caro - Game AI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Kích thước cửa sổ có thể tùy chỉnh
		int windowWidth = Value.SIZE*Value.CELL_WIDTH+3*Value.MARGIN+280;
		int windowHeight = Value.SIZE*Value.CELL_WIDTH+150;
		setSize(windowWidth, windowHeight);
		
		// Đặt kích thước tối thiểu
		setMinimumSize(new java.awt.Dimension(800, 600));
		
		// Đặt kích thước tối đa (tùy chọn)
		// setMaximumSize(new java.awt.Dimension(1200, 900));
		
		// Căn giữa màn hình
		setLocationRelativeTo(null);
		
		// Cho phép maximize window
		setExtendedState(JFrame.NORMAL);
		
		// Tạo menu bar
		createMenuBar();
		
		// Thêm keyboard shortcuts
		setupKeyboardShortcuts();
		
		// Thêm ComponentListener để xử lý resize
		addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				// Delay một chút để tránh gọi quá nhiều lần
				javax.swing.Timer timer = new javax.swing.Timer(100, evt -> {
					updateBoardSize();
				});
				timer.setRepeats(false);
				timer.start();
			}
		});

		setting = SettingDao.LoadSettingInfo();
		caro = new CaroAI(setting.getMode()); // khởi tạo CaroAI
		cellBorder = new LineBorder(Color.black, 1); // tạo border cho mỗi cell trong ma trận
		
		/*------------------Tạo các đối tượng------------------*/
		contentPane = new JPanel();
		contentPane.setBackground(setting.getBackgroundColor());
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		// Sử dụng BorderLayout để responsive
		contentPane.setLayout(new BorderLayout());
		
		// Panel chính chứa bàn cờ và control panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(setting.getBackgroundColor());
		
		// Panel bàn cờ - sẽ tự động resize
		TableCells = new JPanel();
		TableCells.setBackground(new Color(255, 255, 255));
		TableCells.setLayout(new GridLayout(Value.SIZE, Value.SIZE, 0, 0));
		TableCells.setFont(new Font("Tahoma", Font.PLAIN, 14));
		TableCells.setPreferredSize(new java.awt.Dimension(Value.SIZE*Value.CELL_WIDTH, Value.SIZE*Value.CELL_WIDTH));
		TableCells.setMinimumSize(new java.awt.Dimension(Value.SIZE*20, Value.SIZE*20)); // Kích thước tối thiểu
		TableCells.setMaximumSize(new java.awt.Dimension(Value.SIZE*50, Value.SIZE*50)); // Kích thước tối đa
		
		// Thêm bàn cờ vào center
		mainPanel.add(TableCells, BorderLayout.CENTER);
		
		// Tạo ma trận và add vào TableCells
		cell = new JLabel[Value.SIZE][Value.SIZE];
		for (int i = 0; i < Value.SIZE; i++) {
			for (int j = 0; j < Value.SIZE; j++) {
				cell[i][j] = new JLabel();
				cell[i][j].setSize(Value.CELL_WIDTH, Value.CELL_WIDTH); // kích cỡ mỗi cell
				cell[i][j].setOpaque(true);
				cell[i][j].setBorder(cellBorder);
				cell[i][j].setFont(new Font("Arial", Font.BOLD, TEXT_CELL_SIZE));
				cell[i][j].setBackground(setting.getCellColor());
				cell[i][j].setForeground(setting.getxColor());
				cell[i][j].setHorizontalAlignment(SwingConstants.CENTER); // căn giữa chữ
				cell[i][j].addMouseListener(this); // add hàm bắt sự kiện click chuột
				TableCells.add(cell[i][j]); // add cell vào TableCells
			}
		}
		// Nếu chế độ AI đánh trước => cập nhật lượt đầu của AI
		if(setting.getMode() == 1) updateTableCells(caro.getNextX(), caro.getNextX(), Value.AI_VALUE);
		
		// Control Panel - sẽ đặt bên phải
		JPanel view = new JPanel();
		view.setBackground(new Color(200, 230, 201));
		view.setForeground(Color.BLACK);
		view.setLayout(null);
		view.setPreferredSize(new java.awt.Dimension(300, 650)); // Tăng chiều cao để chứa tất cả nút
		view.setMinimumSize(new java.awt.Dimension(250, 450));
		
		// Thêm control panel vào bên phải
		mainPanel.add(view, BorderLayout.EAST);
		
		// Thêm main panel vào content pane
		contentPane.add(mainPanel, BorderLayout.CENTER);
		
		JLabel lbltitle = new JLabel("CỜ CARO AI");
		lbltitle.setHorizontalAlignment(SwingConstants.CENTER);
		lbltitle.setFont(new Font("Segoe UI Black", Font.BOLD, 40));
		lbltitle.setForeground(new Color(255, 69, 0));
		lbltitle.setBounds(10, 11, 254, 50);
		view.add(lbltitle);

		JLabel lblMode = new JLabel("Chế độ:");
		lblMode.setHorizontalAlignment(SwingConstants.LEFT);
		lblMode.setForeground(new Color(0, 0, 139));
		lblMode.setFont(new Font("Arial", Font.PLAIN, 16));
		lblMode.setBounds(10, 162, 254, 20);
		view.add(lblMode);
		
		JButton btnNewGame = new JButton("Chơi mới");
		btnNewGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, "Bạn có muốn chơi mới?", "Xác nhận", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.YES_OPTION) newGame();
			}
		});
		btnNewGame.setFont(new Font("Arial", Font.BOLD, 12));
		btnNewGame.setBounds(30, 470, 89, 37); // Di chuyển xuống dưới separator
		btnNewGame.setBackground(new Color(255, 20, 147));
		btnNewGame.setForeground(new Color(85, 107, 47));
		btnNewGame.setOpaque(false);
		btnNewGame.setBorder(new RoundedBorder(10));
		view.add(btnNewGame);
		
		JButton btnExitGame = new JButton("Thoát trò chơi");
		btnExitGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, "Bạn có muốn đóng trò chơi?", "Xác nhận", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.YES_OPTION) System.exit(0); // thoát game
			}
		});
		btnExitGame.setFont(new Font("Arial", Font.BOLD, 12));
		btnExitGame.setOpaque(false);
		btnExitGame.setForeground(new Color(85, 107, 47));
		btnExitGame.setBorder(new RoundedBorder(10));
		btnExitGame.setBackground(new Color(255, 20, 147));
		btnExitGame.setBounds(156, 470, 89, 37); // Di chuyển xuống dưới separator
		view.add(btnExitGame);
		
		// Nút Undo
		btnUndo = new JButton("Hoàn tác");
		btnUndo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				com.rianta9.caro.bean.Cell undoneCell = caro.undo();
				if(undoneCell != null) {
					cell[undoneCell.getX()][undoneCell.getY()].setText("");
					cell[undoneCell.getX()][undoneCell.getY()].setBackground(setting.getCellColor());
					updateUndoRedoButtons();
					// Nếu undo nước đi của AI, cần undo cả nước đi của User trước đó
					if(undoneCell.getSelected() == Value.AI_VALUE) {
						com.rianta9.caro.bean.Cell userUndoneCell = caro.undo();
						if(userUndoneCell != null) {
							cell[userUndoneCell.getX()][userUndoneCell.getY()].setText("");
							cell[userUndoneCell.getX()][userUndoneCell.getY()].setBackground(setting.getCellColor());
						}
					}
					updateUndoRedoButtons();
				}
			}
		});
		btnUndo.setFont(new Font("Arial", Font.BOLD, 12));
		btnUndo.setOpaque(false);
		btnUndo.setForeground(new Color(85, 107, 47));
		btnUndo.setBorder(new RoundedBorder(10));
		btnUndo.setBackground(new Color(255, 20, 147));
		btnUndo.setBounds(30, 400, 89, 37);
		btnUndo.setEnabled(false);
		view.add(btnUndo);
		
		// Nút Redo
		btnRedo = new JButton("Làm lại");
		btnRedo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				com.rianta9.caro.bean.Cell redoneCell = caro.redo();
				if(redoneCell != null) {
					updateTableCells(redoneCell.getX(), redoneCell.getY(), redoneCell.getSelected());
					updateUndoRedoButtons();
				}
			}
		});
		btnRedo.setFont(new Font("Arial", Font.BOLD, 12));
		btnRedo.setOpaque(false);
		btnRedo.setForeground(new Color(85, 107, 47));
		btnRedo.setBorder(new RoundedBorder(10));
		btnRedo.setBackground(new Color(255, 20, 147));
		btnRedo.setBounds(156, 400, 89, 37);
		btnRedo.setEnabled(false);
		view.add(btnRedo);
		
		JRadioButton rdbtnUserPlaysFirst = new JRadioButton("Người chơi trước");
		JRadioButton rdbtnAiPlaysFirst = new JRadioButton("AI chơi trước");
		if(setting.getMode() == 0) rdbtnUserPlaysFirst.setSelected(true);
		else rdbtnAiPlaysFirst.setSelected(true);
		
		rdbtnUserPlaysFirst.setForeground(new Color(107, 142, 35));
		rdbtnUserPlaysFirst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(setting.getMode() == 1) {
					int result = JOptionPane.showConfirmDialog(null, "Xác nhận đổi chế độ chơi?", "Xác nhận", JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.YES_OPTION) {
						setting.setMode(0); // cập nhật mode
						newGame(); // clear màn chơi cũ
					}
					else {
						rdbtnUserPlaysFirst.setSelected(false);
						rdbtnAiPlaysFirst.setSelected(true);
					}
				}
			}
		});
		rdbtnUserPlaysFirst.setFont(new Font("Arial", Font.PLAIN, 14));
		buttonGroup.add(rdbtnUserPlaysFirst);
		rdbtnUserPlaysFirst.setOpaque(false);
		rdbtnUserPlaysFirst.setBounds(26, 192, 232, 23);
		view.add(rdbtnUserPlaysFirst);
		
		rdbtnAiPlaysFirst.setForeground(new Color(107, 142, 35));
		rdbtnAiPlaysFirst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(setting.getMode() == 0) {
					int result = JOptionPane.showConfirmDialog(null, "Xác nhận đổi chế độ chơi?", "Xác nhận", JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.YES_OPTION) {
						setting.setMode(1); // cập nhật mode
						newGame(); // clear màn chơi cũ
					}
					else {
						rdbtnUserPlaysFirst.setSelected(true);
						rdbtnAiPlaysFirst.setSelected(false);
					}
				}
			}
		});
		rdbtnAiPlaysFirst.setFont(new Font("Arial", Font.PLAIN, 14));
		buttonGroup.add(rdbtnAiPlaysFirst);
		rdbtnAiPlaysFirst.setOpaque(false);
		rdbtnAiPlaysFirst.setBounds(26, 218, 232, 23);
		view.add(rdbtnAiPlaysFirst);
		
		// ComboBox chọn mức độ khó
		JLabel lblDifficulty = new JLabel("Mức:");
		lblDifficulty.setHorizontalAlignment(SwingConstants.LEFT);
		lblDifficulty.setForeground(new Color(0, 0, 139));
		lblDifficulty.setFont(new Font("Arial", Font.PLAIN, 16));
		lblDifficulty.setBounds(10, 250, 254, 20);
		view.add(lblDifficulty);
		
		cmbDifficulty = new JComboBox<String>();
		cmbDifficulty.addItem("Dễ");
		cmbDifficulty.addItem("Trung bình");
		cmbDifficulty.addItem("Khó");
		cmbDifficulty.addItem("Cao thủ");
		cmbDifficulty.setSelectedIndex(setting.getDifficulty() - 1);
		cmbDifficulty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int newDifficulty = cmbDifficulty.getSelectedIndex() + 1;
				if(setting.getDifficulty() != newDifficulty) {
					setting.setDifficulty(newDifficulty);
					caro.setDifficulty(newDifficulty);
				}
			}
		});
		cmbDifficulty.setFont(new Font("Arial", Font.PLAIN, 12));
		cmbDifficulty.setBounds(26, 275, 232, 25);
		view.add(cmbDifficulty);
		
		JLabel lblSetting = new JLabel("Màu sắc:");
		lblSetting.setHorizontalAlignment(SwingConstants.LEFT);
		lblSetting.setForeground(new Color(0, 0, 139));
		lblSetting.setFont(new Font("Arial", Font.PLAIN, 16));
		lblSetting.setBounds(10, 310, 254, 20);
		view.add(lblSetting);
		
		JSeparator separator = new JSeparator();
		separator.setBackground(Color.GRAY);
		separator.setForeground(Color.DARK_GRAY);
		separator.setBounds(10, 450, 254, 2); // Di chuyển xuống dưới nút Undo/Redo
		view.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setForeground(Color.DARK_GRAY);
		separator_1.setBackground(Color.GRAY);
		separator_1.setBounds(10, 149, 254, 2);
		view.add(separator_1);
		
		JLabel lblUser = new JLabel("Người chơi");
		lblUser.setForeground(new Color(220, 20, 60));
		lblUser.setFont(new Font("Arial", Font.BOLD, 16));
		lblUser.setHorizontalAlignment(SwingConstants.CENTER);
		lblUser.setBounds(10, 91, 122, 20);
		view.add(lblUser);
		
		JLabel lblAI = new JLabel("AI");
		lblAI.setForeground(new Color(0, 139, 139));
		lblAI.setFont(new Font("Arial", Font.BOLD, 16));
		lblAI.setHorizontalAlignment(SwingConstants.CENTER);
		lblAI.setBounds(142, 91, 122, 20);
		view.add(lblAI);
		
		lblUserScore = new JLabel("0");
		lblUserScore.setForeground(new Color(65, 105, 225));
		lblUserScore.setFont(new Font("Arial", Font.BOLD, 11));
		lblUserScore.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserScore.setBounds(10, 122, 122, 20);
		view.add(lblUserScore);
		
		lblAIScore = new JLabel("0");
		lblAIScore.setForeground(new Color(0, 128, 0));
		lblAIScore.setFont(new Font("Arial", Font.BOLD, 11));
		lblAIScore.setHorizontalAlignment(SwingConstants.CENTER);
		lblAIScore.setBounds(142, 122, 122, 20);
		view.add(lblAIScore);
		
		JButton btnInfo = new JButton("Thông tin");
		btnInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				notification = getNotificationInstance();
				notification.show("Thông tin", "Thông Tin", Value.INFO_MESSAGE);
			}
		});
		btnInfo.setOpaque(false);
		btnInfo.setForeground(new Color(85, 107, 47));
		btnInfo.setFont(new Font("Arial", Font.BOLD, 12));
		btnInfo.setBorder(new RoundedBorder(10));
		btnInfo.setBackground(new Color(255, 20, 147));
		btnInfo.setBounds(156, 520, 89, 37); // Di chuyển xuống dưới
		view.add(btnInfo);
		
		JButton btnIntroduce = new JButton("Về game");
		btnIntroduce.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				notification = getNotificationInstance();
				notification.show("Thông tin", "Giới Thiệu", Value.INTRODUCE_MESSAGE);
			}
		});
		btnIntroduce.setOpaque(false);
		btnIntroduce.setForeground(new Color(85, 107, 47));
		btnIntroduce.setFont(new Font("Arial", Font.BOLD, 12));
		btnIntroduce.setBorder(new RoundedBorder(10));
		btnIntroduce.setBackground(new Color(255, 20, 147));
		btnIntroduce.setBounds(30, 520, 89, 37); // Di chuyển xuống dưới
		view.add(btnIntroduce);
		
		// Nút Statistics
		btnStatistics = new JButton("Thống kê");
		btnStatistics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] stats = ActivityLog.getStatistics();
				String statsText = String.format(
					"THỐNG KÊ GAME\n\n" +
					"Tổng số game: %d\n" +
					"Người thắng: %d\n" +
					"AI thắng: %d\n" +
					"Hòa: %d\n\n" +
					"Tỷ lệ thắng của người chơi: %.1f%%",
					stats[3], stats[0], stats[1], stats[2],
					stats[3] > 0 ? (double)stats[0] / stats[3] * 100 : 0
				);
				notification = getNotificationInstance();
				notification.show("Thống kê", "Thống Kê Game", statsText);
			}
		});
		btnStatistics.setOpaque(false);
		btnStatistics.setForeground(new Color(85, 107, 47));
		btnStatistics.setFont(new Font("Arial", Font.BOLD, 12));
		btnStatistics.setBorder(new RoundedBorder(10));
		btnStatistics.setBackground(new Color(255, 20, 147));
		btnStatistics.setBounds(156, 560, 89, 37); // Di chuyển xuống dưới
		view.add(btnStatistics);
		
		// Nút History
		btnHistory = new JButton("Lịch sử");
		btnHistory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> history = ActivityLog.getGameHistory();
				StringBuilder historyText = new StringBuilder("LỊCH SỬ GAME\n\n");
				if(history.isEmpty()) {
					historyText.append("Chưa có lịch sử game nào.");
				} else {
					// Hiển thị 10 game gần nhất
					int start = Math.max(0, history.size() - 10);
					for(int i = start; i < history.size(); i++) {
						historyText.append(history.get(i)).append("\n");
					}
				}
				notification = getNotificationInstance();
				notification.show("Lịch sử", "Lịch Sử Game", historyText.toString());
			}
		});
		btnHistory.setOpaque(false);
		btnHistory.setForeground(new Color(85, 107, 47));
		btnHistory.setFont(new Font("Arial", Font.BOLD, 12));
		btnHistory.setBorder(new RoundedBorder(10));
		btnHistory.setBackground(new Color(255, 20, 147));
		btnHistory.setBounds(30, 560, 89, 37); // Di chuyển xuống dưới
		view.add(btnHistory);
		
		JButton btnXColor = new JButton("Màu X");
		btnXColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Color color = JColorChooser.showDialog(App.this, 
	                    "Chọn màu chữ của User", setting.getxColor()); 
				if(color != null) {
					setting.setxColor(color);
					newGame();
				}
			}
		});
		btnXColor.setOpaque(false);
		btnXColor.setForeground(new Color(85, 107, 47));
		btnXColor.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnXColor.setBorder(new RoundedBorder(10));
		btnXColor.setBackground(new Color(255, 20, 147));
		btnXColor.setBounds(30, 340, 89, 28);
		view.add(btnXColor);
		
		JButton btnBackgroundColor = new JButton("Màu Nền");
		btnBackgroundColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(App.this, 
	                    "Chọn màu nền", setting.getBackgroundColor()); 
				if(color != null) {
					setting.setBackgroundColor(color);
					contentPane.setBackground(color);
				}
			}
		});
		btnBackgroundColor.setOpaque(false);
		btnBackgroundColor.setForeground(new Color(85, 107, 47));
		btnBackgroundColor.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnBackgroundColor.setBorder(new RoundedBorder(10));
		btnBackgroundColor.setBackground(new Color(255, 20, 147));
		btnBackgroundColor.setBounds(30, 375, 89, 28);
		view.add(btnBackgroundColor);
		
		JButton btnOColor = new JButton("Màu O");
		btnOColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(App.this, 
	                    "Chọn màu chữ của AI", setting.getoColor()); 
				if(color != null) {
					setting.setoColor(color);
					newGame();
				}
			}
		});
		btnOColor.setOpaque(false);
		btnOColor.setForeground(new Color(85, 107, 47));
		btnOColor.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnOColor.setBorder(new RoundedBorder(10));
		btnOColor.setBackground(new Color(255, 20, 147));
		btnOColor.setBounds(156, 340, 89, 28);
		view.add(btnOColor);
		
		JButton btnCellColor = new JButton("Màu Ô");
		btnCellColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color color = JColorChooser.showDialog(App.this, 
	                    "Chọn màu của mỗi ô vuông", setting.getCellColor()); 
				if(color != null) {
					setting.setCellColor(color);
					newGame();
				}
			}
		});
		btnCellColor.setOpaque(false);
		btnCellColor.setForeground(new Color(85, 107, 47));
		btnCellColor.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnCellColor.setBorder(new RoundedBorder(10));
		btnCellColor.setBackground(new Color(255, 20, 147));
		btnCellColor.setBounds(156, 375, 89, 28);
		view.add(btnCellColor);
		
		
	}

	

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent element) {
		int x = -1, y = -1; // lưu tọa độ
		// lấy tọa độ user click
		for (int i = 0; i < Value.SIZE; i++) {
			boolean fl = false;
			for (int j = 0; j < Value.SIZE; j++) {
				if(cell[i][j] == element.getSource()) { // nếu người dùng click vào ô này
					x = i;
					y = j;
					fl = true;
					break;
				}
			}
			if(fl) break; // dừng kiểm tra
		}
		
		/*kiểm tra số lần click của user*/	
		if(element.getClickCount() == 1) { // người dùng click dạo(click 1 lần)
			if(userClickedCell != null && userClickedCell != aiClickedCell) {
				userClickedCell.setBackground(setting.getCellColor()); // đặt lại màu clickedCell cũ
			}
			userClickedCell = cell[x][y]; // cập nhật clickedCell
			userClickedCell.setBackground(Value.CLICK_CELL_COLOR); // làm nổi bật ô được click
		}
		
		else if(element.getClickCount() == 2) { // người dùng chọn đánh ô này
			if(caro.isClickable(x, y)) {//nếu ô này chưa được đánh
				caro.update(x, y, Value.USER_VALUE); // update matrix
		        System.out.println("\n----------------------------------------------------------------------");
				System.out.println("Nước đi của User:" + x + " " + y);
				
				// Cập nhật bước đi của User
				updateTableCells(x, y, Value.USER_VALUE);
				updateUndoRedoButtons(); // Cập nhật trạng thái nút undo/redo
				// Kiểm tra trạng thái của bàn cờ sau khi User đánh
				if(checkResult(Value.USER_VALUE)) return;				
				// Nếu user không thắng và bàn cờ chưa full thì đến lượt AI đánh
				caro.nextStep();
				
				// Cập nhật bước đi của AI
				x = caro.getNextX();
				y = caro.getNextY();
				updateTableCells(x, y, Value.AI_VALUE);
				updateUndoRedoButtons(); // Cập nhật trạng thái nút undo/redo
				
				// Kiểm tra trạng thái của bàn cờ sau khi AI đánh
				if(checkResult(Value.AI_VALUE)) return;
			}
		}
	}

	/**
	 * 
	 */
	private boolean checkResult(int player) {
		if(player == Value.USER_VALUE) {
			boolean result = caro.checkWinner(Value.USER_VALUE);
			if(result == true) {
				System.out.println("Người chơi thắng!");
				saveGameResult(Value.USER_VALUE); // Lưu kết quả
				JOptionPane.showMessageDialog(null, "Bạn đã thắng!");
				int currentPoint = Integer.valueOf(lblUserScore.getText())+1;
				lblUserScore.setText(String.valueOf(currentPoint));
				newGame();
				return true; // kết thúc màn chơi
			}
		}
		else {
			boolean result = caro.checkWinner(Value.AI_VALUE);
			if(result == true) {
				System.out.println("AI thắng!");
				saveGameResult(Value.AI_VALUE); // Lưu kết quả
				JOptionPane.showMessageDialog(null, "AI đã thắng!");
				int currentPoint = Integer.valueOf(lblAIScore.getText())+1;
				lblAIScore.setText(String.valueOf(currentPoint));
				newGame();
				return true;
			}
		}
		if(caro.isOver()) {
			System.out.println("Hòa!");
			saveGameResult(Value.DRAW_VALUE); // Lưu kết quả hòa
			JOptionPane.showMessageDialog(null, "Hòa!");
			newGame();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Tạo menu bar với các tùy chọn window
	 */
	private void createMenuBar() {
		javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
		
		// Menu Game
		javax.swing.JMenu gameMenu = new javax.swing.JMenu("Game");
		
		javax.swing.JMenuItem newGameItem = new javax.swing.JMenuItem("Game mới");
		newGameItem.addActionListener(e -> {
			int result = JOptionPane.showConfirmDialog(null, 
				"Bạn có muốn chơi mới?", "Xác nhận", JOptionPane.YES_NO_OPTION);
			if(result == JOptionPane.YES_OPTION) newGame();
		});
		gameMenu.add(newGameItem);
		
		gameMenu.addSeparator();
		
		javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem("Đóng");
		exitItem.addActionListener(e -> {
			int result = JOptionPane.showConfirmDialog(null, 
				"Bạn có muốn đóng trò chơi?", "Xác nhận", JOptionPane.YES_NO_OPTION);
			if(result == JOptionPane.YES_OPTION) System.exit(0);
		});
		gameMenu.add(exitItem);
		
		menuBar.add(gameMenu);
		
		// Menu Window
		javax.swing.JMenu windowMenu = new javax.swing.JMenu("Window");
		
		javax.swing.JMenuItem minimizeItem = new javax.swing.JMenuItem("Minimize");
		minimizeItem.addActionListener(e -> setState(JFrame.ICONIFIED));
		windowMenu.add(minimizeItem);
		
		javax.swing.JMenuItem maximizeItem = new javax.swing.JMenuItem("Maximize");
		maximizeItem.addActionListener(e -> setExtendedState(JFrame.MAXIMIZED_BOTH));
		windowMenu.add(maximizeItem);
		
		javax.swing.JMenuItem restoreItem = new javax.swing.JMenuItem("Restore");
		restoreItem.addActionListener(e -> setExtendedState(JFrame.NORMAL));
		windowMenu.add(restoreItem);
		
		windowMenu.addSeparator();
		
		javax.swing.JMenuItem centerItem = new javax.swing.JMenuItem("Center Window");
		centerItem.addActionListener(e -> setLocationRelativeTo(null));
		windowMenu.add(centerItem);
		
		menuBar.add(windowMenu);
		
		// Menu Help
		javax.swing.JMenu helpMenu = new javax.swing.JMenu("Tra cứu");
		
		javax.swing.JMenuItem aboutItem = new javax.swing.JMenuItem("về chúng tôi");
		aboutItem.addActionListener(e -> {
			notification = getNotificationInstance();
			notification.show("About", "Thông Tin", Value.INFO_MESSAGE);
		});
		helpMenu.add(aboutItem);
		
		javax.swing.JMenuItem rulesItem = new javax.swing.JMenuItem("Về game");
		rulesItem.addActionListener(e -> {
			notification = getNotificationInstance();
			notification.show("Rules", "Giới Thiệu", Value.INTRODUCE_MESSAGE);
		});
		helpMenu.add(rulesItem);
		
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
	}
	
	/**
	 * Thiết lập keyboard shortcuts cho window
	 */
	private void setupKeyboardShortcuts() {
		// Tạo InputMap và ActionMap
		javax.swing.InputMap inputMap = getRootPane().getInputMap(
			javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
		javax.swing.ActionMap actionMap = getRootPane().getActionMap();
		
		// Ctrl+N: New Game
		inputMap.put(javax.swing.KeyStroke.getKeyStroke("ctrl N"), "newGame");
		actionMap.put("newGame", new javax.swing.AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, 
					"Bạn có muốn chơi mới?", "Xác nhận", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.YES_OPTION) newGame();
			}
		});
		
		// Ctrl+Z: Undo
		inputMap.put(javax.swing.KeyStroke.getKeyStroke("ctrl Z"), "undo");
		actionMap.put("undo", new javax.swing.AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if(btnUndo.isEnabled()) {
					btnUndo.doClick();
				}
			}
		});
		
		// Ctrl+Y: Redo
		inputMap.put(javax.swing.KeyStroke.getKeyStroke("ctrl Y"), "redo");
		actionMap.put("redo", new javax.swing.AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if(btnRedo.isEnabled()) {
					btnRedo.doClick();
				}
			}
		});
		
		// F11: Toggle Fullscreen
		inputMap.put(javax.swing.KeyStroke.getKeyStroke("F11"), "toggleFullscreen");
		actionMap.put("toggleFullscreen", new javax.swing.AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if(getExtendedState() == JFrame.MAXIMIZED_BOTH) {
					setExtendedState(JFrame.NORMAL);
				} else {
					setExtendedState(JFrame.MAXIMIZED_BOTH);
				}
			}
		});
		
		// Alt+F4: Exit
		inputMap.put(javax.swing.KeyStroke.getKeyStroke("alt F4"), "exit");
		actionMap.put("exit", new javax.swing.AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, 
					"Bạn có muốn đóng trò chơi?", "Xác nhận", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.YES_OPTION) System.exit(0);
			}
		});
		
		// Ctrl+M: Minimize
		inputMap.put(javax.swing.KeyStroke.getKeyStroke("ctrl M"), "minimize");
		actionMap.put("minimize", new javax.swing.AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setState(JFrame.ICONIFIED);
			}
		});
	}
	
	/**
	 * Cập nhật kích thước bàn cờ khi window resize
	 */
	private void updateBoardSize() {
		if(TableCells != null) {
			// Lấy kích thước hiện tại của window
			int windowWidth = getWidth();
			int windowHeight = getHeight();
			
			// Tính kích thước mới cho bàn cờ (trừ đi control panel ~300px)
			int availableWidth = windowWidth - 320; // 300px control panel + 20px margin
			int availableHeight = windowHeight - 100; // 100px cho menu và margin
			
			// Đảm bảo kích thước tối thiểu
			availableWidth = Math.max(availableWidth, Value.SIZE * 20);
			availableHeight = Math.max(availableHeight, Value.SIZE * 20);
			
			// Tính kích thước ô mới
			int newCellSize = Math.min(availableWidth / Value.SIZE, availableHeight / Value.SIZE);
			newCellSize = Math.max(newCellSize, 15); // Kích thước tối thiểu 15px
			newCellSize = Math.min(newCellSize, 50); // Kích thước tối đa 50px
			
			// Cập nhật kích thước bàn cờ
			int boardSize = newCellSize * Value.SIZE;
			TableCells.setPreferredSize(new java.awt.Dimension(boardSize, boardSize));
			TableCells.setMinimumSize(new java.awt.Dimension(boardSize, boardSize));
			TableCells.setMaximumSize(new java.awt.Dimension(boardSize, boardSize));
			
			// Cập nhật font size cho các ô
			int fontSize = Math.max(12, newCellSize / 2);
			for (int i = 0; i < Value.SIZE; i++) {
				for (int j = 0; j < Value.SIZE; j++) {
					if(cell[i][j] != null) {
						cell[i][j].setFont(new Font("Arial", Font.BOLD, fontSize));
					}
				}
			}
			
			// Refresh layout
			TableCells.revalidate();
			TableCells.repaint();
		}
	}
}
