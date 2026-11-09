import java.sql.*;
import java.io.*;

/** 导出独立 PL/SQL 过程源码(all_source)。用法: java DumpProc <OWNER> <NAME> [输出文件] */
public class DumpProc {
    public static void main(String[] a) throws Exception {
        String owner = a[0], name = a[1];
        String out = a.length > 2 ? a[2] : "work/proc_" + name.toLowerCase() + ".sql";
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection(
            "jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"),
            System.getenv("ORA_PASS"));
        PreparedStatement ps = c.prepareStatement(
            "SELECT text FROM all_source WHERE owner=? AND name=? ORDER BY line");
        ps.setString(1, owner); ps.setString(2, name);
        ResultSet r = ps.executeQuery();
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"));
        int n = 0;
        while (r.next()) { w.print(r.getString(1)); n++; }
        w.close();
        System.out.println("lines=" + n + " -> " + out);
    }
}
