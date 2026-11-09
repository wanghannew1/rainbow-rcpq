import java.sql.*;
import java.io.*;

/** 通用单表种子导出: INSERT 流 → work/seed_<table>.sql。用法: java DumpTable <TABLE> [WHERE] */
public class DumpTable {
    public static void main(String[] a) throws Exception {
        String table = a[0];
        String where = a.length > 1 ? a[1] : null;
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection(
            "jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"),
            System.getenv("ORA_PASS"));
        PreparedStatement pm = c.prepareStatement(
            "SELECT column_name, data_type FROM user_tab_columns WHERE table_name=? ORDER BY column_id");
        pm.setString(1, table);
        ResultSet m = pm.executeQuery();
        java.util.List<String> cols = new java.util.ArrayList<>();
        java.util.List<String> types = new java.util.ArrayList<>();
        while (m.next()) { cols.add(m.getString(1)); types.add(m.getString(2)); }
        String sql = "SELECT " + String.join(",", cols) + " FROM " + table + (where == null ? "" : " WHERE " + where);
        ResultSet r = c.createStatement().executeQuery(sql);
        PrintWriter w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream("work/seed_" + table.toLowerCase() + ".sql"), "UTF-8"), 1 << 20));
        w.println("-- seed " + table + (where == null ? "" : " WHERE " + where));
        w.println("BEGIN;");
        long n = 0;
        StringBuilder sb = new StringBuilder();
        while (r.next()) {
            sb.setLength(0);
            sb.append("INSERT INTO ").append(table.toLowerCase()).append(" (");
            for (int i = 0; i < cols.size(); i++) sb.append(cols.get(i).toLowerCase()).append(i < cols.size() - 1 ? "," : "");
            sb.append(") VALUES (");
            for (int i = 1; i <= cols.size(); i++) {
                Object v = r.getObject(i);
                if (v == null) sb.append("NULL");
                else if (types.get(i - 1).contains("NUMBER")) sb.append(v.toString());
                else sb.append('\'').append(v.toString().replace("'", "''")).append('\'');
                sb.append(i <= cols.size() - 1 ? "," : "");
            }
            sb.append(");\n");
            w.print(sb);
            n++;
            if (n % 50000 == 0) { w.flush(); System.out.println("  " + n + " rows..."); }
        }
        w.println("COMMIT;");
        w.close();
        System.out.println("dumped " + n + " rows -> work/seed_" + table.toLowerCase() + ".sql");
        c.close();
    }
}
