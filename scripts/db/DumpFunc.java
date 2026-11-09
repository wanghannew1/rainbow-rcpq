import java.sql.*;
import java.io.*;

/** 导出 PL/SQL 函数源码(user_source 全对象文本搜索)。用法: java DumpFunc <NAME> [输出文件] */
public class DumpFunc {
    public static void main(String[] a) throws Exception {
        String name = a[0];
        String out = a.length > 1 ? a[1] : "work/func_" + name + ".sql";
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection(
            "jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"),
            System.getenv("ORA_PASS"));
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"));
        PreparedStatement ps = c.prepareStatement(
            "SELECT line, text FROM user_source WHERE UPPER(name)=UPPER(?) ORDER BY line");
        ps.setString(1, name);
        ResultSet r = ps.executeQuery();
        int n = 0;
        while (r.next()) { w.print(r.getString(2)); n++; }
        w.close();
        System.out.println("lines=" + n + " -> " + out);
        c.close();
    }
}
