package ideamc.titleplugin.SQL;

import ideamc.titleplugin.GUI.biyao;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ideamc.titleplugin.TitlePlugin.instance;
/**
 * @author suxiaolin
 * &#064;date  2024/4/16
 * 数据库表名有 Title 和 PlayerTitle
 * TitlePlugin
 */
public class DataBase {
    private final boolean isMysql;

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private final String url;
    private final String userName;
    private final String password;
    public DataBase(boolean isMysql) {
        this.isMysql = isMysql;
        String dataBase = instance.getConfig().getString("SQL.database");
        String host = instance.getConfig().getString("SQL.host");
        int port = instance.getConfig().getInt("SQL.port");
        userName = instance.getConfig().getString("SQL.username");
        password = instance.getConfig().getString("SQL.password");
        if (isMysql) {
            url = "jdbc:mysql://" + host + ":" + port + "/" + dataBase;
        } else {
            url = "jdbc:sqlite:./plugins/TitlePlugin/TitlePlugin.db";
        }
        try {
            Class.forName(getJdbcClass());
            connection = getConnection();
            createTableIfNotExists();
            Bukkit.getConsoleSender().sendMessage("[TitlePlugin]§2数据库连接成功");
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[TitlePlugin]§4数据库连接错误!");
            throw new RuntimeException(e);
        } finally {
            tryClose();
        }
    }
    private void tryClose() {
        try {
            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }
            if (statement != null) {
                statement.close();
                statement = null;
            }
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[TitlePlugin]§4" + e);
        }
    }
    protected void createTableIfNotExists() {
        try {
            connection = getConnection();
            statement = connection.createStatement();
            String createTableSQL_Title =
                    "CREATE TABLE IF NOT EXISTS Title " +
                    "(title_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "title_name TEXT NOT NULL," +
                    "type TEXT NOT NULL," +
                    "description TEXT," +
                    "vault INT," + //所需金币
                    "playerpoints INT," + //所需点券
                    "canbuy BOOLEAN," + //能否购买
                    "permission TEXT," + //所需权限
                    "youxiao INT," + //购买有效期
                    "sale_end_date TEXT)";//限时销售截止日期
            statement.executeUpdate(createTableSQL_Title);
            String createTableSQL_PlayerTitle =
                    "CREATE TABLE IF NOT EXISTS PlayerTitle " +
                    "(title_id INT NOT NULL," +
                    "player_uuid TEXT NOT NULL," +
                    "expiration_date TEXT," +
                    "prefix_enable boolean NOT NULL," +
                    "suffix_enable boolean NOT NULL)";
            statement.executeUpdate(createTableSQL_PlayerTitle);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            tryClose();
        }
    }
    private Connection getConnection() throws SQLException {
        return isMysql ? DriverManager.getConnection(url, userName, password) : DriverManager.getConnection(url);
    }
    private String getJdbcClass() {
        return isMysql ? "com.mysql.jdbc.Driver" : "org.sqlite.JDBC";
    }
    /**
     * 执行sql语句时调用
     *
     * @param sql sql语句
     * @param sender 玩家
     *
     * 返回boolean类型(成功/失败)
     */
    public boolean query(String sql, CommandSender sender) {
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            sender.sendMessage("[TitlePlugin]§4" + e);
        } finally {
            tryClose();
        }
        return false;
    }
    /**
     * 执行sql语句时调用
     *
     * @param sql sql语句
     *
     * 返回boolean类型(成功/失败)
     */
    public boolean eventQuery(String sql) {
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            Bukkit.getConsoleSender().sendMessage("[TitlePlugin]§2" + e);
        } finally {
            tryClose();
        }
        return false;
    }
    /**
     * 执行sql语句时调用
     *
     * @param sql sql语句
     * @param sender 玩家
     * @param table_name sql语句中的表名
     * select时一定要 select *
     * 返回TitleData类型的数据 具体见GUI文件夹里的biyao.java
     */
    public List<biyao.TitleData> readQuery(String sql, CommandSender sender, String table_name) {
        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            List<biyao.TitleData> titleData = new ArrayList<>();
            while (resultSet.next()) {
                if (table_name.equalsIgnoreCase("title")) {
                    int titleId = resultSet.getInt("title_id");
                    String titleName = resultSet.getString("title_name");
                    String type = resultSet.getString("type");
                    String description = resultSet.getString("description");
                    int coin = resultSet.getInt("vault");
                    int point = resultSet.getInt("playerpoints");
                    boolean canBuy = resultSet.getBoolean("canbuy");
                    String permission = resultSet.getString("permission");
                    int youxiao = resultSet.getInt("youxiao");
                    String saleEndDate = resultSet.getString("sale_end_date");
                    titleData.add(new biyao.TitleData(titleId, titleName, type, description, coin, point, canBuy, permission, youxiao, saleEndDate, null, null, true, true));
                } else if (table_name.equalsIgnoreCase("playertitle")) {
                    int titleId = resultSet.getInt("title_id");
                    String playerUuid = resultSet.getString("player_uuid");
                    String expirationDate = resultSet.getString("expiration_date");
                    boolean prefixEnable = resultSet.getBoolean("prefix_enable");
                    boolean suffixEnable = resultSet.getBoolean("suffix_enable");
                    titleData.add(new biyao.TitleData(titleId, null, null, null, 0, 0, false, null, 0, null, playerUuid, expirationDate, prefixEnable, suffixEnable));
                }
            }
            return titleData;
        } catch (SQLException e) {
            if (sender != null) {
                sender.sendMessage("[TitlePlugin]§4" + e);
            }
        } finally {
            tryClose();
        }
        return null;
    }
    /**
     * 执行sql语句时调用
     *
     * @param sql sql语句
     * @param sender 玩家
     * @param a 填1就行
     * 返回boolean类型(成功/失败)
     */
    public boolean readQuery(String sql, CommandSender sender, int a) {
        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (Objects.requireNonNull(resultSet).next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            sender.sendMessage("[TitlePlugin]§4" + e);
        } finally {
            tryClose();
        }
        return false;
    }
}