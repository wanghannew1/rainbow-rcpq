import java.sql.*; import java.io.*;

/** az 波次依赖抓取: 7个PL/SQL过程源码 + V_AB01_AB07视图定义 + 12表行数/序列值
 *  凭据走环境变量: ORA_USER(默认ccrcpq) ORA_PASS(必填) */
public class DumpAzDeps {
    static final String[] PROCS = {
        "PRC_CREATE_CHILD", "PRC_单位基本信息变更", "PRC_个人信息变更",
        "PRC_AC01_UPDATE_ONLY", "P_UPDATE_VALUE", "P_DELETE", "PRC_CREATE_PINYIN"
    };
    static final String[] TABLES = {
        "AB01", "AB06", "AB07", "SYS_FILE", "AC01", "AC01_LB", "AC06", "AC0B", "AC0D", "CC0P", "TC90", "TC90_LIST"
    };
    public static void main(String[] a) throws Exception {
        String pass = System.getenv("ORA_PASS");
        if (pass == null || pass.isEmpty()) { System.err.println("请设置 ORA_PASS"); System.exit(2); }
        String host = env("ORA_HOST", "172.16.0.60"), svc = env("ORA_SVC", "orcl");
        String user = env("ORA_USER", "ccrcpq");
        File workDir = new File(repoRoot(), "work");
        workDir.mkdirs();
        PrintStream out = new PrintStream(new FileOutputStream(new File(workDir, "az_deps.txt")), true, "UTF-8");
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@" + host + ":1521:" + svc, user, pass);

        out.println("=== 过程/函数源码 ===");
        PreparedStatement p = c.prepareStatement(
            "select name, type, line, text from user_source where name in (select object_name from user_objects where object_type in ('PROCEDURE','FUNCTION','PACKAGE','PACKAGE BODY')) order by name, type, line");
        ResultSet r = p.executeQuery();
        StringBuilder cur = new StringBuilder();
        String curName = "", curType = "";
        while (r.next()) {
            String nm = r.getString(1), ty = r.getString(2), tx = r.getString(4);
            if (!nm.equals(curName) || !ty.equals(curType)) {
                if (curName.length() > 0 && wanted(curName)) out.print(cur);
                cur = new StringBuilder("---- " + nm + " (" + ty + ") ----\n");
                curName = nm; curType = ty;
            }
            cur.append(tx);
        }
        if (curName.length() > 0 && wanted(curName)) out.print(cur);
        r.close(); p.close();

        out.println("\n=== V_AB01_AB07 视图定义 ===");
        r = c.createStatement().executeQuery("select text from user_views where view_name='V_AB01_AB07'");
        while (r.next()) {
            String v = r.getString(1);
            out.println(v == null ? "(空)" : v);
        }

        out.println("\n=== 表行数 ===");
        for (String tb : TABLES) {
            try {
                ResultSet rc = c.createStatement().executeQuery("select count(*) from " + tb);
                rc.next(); out.println(tb + "=" + rc.getLong(1)); rc.close();
            } catch (Exception e) { out.println(tb + "=ERR " + e.getMessage()); }
        }

        out.println("\n=== 相关序列当前值 ===");
        r = c.createStatement().executeQuery(
            "select sequence_name, last_number from user_sequences where sequence_name in ('SEQ_AC01_AAC001','SEQ_AC06_AAC060')");
        while (r.next()) out.println(r.getString(1) + "=" + r.getLong(2));

        out.println("\n=== AB01/AC01 主键抽查 ===");
        r = c.createStatement().executeQuery("select count(*), max(aab001) from AB01 where aab001 is not null");
        while (r.next()) out.println("AB01.aab001 非空数=" + r.getLong(1) + " max=" + r.getLong(2));
        r = c.createStatement().executeQuery("select count(*), min(aac001), max(aac001) from AC01");
        while (r.next()) out.println("AC01.aac001 数=" + r.getLong(1) + " min=" + r.getLong(2) + " max=" + r.getLong(3));
        c.close(); out.close(); System.out.println("done");
    }
    static File repoRoot() {
        File d = new File("").getAbsoluteFile();
        while (d != null && !new File(d, "settings.gradle.kts").exists()) d = d.getParentFile();
        if (d == null) throw new IllegalStateException("未找到仓库根");
        return d;
    }
    static String env(String k, String d) { String v = System.getenv(k); return v == null ? d : v; }
    static boolean wanted(String n) {
        for (String s : PROCS) if (n.toUpperCase().contains(s.toUpperCase())) return true;
        return n.toUpperCase().startsWith("PKG_基本信息") || n.toUpperCase().startsWith("PKG_COMMON") || n.toUpperCase().startsWith("PKG_PINYIN") || n.equals("P_UPDATE_VALUE") || n.equals("P_DELETE") || n.equalsIgnoreCase("PRC_CREATE_PINYIN");
    }
}
