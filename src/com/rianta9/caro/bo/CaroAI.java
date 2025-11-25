package com.rianta9.caro.bo;
import java.util.ArrayList;
import java.util.Random;

import com.rianta9.caro.bean.Cell;
import com.rianta9.caro.bean.EvalCell;
import com.rianta9.caro.bean.State;
import com.rianta9.caro.values.Value;


/**
 * 
 * @author rianta9
 */

public class CaroAI {
    Random rand;
    private int nextX;
    private int nextY;
    private int mode;
    private int difficulty;
    private State root; // Trạng thái hiện tại của trò chơi
    private Heuristic heuristic;

    
    public CaroAI(int mode) {
        rand = new Random();
        this.mode = mode;
        this.difficulty = Value.DEFAULT_DIFFICULTY;
		if(this.mode == 1) { // Nếu Mode = 1 => AI đánh trước
			root = new State(); 
			// Nếu người đánh trước là AI thì AI đánh mặc định nút ở giữa
			root.update(Value.SIZE/2, Value.SIZE/2, Value.AI_VALUE);
			nextX = Value.SIZE/2;
			nextY = Value.SIZE/2;
		}
		else root = new State(); 
		heuristic = new Heuristic();
    }
    
    public CaroAI(int mode, int difficulty) {
        rand = new Random();
        this.mode = mode;
        this.difficulty = difficulty;
		if(this.mode == 1) { // Nếu Mode = 1 => AI đánh trước
			root = new State(); 
			// Nếu người đánh trước là AI thì AI đánh mặc định nút ở giữa
			root.update(Value.SIZE/2, Value.SIZE/2, Value.AI_VALUE);
			nextX = Value.SIZE/2;
			nextY = Value.SIZE/2;
		}
		else root = new State(); 
		heuristic = new Heuristic();
    }

    
    public int getNextX() {
    	return nextX;
    }
    
    public int getNextY() {
    	return nextY;
    }
    
    public int getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    
    /**
     * Lấy độ sâu tìm kiếm dựa trên mức độ khó
     */
    private int getMaxDepth() {
        switch (difficulty) {
            case Value.DIFFICULTY_EASY:
                return Value.EASY_DEPTH;
            case Value.DIFFICULTY_MEDIUM:
                return Value.MEDIUM_DEPTH;
            case Value.DIFFICULTY_HARD:
                return Value.HARD_DEPTH;
            case Value.DIFFICULTY_EXPERT:
                return Value.EXPERT_DEPTH;
            default:
                return Value.MEDIUM_DEPTH;
        }
    }
    
    /**
     * Lấy số lượng ô tối ưu dựa trên mức độ khó
     */
    private int getMaxCellList() {
        switch (difficulty) {
            case Value.DIFFICULTY_EASY:
                return Value.EASY_CELL_LIST;
            case Value.DIFFICULTY_MEDIUM:
                return Value.MEDIUM_CELL_LIST;
            case Value.DIFFICULTY_HARD:
                return Value.HARD_CELL_LIST;
            case Value.DIFFICULTY_EXPERT:
                return Value.EXPERT_CELL_LIST;
            default:
                return Value.MEDIUM_CELL_LIST;
        }
    }
    
    /**
	 * Kiểm tra người thắng cuộc
	 * @return false: nếu không ai thắng, true: nếu player(AI: 2, USER: 1) thắng.
	 */
	public boolean checkWinner(int player) {
		return root.checkWinner(player);
	}
	
	/**
	 * Cập nhật ma trận
	 * @param x vị trí hàng
	 * @param y vị trí cột
	 * @param value USER_VALUE: 1, AI_VALUE: 2
	 */
	public void update(int x, int y, int value) {
		root.update(x, y, value);
	}
	
	/**
	 * Kiểm tra người chơi có thể chọn ô này hay không
	 * @param x vị trí hàng
	 * @param y vị trí cột
	 * @return true: nếu cell[x][y] = 0, false: ngược lại
	 */
	public boolean isClickable(int x, int y) {
		return root.isClickable(x, y);
	}
    
	/**
	 * Kiểm tra bàn cờ full chưa
	 * @return true: full, ngược lại
	 */
	public boolean isOver() {
		return root.isOver();
	}
	
	/**
	 * Hoàn tác nước đi cuối cùng
	 * @return Cell đã được hoàn tác, null nếu không có nước đi nào
	 */
	public Cell undo() {
		return root.undo();
	}
	
	/**
	 * Làm lại nước đi đã hoàn tác
	 * @return Cell đã được làm lại, null nếu không có nước đi nào để làm lại
	 */
	public Cell redo() {
		return root.redo();
	}
	
	/**
	 * Kiểm tra có thể undo không
	 * @return true nếu có thể undo
	 */
	public boolean canUndo() {
		return root.canUndo();
	}
	
	/**
	 * Kiểm tra có thể redo không
	 * @return true nếu có thể redo
	 */
	public boolean canRedo() {
		return root.canRedo();
	}
	
	/**
	 * Lấy số nước đi của User
	 * @return số nước đi của User
	 */
	public int getUserMoveCount() {
		return root.getUserMoveCount();
	}
	
	/**
	 * Lấy số nước đi của AI
	 * @return số nước đi của AI
	 */
	public int getAIMoveCount() {
		return root.getAIMoveCount();
	}
    

    /**
     * Search moves
     */
    
