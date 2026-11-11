import java.sql.*;
import java.io.*;

/** 查 user_objects 状态(铁律1: 翻译前必查) + 指定包源码行数。
 *  用法: java DbStatus [包名...] ; 无参=列出全部 INVALID 对象 */
public class DbStatus {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection(
            "jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"),
            System.getenv("ORA_PASS"));
        if (a.length == 0) {
            System.out.println("== INVALID 对象清单 ==");
            ResultSet r = c.createStatement().executeQuery(
                "SELECT object_type, object_name FROM user_objects WHERE status='INVALID' " +
                "AND object_type IN ('PACKAGE BODY','PACKAGE','PROCEDURE','FUNCTION','VIEW') ORDER BY 2,1");
            while (r.next()) System.out.println(r.getString(1) + "\t" + r.getString(2));
        }
        for (String pkg : a) {
            PreparedStatement ps = c.prepareStatement(
                "SELECT object_type, status, last_ddl_time FROM user_objects WHERE object_name=?");
            ps.setString(1, pkg);
            ResultSet r = ps.executeQuery();
            while (r.next())
                System.out.println(pkg + "\t" + r.getString(1) + "\t" + r.getString(2) + "\t" + r.getString(3));
            PreparedStatement ps2 = c.prepareStatement(
                "SELECT type, count(*) FROM user_source WHERE name=? GROUP BY type");
            ps2.setString(1, pkg);
            ResultSet r2 = ps2.executeQuery();
            while (r2.next()) System.out.println(pkg + " source " + r2.getString(1) + " lines=" + r2.getInt(2));
        }
    }
}
