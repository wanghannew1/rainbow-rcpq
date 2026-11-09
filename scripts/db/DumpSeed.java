import java.sql.*; import java.io.*; import java.text.*;
/** xa 4 表全量 INSERT → rcpq-web/src/main/resources/db/V3__xa_seed_data.sql */
public class DumpSeed {
    public static void main(String[] a) throws Exception {
        if (System.getenv("ORA_PASS") == null || System.getenv("ORA_PASS").isEmpty()) { System.err.println("请设置 ORA_PASS"); System.exit(2); }
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String tbl = System.getProperty("tables", "AA01,AA10,AA12,SYS_QUESTION");
        String[] tables = tbl.split(",");
        String outfile = System.getProperty("out", "V3__xa_seed_data.sql");
        SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        File dir = new File(new File(repoRoot(), "rcpq-web/src/main/resources"), "db");
        dir.mkdirs();
        Writer w = new OutputStreamWriter(new FileOutputStream(new File(dir, outfile)), "UTF-8");
        Connection c = conn(System.getenv().getOrDefault("ORA_HOST", "172.16.0.60"), System.getenv().getOrDefault("ORA_SVC", "orcl"),
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        w.write("-- 种子数据(备份库全量导出) " + outfile + "\n-- " + String.join(",", tables) + "\n\n");
        for (String tb : tables) {
            try {
            Statement s = c.createStatement();
            ResultSet r = s.executeQuery("select * from " + tb);
            ResultSetMetaData md = r.getMetaData();
            int n = md.getColumnCount();
            String[] cols = new String[n];
            for (int i = 0; i < n; i++) cols[i] = md.getColumnName(i + 1); // JDBC 1-based
            StringBuilder ins = new StringBuilder();
            long rows = 0;
            while (r.next()) {
                ins.setLength(0);
                ins.append("INSERT INTO ").append(tb).append(" (").append(String.join(", ", cols)).append(") VALUES (");
                for (int i = 1; i <= n; i++) {
                    if (i > 1) ins.append(", ");
                    int t = md.getColumnType(i);
                    Object v = (t == Types.TIMESTAMP || t == Types.DATE) ? (Object) r.getTimestamp(i) : (Object) r.getString(i);
                    if (v == null) ins.append("NULL");
                    else if (v instanceof Timestamp) ins.append('\'').append(ts.format((Timestamp) v)).append('\'');
                    else ins.append('\'').append(v.toString().replace("'", "''")).append('\'');
                }
                ins.append(");\n");
                w.write(ins.toString());
                if (++rows % 2000 == 0) w.write("\n");
            }
            r.close(); s.close();
            System.err.println(tb + ": " + rows + " rows");
            } catch (Exception eSkip) { System.err.println("SKIP " + tb + ": " + eSkip.getMessage()); }
        }
        w.close(); c.close(); System.out.println("done");
    }
    static File repoRoot() {
        File d = new File("").getAbsoluteFile();
        while (d != null && !new File(d, "settings.gradle.kts").exists()) d = d.getParentFile();
        if (d == null) throw new IllegalStateException("未找到仓库根(settings.gradle.kts)");
        return d;
    }
    static Connection conn(String h, String svc, String u, String p) throws SQLException {
        return DriverManager.getConnection("jdbc:oracle:thin:@" + h + ":1521:" + svc, u, p);
    }
}
