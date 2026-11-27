import java.sql.*;
import java.io.*;

/** 桥单 20261127-041 门开探针·旧侧实弹(RCPQ_TEST 场, PKG_保险补退 包体 VALID 2026-11-27)。
 *  隔离键: aab001/atb930=20990961, atb900=20990962, atc900=20990601, aae002=209906,
 *  aac003='补退探针员卷41', ac10.id=209906000x。克隆源: tb93@39737/tb90@39737 单位/ab01@2239/
 *  tc90@53031(全行 ROWTYPE 克隆+键改写)。三分支: E 报错(aae011 无人)/Z 零操作(门关 atc90p='2')/
 *  C 产出(门开 atc90p='0'+AC10 两月)→TC07 全列 dump。末尾清场+零残留验证。
 *  用法: java SupProbeOld(需 JDK8+ojdbc14) 输出 work/bc41_old_probe.txt */
public class SupProbeOld {
    static Connection c;
    static PrintWriter out;

    static void p(String s) { out.println(s); System.out.println(s); }

    static int proc(String aae002, String atc900, String aae011) throws Exception {
        CallableStatement cs = c.prepareCall("{call PKG_保险补退.PRC_保险补退明细(?,?,?,?,?)}");
        cs.setString(1, aae002); cs.setString(2, atc900); cs.setString(3, aae011);
        cs.registerOutParameter(4, Types.NUMERIC); cs.registerOutParameter(5, Types.VARCHAR);
        cs.execute();
        int code = cs.getInt(4); String msg = cs.getString(5);
        p("PROC aae011=" + aae011 + " => code=" + code + " msg=" + msg);
        cs.close();
        return code;
    }

    static void dumpTc07() throws Exception {
        PreparedStatement ps = c.prepareStatement(
            "SELECT * FROM tc07 WHERE aae002=209906 AND atc900=20990601 ORDER BY atc070");
        ResultSet r = ps.executeQuery();
        ResultSetMetaData m = r.getMetaData();
        int n = 0;
        while (r.next()) {
            n++;
            StringBuilder sb = new StringBuilder("[TC07#" + n + "] ");
            for (int i = 1; i <= m.getColumnCount(); i++) {
                String v = r.getString(i);
                if (v != null) sb.append(m.getColumnName(i)).append("=").append(v).append(" ");
            }
            p(sb.toString());
        }
        p("TC07_ROWS=" + n);
        ps.close();
    }

