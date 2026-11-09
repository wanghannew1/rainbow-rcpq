import java.sql.*;
import java.io.*;

/** 导出指定 PL/SQL 包(含包体)完整源码 → work/ 文件。用法: java DumpPackage <PKG_NAME> [输出文件] */
public class DumpPackage {
    public static void main(String[] a) throws Exception {
        String pkg = a[0];
        String out = a.length > 1 ? a[1] : "work/pkg_" + pkg.toLowerCase() + ".sql";
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection(
            "jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"),
            System.getenv("ORA_PASS"));
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"));
        // type DESC: 'PACKAGE BODY' 排在 'PACKAGE' 前——包体先输出
        PreparedStatement ps = c.prepareStatement(
            "SELECT type, line, text FROM user_source WHERE name=? ORDER BY type DESC, line");
        ps.setString(1, pkg);
        ResultSet r = ps.executeQuery();
        String curType = "";
        int n = 0;
        while (r.next()) {
            String type = r.getString(1);
            if (!type.equals(curType)) {
                w.println();
                w.println("-- ===== " + type + " " + pkg + " =====");
                curType = type;
            }
            w.print(r.getString(3));
            n++;
        }
        w.close();
        System.out.println("lines=" + n + " -> " + out);
        c.close();
    }
}
