import java.sql.*;
/** 列出 Oracle 用户表+估算行数(按列名过滤), 用法: java DbTables [关键词] */
public class DbTables {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        String filter = a.length > 0 ? a[0].toUpperCase() : "";
        PreparedStatement ps = c.prepareStatement(
            "SELECT t.table_name, t.num_rows FROM user_tables t WHERE t.table_name LIKE ? ORDER BY t.table_name");
        ps.setString(1, "%" + filter + "%");
        ResultSet r = ps.executeQuery();
        while (r.next()) System.out.println(r.getString(1) + "\t" + (r.getObject(2) == null ? "?" : r.getLong(2)));
    }
}
