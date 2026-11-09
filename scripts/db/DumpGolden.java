import java.sql.*; import java.io.*;

/** xa 4 表黄金样本 → rcpq-service/src/test/resources/golden/xa-samples.json（列名必须小写！） */
public class DumpGolden {
    public static void main(String[] a) throws Exception {
        if (System.getenv("ORA_PASS") == null || System.getenv("ORA_PASS").isEmpty()) { System.err.println("请设置 ORA_PASS"); System.exit(2); }
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String[] tables = {"AA01", "AA10", "AA12", "SYS_QUESTION"};
        StringBuilder j = new StringBuilder("{\n");
        Connection c = conn(System.getenv().getOrDefault("ORA_HOST", "172.16.0.60"), System.getenv().getOrDefault("ORA_SVC", "orcl"),
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        Statement cnt = c.createStatement();
        for (int t = 0; t < tables.length; t++) {
            String tb = tables[t];
            ResultSet rc = cnt.executeQuery("select count(*) from " + tb);
            rc.next(); long total = rc.getLong(1); rc.close();
            Statement s = c.createStatement();
            s.setMaxRows(50);
            ResultSet r = s.executeQuery("select * from " + tb);
            ResultSetMetaData md = r.getMetaData();
            int n = md.getColumnCount();
            j.append("  \"").append(tb).append("\": {\"totalRows\": ").append(total).append(", \"sampled\": [");
            int rows = 0;
            while (r.next()) {
                if (rows++ > 0) j.append(",\n    ");
                j.append('{');
                for (int i = 1; i <= n; i++) {
                    if (i > 1) j.append(", ");
                    String col = md.getColumnName(i).toLowerCase(); // 元数据小写口径
                    String v = r.getString(i);
                    j.append('"').append(col).append("\": ");
                    if (v == null) j.append("null");
                    else { j.append('"').append(v.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")).append('"'); }
                }
                j.append('}');
            }
            j.append("]}");
            r.close(); s.close();
            System.err.println(tb + ": total=" + total + " sampled=" + rows);
            if (t < tables.length - 1) j.append(",\n");
        }
        j.append("\n}\n");
        File dir = new File(new File(repoRoot(), "rcpq-service/src/test/resources"), "golden");
        dir.mkdirs();
        Writer w = new OutputStreamWriter(new FileOutputStream(new File(dir, "xa-samples.json")), "UTF-8");
        w.write(j.toString()); w.close();
        c.close(); System.out.println("done");
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
