import java.sql.*; import java.io.*;
public class DumpPKs {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection(
            "jdbc:oracle:thin:@172.16.0.60:1521:orcl", "ccrcpq", System.getenv("ORA_PASS"));
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
            new File(repoRoot(), "work/oracle_pks.tsv")), "UTF-8"), true);
        ResultSet r = conn.createStatement().executeQuery(
            "SELECT cc.table_name, cc.column_name, cc.position FROM user_cons_columns cc " +
            "JOIN user_constraints c ON c.constraint_name = cc.constraint_name " +
            "WHERE c.constraint_type='P' ORDER BY cc.table_name, cc.position");
        int n = 0;
        while (r.next()) { w.println(r.getString(1) + "\t" + r.getString(2)); n++; }
        w.close(); System.out.println("pk_columns=" + n); conn.close();
    }
    static File repoRoot() {
        File d = new File("").getAbsoluteFile();
        while (d != null && !new File(d, "settings.gradle.kts").exists()) d = d.getParentFile();
        return d;
    }
}
