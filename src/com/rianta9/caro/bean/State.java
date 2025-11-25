package com.rianta9.caro.bean;
import java.util.ArrayList;
import com.rianta9.caro.values.Value;



/**
 * Trạng thái trò chơi
 * @author rianta9
 */
public class State {
	private int[][] cell; // ma trận trạng thái
	private ArrayList<Cell> moveSteps; // danh sách các bước đã đi
	private ArrayList<Cell> redoSteps; // danh sách các bước đã undo để có thể redo
	private int steps; // số ô đã đánh
	
	public State(){
		this.steps = 0;
		this.moveSteps = new ArrayList<Cell>();
		this.redoSteps = new ArrayList<Cell>();
		this.cell = new int[Value.SIZE][Value.SIZE];
		for (int i=0; i<Value.SIZE; i++) {
			for(int j=0; j<Value.SIZE; j++) cell[i][j] = 0;
		}
	}
	
	public State(int[][] cell){
		this.steps = 0;
		this.moveSteps = new ArrayList<Cell>();
		this.redoSteps = new ArrayList<Cell>();
		this.cell = new int[Value.SIZE][Value.SIZE];
		for (int i=0; i<Value.SIZE; i++) {
			for(int j=0; j<Value.SIZE; j++) {
				this.cell[i][j] = cell[i][j];
				if(cell[i][j] != 0) {
					this.moveSteps.add(new Cell(i, j, cell[i][j]));
					this.steps++;
				}
			}
		}
		
	}
	
	/**
	 * Cập nhật nước đi mới
	 * @param x
	 * @param y
	 * @param player
	 */
	public void update(int x, int y, int player) {
		this.cell[x][y] = player;
		this.moveSteps.add(new Cell(x, y, player));
		this.steps++;
		// Xóa redo steps khi có nước đi mới
		this.redoSteps.clear();
	}
	
	public int[][] getState(){
		return this.cell;
	}
	
	public void setState(int[][] cell) {
		for (int i=0; i<Value.SIZE; i++) {
			for(int j=0; j<Value.SIZE; j++) this.cell[i][j] = cell[i][j];
		}
	}
	
	public void printState() {
		for (int i=0; i<Value.SIZE; i++) {
			for(int j=0; j<Value.SIZE; j++) {
				if(cell[i][j] == Value.AI_VALUE) System.out.print("- ");
				else if(cell[i][j] == Value.USER_VALUE) System.out.print("+ ");
				else System.out.print(cell[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	
	/**
	 * Kiểm tra người thắng cuộc
	 * @return true: nếu player thắng, false nếu ngược lại
	 */
	public boolean checkWinner(int player) {
		int[] lineX = {1, 1, 0, 1};  // |các đường cần kiểm tra(ngang, dọc, chéo xuống trái, chéo xuống phải)
		int[] lineY = {0, 1, 1, -1}; // |để tìm người thắng
		for (int x = 0; x < Value.SIZE; x++) {
			for (int y = 0; y < Value.SIZE; y++) {
				if(cell[x][y] == player) { // Nếu ô này đã được player chọn => kiểm tra			
					for (int i = 0; i < 4; i++) { // kiểm tra 4 đường
						int count = 1; // count = 5 => player chiến thắng
						for(int j = 1; j <= 4; j++) { // kiểm tra 4 ô tiếp theo
							int vtx = x + lineX[i]*j; // vị trí x của ô tiếp theo cần check
							int vty = y + lineY[i]*j; // vị trí y của ô tiếp theo cần check
							// vtx hoặc vty < 0 hoặc > Value.SIZE, hoặc ô này != ô đầu => khỏi ktra
							if(vtx < 0 || vty < 0 || vtx >= Value.SIZE || vty >= Value.SIZE) break;
							if(cell[vtx][vty] == player) count++;
							else break;
						}
						if(count == 5) return true; // Player thắng
					}
				}
			}
		}
		return false; // Không ai thắng cả
	}

	/**
	 * Kiểm tra một ô đã có ai đánh chưa
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isClickable(int x, int y) {
		if(x >= 0 && x < Value.SIZE && y >= 0 && y < Value.SIZE)
			if(cell[x][y] == 0) return true;
		return false;
	}
	
	/**
	 * Hết ô để đánh
	 * @return true: hết, false: chưa hết
	 */
	public boolean isOver() {
		if(this.steps == Value.SIZE*Value.SIZE) return true;
		else return false;
	}
	
	/**
	 * Hoàn tác nước đi cuối cùng
	 * @return Cell đã được hoàn tác, null nếu không có nước đi nào
	 */
	public Cell undo() {
		if(moveSteps.isEmpty()) return null;
		
		Cell lastMove = moveSteps.remove(moveSteps.size() - 1);
		this.cell[lastMove.getX()][lastMove.getY()] = 0;
		this.steps--;
		this.redoSteps.add(lastMove);
		return lastMove;
	}
	
	/**
	 * Làm lại nước đi đã hoàn tác
	 * @return Cell đã được làm lại, null nếu không có nước đi nào để làm lại
	 */
	public Cell redo() {
		if(redoSteps.isEmpty()) return null;
		
		Cell redoMove = redoSteps.remove(redoSteps.size() - 1);
		this.cell[redoMove.getX()][redoMove.getY()] = redoMove.getSelected();
		this.steps++;
		this.moveSteps.add(redoMove);
		return redoMove;
	}
	
	/**
	 * Kiểm tra có thể undo không
	 * @return true nếu có thể undo
	 */
	public boolean canUndo() {
		return !moveSteps.isEmpty();
	}
	
	/**
	 * Kiểm tra có thể redo không
	 * @return true nếu có thể redo
	 */
	public boolean canRedo() {
		return !redoSteps.isEmpty();
	}
	
	/**
	 * Lấy số nước đi đã thực hiện
	 * @return số nước đi
	 */
	public int getMoveCount() {
		return this.steps;
	}
	
	/**
	 * Lấy số nước đi của User
	 * @return số nước đi của User
	 */
	public int getUserMoveCount() {
		int count = 0;
		for(Cell move : moveSteps) {
			if(move.getSelected() == Value.USER_VALUE) count++;
		}
		return count;
	}
	
	/**
	 * Lấy số nước đi của AI
	 * @return số nước đi của AI
	 */
	public int getAIMoveCount() {
		int count = 0;
		for(Cell move : moveSteps) {
			if(move.getSelected() == Value.AI_VALUE) count++;
		}
		return count;
	}
}
