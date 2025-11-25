/**
 * 
 */
package com.rianta9.caro.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.rianta9.caro.values.Value;

/**
 * Lưu kết quả thắng thua của User và AI
 * @author rianta9
 */
public class ActivityLog {
    private static final String LOG_FILE = "file\\log.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Lưu kết quả game vào file log
     * @param winner 1: User thắng, 2: AI thắng, 0: Hòa
     * @param userMoves số nước đi của User
     * @param aiMoves số nước đi của AI
     * @param gameTime thời gian chơi (giây)
     */
    public static void saveGameResult(int winner, int userMoves, int aiMoves, long gameTime) {
        try {
            File file = new File(LOG_FILE);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            
            OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file, true), StandardCharsets.UTF_8);
            
            String result;
            switch (winner) {
                case Value.USER_VALUE:
                    result = "User thắng";
                    break;
                case Value.AI_VALUE:
                    result = "AI thắng";
                    break;
                default:
                    result = "Hòa";
                    break;
            }
            
            String logEntry = String.format("%s | %s | User: %d nước | AI: %d nước | Thời gian: %ds\n",
                DATE_FORMAT.format(new Date()), result, userMoves, aiMoves, gameTime);
            
            writer.write(logEntry);
            writer.close();
        } catch (Exception e) {
            System.out.println("Lỗi khi lưu log: " + e.getMessage());
        }
    }
    
    /**
     * Đọc lịch sử game từ file log
     * @return danh sách các dòng log
     */
    public static List<String> getGameHistory() {
        List<String> history = new ArrayList<>();
        try {
            File file = new File(LOG_FILE);
            if (!file.exists()) {
                return history;
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Lỗi khi đọc log: " + e.getMessage());
        }
        return history;
    }
    
    /**
     * Xóa toàn bộ lịch sử game
     */
    public static void clearHistory() {
        try {
            File file = new File(LOG_FILE);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi xóa log: " + e.getMessage());
        }
    }
    
    /**
     * Lấy thống kê tổng quan
     * @return mảng [userWins, aiWins, draws, totalGames]
     */
    public static int[] getStatistics() {
        List<String> history = getGameHistory();
        int userWins = 0, aiWins = 0, draws = 0;
        
        for (String line : history) {
            if (line.contains("User thắng")) userWins++;
            else if (line.contains("AI thắng")) aiWins++;
            else if (line.contains("Hòa")) draws++;
        }
        
        return new int[]{userWins, aiWins, draws, history.size()};
    }
}