    public void nextStep() {
    	Cell choice = alphaBeta(root); // tìm kiếm bước đi tối ưu nhất
    	if(choice == null) System.out.println("~ Lỗi! Không tìm thấy nước đi!");
    	else {
    		this.nextX = choice.getX(); // cập nhật nextX, nextY
        	this.nextY = choice.getY();
    		System.out.println("=> Nước đi của AI: " + this.nextX + " " + this.nextY);
        	if(!isClickable(this.nextX, this.nextY)) System.out.println("~ Lỗi! nước đi bị trùng!");
        	else update(nextX, nextY, Value.AI_VALUE); // cập nhật root
    	}
//    	System.out.println("Root State:");
//    	root.printState();
    }
    
    
    
    public Cell alphaBeta(State state) {
        // Luôn sử dụng đánh giá đầy đủ cho tất cả các nước đi
        boolean isExpertMode = (difficulty == Value.DIFFICULTY_EXPERT);
        heuristic.evaluateEachCell(state, Value.AI_VALUE, isExpertMode, difficulty);
        
        System.out.println("Bảng lượng giá các ô:");
        heuristic.printEvalState(); // hiển thị bảng lượng giá
        ArrayList<EvalCell> list = heuristic.getOptimalList(getMaxCellList()); // lấy danh sách những ô tối ưu để xét
        System.out.println("List bước đi tối ưu:(Max:"+getMaxCellList()+")");
        for (EvalCell evalCell : list) {
			System.out.println(evalCell.getX() + " " + evalCell.getY() + ":" + evalCell.getValue());
		}
        
        int maxValue = Integer.MIN_VALUE;
        int n = list.size();
        ArrayList<EvalCell> ListChoose = new ArrayList<EvalCell>();
        
        // Sử dụng Minimax để tìm nước đi tối ưu
        for (int i = 0; i < n; i++) {
            State testState = new State(state.getState()); // Tạo state mới cho mỗi test
            testState.getState()[list.get(i).getX()][list.get(i).getY()] = Value.AI_VALUE;
            
            System.out.println("Với nước đi " + list.get(i).getX() + " " + list.get(i).getY() + ":");
            
            // Sử dụng Minimax để đánh giá nước đi này
            int value = maxValue(testState, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
            System.out.println("Lượng giá trạng thái của bước đi này:" + value);
            
            if (maxValue < value) {
                maxValue = value;
                ListChoose.clear();
                ListChoose.add(list.get(i));
            } else if (maxValue == value) {
                ListChoose.add(list.get(i));
            }
        }
        
        n = ListChoose.size(); // lấy số phần tử có điểm cao nhất
        int x = rand.nextInt(n); // chọn ra một
        return ListChoose.get(x).getCell();
    }

    /**
     * Lượng giá cho Max(AI)
     * @param state
     * @param alpha
     * @param beta
     * @param depth
     * @return
     */
    private int maxValue(State state, int alpha, int beta, int depth) {
        if (depth >= getMaxDepth() || state.checkWinner(Value.AI_VALUE) || state.isOver()) {
            int val = heuristic.evaluateState(state);
            return val;
        }
        if (state.getMoveCount() < 20) {
            heuristic.evaluateNearbyCells(state, Value.AI_VALUE, difficulty);
        } else {
            boolean isExpertMode = (difficulty == Value.DIFFICULTY_EXPERT);
            heuristic.evaluateEachCell(state, Value.AI_VALUE, isExpertMode, difficulty);
        }
        ArrayList<EvalCell> list = heuristic.getOptimalList(getMaxCellList());
        for (int i = 0; i < list.size(); i++) {
            state.getState()[list.get(i).getCell().getX()][list.get(i).getCell().getY()] = Value.AI_VALUE;
            alpha = Math.max(alpha, minValue(state, alpha, beta, depth + 1));
            state.getState()[list.get(i).getCell().getX()][list.get(i).getCell().getY()] = 0;
            if (alpha >= beta) {
                break;
            }
        }
        return alpha;
    }

    /**
     * Lượng giá cho Min(User)
     * @param state
     * @param alpha
     * @param beta
     * @param depth
     * @return
     */
    private int minValue(State state, int alpha, int beta, int depth) {
        if (depth >= getMaxDepth() || state.checkWinner(Value.USER_VALUE) || state.isOver()) {
            int val = heuristic.evaluateState(state);
            return val;
        }
        // Đánh giá cho User (Min player) - tìm nước đi tệ nhất cho AI
        if (state.getMoveCount() < 20) {
            heuristic.evaluateNearbyCells(state, Value.USER_VALUE, difficulty);
        } else {
            boolean isExpertMode = (difficulty == Value.DIFFICULTY_EXPERT);
            heuristic.evaluateEachCell(state, Value.USER_VALUE, isExpertMode, difficulty);
        }
        ArrayList<EvalCell> list = heuristic.getOptimalList(getMaxCellList());
        for (int i = 0; i < list.size(); i++) {
            state.getState()[list.get(i).getCell().getX()][list.get(i).getCell().getY()] = Value.USER_VALUE;
            // User là Min player, nên gọi maxValue cho nước đi tiếp theo của AI
            beta = Math.min(beta, maxValue(state, alpha, beta, depth + 1));
            state.getState()[list.get(i).getCell().getX()][list.get(i).getCell().getY()] = 0;
            if (alpha >= beta) {
                break;
            }
        }
        return beta;
    }
}
