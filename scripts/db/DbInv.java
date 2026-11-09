import java.sql.*;
import java.io.*;

/** 双 schema 对象盘点 → work/db_inventory.tsv（表/视图/过程/包/序列/触发器/同义词/源码统计）
 *  凭据走环境变量: ORA_USER(默认ccrcpq) ORA_PASS(必填) */
public class DbInv {
    public static void main(String[] args) throws Exception {
        String user = env("ORA_USER", "ccrcpq"), pass = System.getenv("ORA_PASS");
        if (pass == null || pass.isEmpty()) { System.err.println("请设置 ORA_PASS"); System.exit(2); }
        String host = System.getenv().getOrDefault("ORA_HOST", "172.16.0.60");
        String svc = System.getenv().getOrDefault("ORA_SVC", "orcl");
        String[][] users = {{user, pass}, {"ccrcpqu33", pass}};
        new File("work").mkdirs();
        PrintStream out = new PrintStream(new FileOutputStream("work/db_inventory.tsv"), true, "UTF-8");
        Class.forName("oracle.jdbc.driver.OracleDriver");
        for (String[] u : users) {
            Connection c = DriverManager.getConnection("jdbc:oracle:thin:@" + host + ":1521:" + svc, u[0], u[1]);
            DatabaseMetaData md = c.getMetaData();
            out.println("###USER\t" + u[0] + "\t" + md.getDatabaseMajorVersion() + "." + md.getDatabaseMinorVersion());
            dump(out, c, "TABLES", "select table_name, num_rows, tablespace_name from user_tables order by table_name");
            dump(out, c, "VIEWS", "select view_name from user_views order by view_name");
            dump(out, c, "FUNCS", "select object_name, object_type from user_objects where object_type in ('PROCEDURE','FUNCTION') order by object_name");
            dump(out, c, "PACKAGES", "select distinct object_name from user_objects where object_type='PACKAGE' order by object_name");
            dump(out, c, "SEQS", "select sequence_name, last_number from user_sequences order by sequence_name");
            dump(out, c, "TRIGGERS", "select trigger_name, table_name, triggering_event, status from user_triggers order by trigger_name");
            dump(out, c, "SYNONYMS", "select synonym_name, table_owner, table_name from user_synonyms order by synonym_name");
            dump(out, c, "SRC", "select name, type, count(*) from user_source group by name, type order by count(*) desc");
            c.close();
        }
        out.close();
        System.out.println("done");
    }
    static String env(String k, String d) { String v = System.getenv(k); return v == null ? d : v; }
    static void dump(PrintStream o, Connection c, String tag, String sql) throws Exception {
        Statement s = c.createStatement();
        ResultSet r = s.executeQuery(sql);
        int n = r.getMetaData().getColumnCount();
        StringBuilder sb = new StringBuilder();
        while (r.next()) {
            sb.setLength(0);
            sb.append(tag);
            for (int i = 1; i <= n; i++) { sb.append('\t'); sb.append(r.getString(i)); }
            o.println(sb);
        }
        r.close(); s.close();
    }
}
