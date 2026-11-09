import java.sql.*; import java.io.*;

/** 全量 schema 依赖抓取: 全部视图定义 + 全部序列值 → work/ */
public class DumpSchema {
    public static void main(String[] a) throws Exception {
        String pass = System.getenv("ORA_PASS");
        if (pass == null || pass.isEmpty()) { System.err.println("请设置 ORA_PASS"); System.exit(2); }
        File work = new File(repoRoot(), "work"); work.mkdirs();
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection(
            "jdbc:oracle:thin:@" + env("ORA_HOST", "172.16.0.60") + ":1521:" + env("ORA_SVC", "orcl"),
            env("ORA_USER", "ccrcpq"), pass);

        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(work, "schema_views.txt")), "UTF-8"), true);
        ResultSet r = c.createStatement().executeQuery("select view_name, text from user_views order by view_name");
        int nv = 0;
        while (r.next()) {
            w.println("==== " + r.getString(1) + " ====");
            String t = r.getString(2);
            w.println(t == null ? "(空)" : t);
            nv++;
        }
        w.close();
        System.out.println("views=" + nv);

        PrintWriter s = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(work, "sequences.tsv")), "UTF-8"), true);
        r = c.createStatement().executeQuery("select sequence_name, last_number from user_sequences order by sequence_name");
        int ns = 0;
        while (r.next()) { s.println(r.getString(1) + "\t" + r.getLong(2)); ns++; }
        s.close();
        System.out.println("sequences=" + ns);
        c.close();
    }
    static String env(String k, String d) { String v = System.getenv(k); return v == null ? d : v; }
    static File repoRoot() {
        File d = new File("").getAbsoluteFile();
        while (d != null && !new File(d, "settings.gradle.kts").exists()) d = d.getParentFile();
        if (d == null) throw new IllegalStateException("未找到仓库根");
        return d;
    }
}