    static void exec(String sql) throws Exception {
        try { c.createStatement().execute(sql); }
        catch (SQLException e) { p("SQL_ERR: " + e.getMessage().split("\n")[0] + " :: " +
            sql.substring(0, Math.min(90, sql.length()))); }
    }

    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            "rcpq_test", "rcpq_test_2026");
        out = new PrintWriter(new FileWriter("work/bc41_old_probe.txt"), true);
        p("== SupProbeOld @ RCPQ_TEST " + new Timestamp(System.currentTimeMillis()) + " ==");
        // ── 预清(隔离键) ──
        exec("DELETE FROM tc07 WHERE aae002=209906 AND atc900=20990601");
        exec("DELETE FROM ac10 WHERE atc900=20990601");
        exec("DELETE FROM tc90 WHERE atc900=20990601");
        exec("DELETE FROM tb93 WHERE atb930=20990961");
        exec("DELETE FROM tb90 WHERE atb900=20990962 AND aab001=20990961");
        exec("DELETE FROM ab01 WHERE aab001=20990961");
        // ── 夹具(ROWTYPE 全行克隆+键改写) ──
        exec("DECLARE r tb93%ROWTYPE; BEGIN SELECT * INTO r FROM tb93 WHERE atb930=39737; " +
             "r.atb930:=20990961; r.atb900:=20990962; r.aab001:=20990961; r.atb931:='补退探针单元卷41'; " +
             "INSERT INTO tb93 VALUES r; END;");
        exec("DECLARE r tb90%ROWTYPE; BEGIN SELECT * INTO r FROM tb90 WHERE aab001=(SELECT aab001 FROM tb93 WHERE atb930=39737) AND ROWNUM=1; " +
             "r.atb900:=20990962; r.aab001:=20990961; INSERT INTO tb90 VALUES r; END;");
        exec("DECLARE r ab01%ROWTYPE; BEGIN SELECT * INTO r FROM ab01 WHERE aab001=2239; " +
             "r.aab001:=20990961; r.aab004:='补退探针单位卷41'; INSERT INTO ab01 VALUES r; END;");
        exec("DECLARE r tc90%ROWTYPE; BEGIN SELECT * INTO r FROM tc90 WHERE atc900=53031; " +
             "r.atc900:=20990601; r.aab001:=20990961; r.atb930:=20990961; r.atb900:=20990962; " +
             "r.aac001:=998072; r.aac002:='220183199906018801'; r.aac003:='补退探针员卷41'; " +
             "r.atb90r:='3'; r.atc90g:='1'; r.aae00p:='1'; r.atc90p:='2'; r.atc90w:='2'; " +
             "r.atc90pa:='2'; r.atc90pb:='2'; r.aac008:='11'; " +
             "r.aaa201:=209901; r.aaa203:=209902; r.aaa208:=209906; r.aaa209:=209906; " +
             "r.aae002:=209905; r.aae003:=209912; INSERT INTO tc90 VALUES r; END;");
        ResultSet rc = c.createStatement().executeQuery(
            "SELECT COUNT(*) FROM tc90 WHERE atc900=20990601");
        rc.next(); p("FIXTURE tc90=" + rc.getString(1));
        // ── 分支 E: 经办人错误 ──
        p("-- BRANCH E 报错面 --");
        proc("209906", "20990601", "nouser41");
        // ── 分支 Z: 门关零操作(atc90p='2') ──
        p("-- BRANCH Z 零操作面 --");
        proc("209906", "20990601", "jiaq");
        dumpTc07();
        // ── 分支 C: 门开产出(atc90p='0', AAA201<AAA203 窗口, AC10 两月) ──
        p("-- BRANCH C 产出面 --");
        exec("UPDATE tc90 SET atc90p='0' WHERE atc900=20990601");
        exec("INSERT INTO ac10 (id, aac101, atc900, atc932, bba001, bba003, baa001, baa003) " +
             "VALUES (2099060001,'1',20990601,209901,100,10,50,5)");
        exec("INSERT INTO ac10 (id, aac101, atc900, atc932, bba001, bba003, baa001, baa003) " +
             "VALUES (2099060002,'1',20990601,209902,60,6,30,3)");
        proc("209906", "20990601", "jiaq");
        dumpTc07();
        // ── 清场+零残留 ──
        exec("DELETE FROM tc07 WHERE aae002=209906 AND atc900=20990601");
        exec("DELETE FROM ac10 WHERE atc900=20990601");
        exec("DELETE FROM tc90 WHERE atc900=20990601");
        exec("DELETE FROM tb93 WHERE atb930=20990961");
        exec("DELETE FROM tb90 WHERE atb900=20990962 AND aab001=20990961");
        exec("DELETE FROM ab01 WHERE aab001=20990961");
        ResultSet rl = c.createStatement().executeQuery(
            "SELECT (SELECT COUNT(*) FROM tc90 WHERE atc900=20990601)||'/'||" +
            "(SELECT COUNT(*) FROM ac10 WHERE atc900=20990601)||'/'||" +
            "(SELECT COUNT(*) FROM tc07 WHERE atc900=20990601)||'/'||" +
            "(SELECT COUNT(*) FROM tb93 WHERE atb930=20990961) FROM dual");
        rl.next(); p("CLEANUP residue=" + rl.getString(1) + "(应0/0/0/0)");
        c.close();
        p("== END ==");
    }
}
