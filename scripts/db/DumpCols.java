import java.sql.*; import java.io.*;
/** 权威表结构: user_tab_columns 全量 → work/oracle_columns.tsv (table, col, type, len, prec, scale, nullable) */
public class DumpCols {
    public static void main(String[] a) throws Exception {
        String pass = System.getenv("ORA_PASS");
        if (pass == null || pass.isEmpty()) { System.err.println("请设置 ORA_PASS"); System.exit(2); }
        File work = new File(repoRoot(), "work"); work.mkdirs();
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection(
            "jdbc:oracle:thin:@172.16.0.60:1521:orcl", "ccrcpq", pass);
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(work, "oracle_columns.tsv")), "UTF-8"), true);
        ResultSet r = c.createStatement().executeQuery(
            "SELECT table_name, column_name, data_type, data_length, data_precision, data_scale, nullable, column_id " +
            "FROM user_tab_columns WHERE table_name NOT LIKE 'BIN$%' ORDER BY table_name, column_id");
        int n = 0;
        while (r.next()) {
            w.println(r.getString(1) + "\t" + r.getString(2) + "\t" + r.getString(3) + "\t" +
                r.getInt(4) + "\t" + r.getInt(5) + "\t" + r.getInt(6) + "\t" + r.getString(7));
            n++;
        }
        w.close(); System.out.println("columns=" + n); c.close();
    }
    static File repoRoot() {
        File d = new File("").getAbsoluteFile();
        while (d != null && !new File(d, "settings.gradle.kts").exists()) d = d.getParentFile();
        return d;
    }
}
