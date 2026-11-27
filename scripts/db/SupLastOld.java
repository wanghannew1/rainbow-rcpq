import java.sql.*;
import java.io.*;

/** 桥单 20261127-047 旧侧实弹探针(RCPQ_TEST 场, PKG_保险补退 包体 041 重编译 VALID)。
 *  对象: PRC_保险上月补退明细(三分支 E/Z/A + 离职 B) + PRC_保险补收基数(单位版) 可运行性考据。
 *  隔离键 卷47: aab001/atb930=20990971, atb900=20990972, atc900=20990701, PRM aae002=209906,
 *  aac003='补退探针员卷47'。克隆源: tb93@39737/tb90@39737 单位/ab01@2239/tc90@53031。
 *  附带: rcpq_test.user_source L1117-1125(单位版 OUT 绑定)与 L1891(全角括号)取证。
 *  用法: java SupLastOld → work/bc47_old_probe.txt */
public class SupLastOld {
    static Connection c;
    static PrintWriter out;

    static void p(String s) { out.println(s); System.out.println(s); }

    static int procLast(String aae002, String atc900, String aae011) throws Exception {
        CallableStatement cs = c.prepareCall("{call PKG_保险补退.PRC_保险上月补退明细(?,?,?,?,?)}");
        cs.setString(1, aae002); cs.setString(2, atc900); cs.setString(3, aae011);
        cs.registerOutParameter(4, Types.NUMERIC); cs.registerOutParameter(5, Types.VARCHAR);
        cs.execute();
        int code = cs.getInt(4); String msg = cs.getString(5);
        p("PROC_LAST aae011=" + aae011 + " => code=" + code + " msg=" + msg);
        cs.close();
        return code;
    }

    static void procUnit(String aae002, String aae011) throws Exception {
        try {
            CallableStatement cs = c.prepareCall("{call PKG_保险补退.PRC_保险补收基数(?,?,?,?)}");
            cs.setString(1, aae002); cs.setString(2, aae011);
            cs.registerOutParameter(3, Types.NUMERIC); cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();
            p("PROC_UNIT aae011=" + aae011 + " => code=" + cs.getInt(3) + " msg=" + cs.getString(4));
            cs.close();
        } catch (SQLException e) {
            p("PROC_UNIT call-fail: " + e.getMessage().split("\n")[0]);
        }
    }

    static void dumpTc07() throws Exception {
        PreparedStatement ps = c.prepareStatement(
            "SELECT * FROM tc07 WHERE aae002=209906 AND atc900=20990701 ORDER BY atc070");
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
        out = new PrintWriter(new FileWriter("work/bc47_old_probe.txt"), true);
        p("== SupLastOld @ RCPQ_TEST " + new Timestamp(System.currentTimeMillis()) + " ==");
        // ── ① 源码取证: 单位版 OUT 绑定行 + 上月明细全角括号(本场是否与 ccrcpq 同源) ──
        ResultSet rs1 = c.createStatement().executeQuery(
            "SELECT text FROM user_source WHERE name='PKG_保险补退' AND type='PACKAGE BODY' " +
            "AND line BETWEEN 1117 AND 1125 ORDER BY line");
        StringBuilder sb = new StringBuilder("SRC_1117_1125: ");
        while (rs1.next()) sb.append(rs1.getString(1)).append(" ");
        p(sb.toString().trim());
        ResultSet rs2 = c.createStatement().executeQuery(
            "SELECT COUNT(*) FROM user_source WHERE name='PKG_保险补退' AND type='PACKAGE BODY' " +
            "AND line BETWEEN 1723 AND 2905 AND (text LIKE '%（%' OR text LIKE '%）%')");
        rs2.next(); p("SRC_fullwidth_lines_1723_2905=" + rs2.getString(1));
        ResultSet rs3 = c.createStatement().executeQuery(
            "SELECT status, TO_CHAR(last_ddl_time,'yyyy-mm-dd hh24:mi') FROM user_objects " +
            "WHERE object_name='PKG_保险补退' AND object_type='PACKAGE BODY'");
        rs3.next(); p("BODY_STATUS=" + rs3.getString(1) + " last_ddl=" + rs3.getString(2));
        // ── ② 预清(隔离键) ──
        exec("DELETE FROM tc07 WHERE atc900=20990701");
        exec("DELETE FROM ac10 WHERE atc900=20990701");
        exec("DELETE FROM tc93 WHERE atc900=20990701");
        exec("DELETE FROM tj0a1 WHERE atc900=20990701");
        exec("DELETE FROM tj0b1 WHERE atc900=20990701");
        exec("DELETE FROM tj0d1 WHERE atc900=20990701");
        exec("DELETE FROM tc08 WHERE atb930=20990971");
        exec("DELETE FROM tc90 WHERE atc900=20990701");
        exec("DELETE FROM tb93 WHERE atb930=20990971");
        exec("DELETE FROM tb90 WHERE atb900=20990972 AND aab001=20990971");
        exec("DELETE FROM ab01 WHERE aab001=20990971");
        // ── ③ 夹具(ROWTYPE 全行克隆+键改写; 三险种门开+差异灯) ──
        exec("DECLARE r tb93%ROWTYPE; BEGIN SELECT * INTO r FROM tb93 WHERE atb930=39737; " +
             "r.atb930:=20990971; r.atb900:=20990972; r.aab001:=20990971; r.atb931:='补退探针单元卷47'; r.aaa803:='1'; " +
             "INSERT INTO tb93 VALUES r; END;");
        exec("DECLARE r tb90%ROWTYPE; BEGIN SELECT * INTO r FROM tb90 WHERE aab001=(SELECT aab001 FROM tb93 WHERE atb930=39737) AND ROWNUM=1; " +
             "r.atb900:=20990972; r.aab001:=20990971; INSERT INTO tb90 VALUES r; END;");
        exec("DECLARE r ab01%ROWTYPE; BEGIN SELECT * INTO r FROM ab01 WHERE aab001=2239; " +
             "r.aab001:=20990971; r.aab004:='补退探针单位卷47'; INSERT INTO ab01 VALUES r; END;");
        exec("DECLARE r tc90%ROWTYPE; BEGIN SELECT * INTO r FROM tc90 WHERE atc900=53031; " +
             "r.atc900:=20990701; r.aab001:=20990971; r.atb930:=20990971; r.atb900:=20990972; " +
             "r.aac001:=998072; r.aac002:='220183199906018801'; r.aac003:='补退探针员卷47'; " +
             "r.atb90r:='3'; r.atc90g:='1'; r.aae00p:='1'; " +
             "r.atc90p:='0'; r.aaa201:=209901; r.aaa203:=209902; r.aaa208:=209905; r.aaa209:=209906; " +
             "r.aae002:=209905; r.aae003:=209901; " +
             "r.atc90pa:='0'; r.aaa601:=209901; r.aaa603:=209902; r.aaa608:=209905; r.aaa110:='1299'; " +
             "r.aaa803:='1'; r.aae011:='jiaq'; " +
             "r.atc90w:='0'; r.aaa501:=209901; r.aaa503:=209902; r.aaa508:=209905; r.aaa120:='1299'; " +
             "r.aac008:='1127'; INSERT INTO tc90 VALUES r; END;");
        // AC10 三月(社保/医保/公积金窗口)
        exec("INSERT INTO ac10 (id, aac101, atc900, atc932, bba001, bba003, baa001, baa003, bba002, bba004, bba005, atb90t, baa002, caa001, caa002) " +
             "VALUES (2099070001,'1',20990701,209901,100,10,50,5,90,80,70,2,60,30,20)");
        exec("INSERT INTO ac10 (id, aac101, atc900, atc932, bba001, bba003, baa001, baa003, bba002, bba004, bba005, atb90t, baa002, caa001, caa002) " +
             "VALUES (2099070002,'1',20990701,209902,60,6,30,3,50,40,30,1,35,20,15)");
        exec("INSERT INTO ac10 (id, aac101, atc900, atc932, bba001, bba003, baa001, baa003, bba002, bba004, bba005, atb90t, baa002, caa001, caa002) " +
             "VALUES (2099070003,'1',20990701,209903,10,1,5,1,8,6,4,1,6,12,9)");
        p("-- BRANCH E 报错面(经办人无人) --");
        procLast("209906", "20990701", "nouser47");
        p("-- BRANCH Z 零操作面(三门全停, 差异夹具未插) --");
        exec("UPDATE tc90 SET atc90p='2', atc90pa='2', atc90w='2' WHERE atc900=20990701");
        procLast("209906", "20990701", "jiaq");
        dumpTc07();
        // TJ0A1@209903(征缴 500) vs TC93@209903(工资 700)
        exec("INSERT INTO tj0a1 (aae002, atc900, atj000, atb930, aac001, bba001, bba003, baa001, baa003, atj0a7) " +
             "VALUES (209903, 20990701, '1', 20990971, 998072, 110, 120, 130, 140, 500)");
        // TJ0B1@209904(征缴 1335) vs TC93@209904(工资 1790)
        exec("INSERT INTO tj0b1 (aae002, atc900, atj000, atb930, aac001, bba002, bba004, bba005, atb90t, baa002, daa001) " +
             "VALUES (209904, 20990701, '1', 20990971, 998072, 310, 320, 330, 10, 350, 15)");
        // TJ0D1@209903(公积金差异 70) + @209905(基数差异 1665)
        exec("INSERT INTO tj0d1 (aae002, atc900, atj000, atb930, aac001, caa001, caa002, atj0d5) " +
             "VALUES (209903, 20990701, '1', 20990971, 998072, 40, 30, 70)");
        exec("INSERT INTO tj0d1 (aae002, atc900, atj000, atb930, aac001, caa001, caa002, atj0d5) " +
             "VALUES (209905, 20990701, '2', 20990971, 998072, 888, 777, 1665)");
        // TC93 两月(已确认工资行)
        exec("INSERT INTO tc93 (atc930, atb900, atc900, atc931, atc937, aae214, bba001, bba003, baa001, baa003, caa001, caa002) " +
             "VALUES (2099070001, 20990972, 20990701, 209903, '1', '1', 210, 220, 230, 240, 55, 45)");
        exec("INSERT INTO tc93 (atc930, atb900, atc900, atc931, atc937, aae214, bba002, bba004, bba005, atb90t, baa002, atc93bc, atc93bd, caa001, caa002) " +
             "VALUES (2099070002, 20990972, 20990701, 209904, '1', '1', 410, 420, 430, 20, 450, 25, 35, 65, 50)");
        p("-- BRANCH A 产出面(门开: 社保年月差异+农民工+当月差异/省医保年月差异+当月差异/公积金年月差异+基数差异+当月差异) --");
        exec("UPDATE tc90 SET atc90p='0', atc90pa='0', atc90w='0' WHERE atc900=20990701");
        procLast("209906", "20990701", "jiaq");
        dumpTc07();
        p("-- BRANCH B 离职未参保(atb90r='5'+aae003=结算月+三停) --");
        exec("UPDATE tc90 SET atb90r='5', aae003=209906, atc90p='2', atc90pa='2', atc90w='2' WHERE atc900=20990701");
        procLast("209906", "20990701", "jiaq");
        dumpTc07();
// 经办人隔离: 零 tc90 合同的现成经办人(jiaq 有 3 条生产行命中社保门→clamp+RETURN 挡住后续段)
        ResultSet rop = c.createStatement().executeQuery(
            "SELECT aae011 FROM sys_org_user_unit WHERE aae011 IS NOT NULL " +
            "AND aae011 NOT IN (SELECT DISTINCT aae011 FROM tc90 WHERE aae011 IS NOT NULL) AND ROWNUM=1");
        rop.next(); String op47 = rop.getString(1);
        p("ISOLATED_OPERATOR=" + op47);
        p("-- 单位版 Run1 社保段: clamp+反转守卫观察(aaa201=209801, atc90p NULL→arm1 命中即 return) --");
        exec("DELETE FROM tc08 WHERE aac001=998072");
        exec("UPDATE tc90 SET atb90r='3', atc90p=NULL, aaa201=209801, aaa208=209906, aaa401='1', aaa402='0', aaa407='0', aae011='" + op47 + "' WHERE atc900=20990701");
        procUnit("209906", op47);
        ResultSet r8 = c.createStatement().executeQuery(
            "SELECT COUNT(*) FROM tc08 WHERE aac001=998072");
        r8.next(); p("TC08_ROWS_AFTER_UNIT1=" + r8.getString(1) + "(预期0: 反转守卫 return)");
        p("-- 单位版 Run2 医保段: arm1(aaa601=209801<结算月, atc90pa NULL, 2098<2099) --");
        exec("UPDATE tc90 SET aaa401='0', aaa402='1', atc90pa=NULL, aaa601=209801, aaa603=209801 WHERE atc900=20990701");
        ResultSet rd = c.createStatement().executeQuery(
            "SELECT COUNT(*) FROM tc90 a, ab01 b WHERE a.aab001=b.aab001 AND aaa402='1' " +
            "AND ((aaa601<209906 AND atc90pa IS NULL AND SUBSTR(aaa601,1,4)<SUBSTR('209906',1,4)) " +
            "OR (aaa603<209906 AND aaa608=209906 AND atc90pa='0' AND SUBSTR(aaa603,1,4)<SUBSTR(aaa608,1,4))) " +
            "AND SUBSTR(aaa110,3,2)='99' AND a.aae011='" + op47 + "' " +
            "AND NOT EXISTS (SELECT 1 FROM tc08 WHERE aae002=a.aae002 AND atc900=a.atc900) " +
            "AND a.atc900=20990701");
        rd.next(); p("MED_GATE_HIT=" + rd.getString(1));
        ResultSet rf = c.createStatement().executeQuery(
            "SELECT aaa402, aaa401, atc90pa, aaa601, aaa603, aaa608, aae011, aaa110, aac001 FROM tc90 WHERE atc900=20990701");
        if (rf.next()) p("FIXTURE: aaa402=" + rf.getString(1) + " aaa401=" + rf.getString(2) +
          " atc90pa=" + rf.getString(3) + " aaa601=" + rf.getString(4) + " aaa603=" + rf.getString(5) +
          " aaa608=" + rf.getString(6) + " aae011=" + rf.getString(7) + " aaa110=" + rf.getString(8) +
          " aac001=" + rf.getString(9));
        procUnit("209906", op47);
        r8 = c.createStatement().executeQuery(
            "SELECT aae000, ksny, jsny, aaa000, aaa300 FROM tc08 WHERE aac001=998072 ORDER BY aae000");
        int un = 0;
        while (r8.next()) { un++;
            p("[TC08#" + un + "] aae000=" + r8.getString(1) + " ksny=" + r8.getString(2) +
              " jsny=" + r8.getString(3) + " aaa000=" + r8.getString(4) + " aaa300=" + r8.getString(5)); }
        p("TC08_ROWS_AFTER_UNIT2=" + un + "(预期1: 医保 2098 年度行)");
        p("-- 单位版 Run3 公积金段: D_KSNY=AAA201 笔误观察(aaa501=209801 但 ksny 取 209901→全跳) --");
        exec("UPDATE tc90 SET aaa402='0', aaa407='1', atc90w=NULL, aaa501=209801, aaa503=209801 WHERE atc900=20990701");
        procUnit("209906", op47);
        r8 = c.createStatement().executeQuery("SELECT COUNT(*) FROM tc08 WHERE aac001=998072");
        r8.next(); p("TC08_ROWS_AFTER_UNIT3=" + r8.getString(1));
        ResultSet r8f = c.createStatement().executeQuery("SELECT * FROM tc08 WHERE aac001=998072 ORDER BY aaa000");
        ResultSetMetaData m8 = r8f.getMetaData();
        int u8 = 0;
        while (r8f.next()) { u8++;
          StringBuilder sb8 = new StringBuilder("[TC08F#" + u8 + "] ");
          for (int i = 1; i <= m8.getColumnCount(); i++) {
            String v = r8f.getString(i);
            if (v != null) sb8.append(m8.getColumnName(i)).append("=").append(v).append(" ");
          }
          p(sb8.toString());
        }
        // ── 清场+零残留 ──
        exec("DELETE FROM tc07 WHERE atc900=20990701");
        exec("DELETE FROM ac10 WHERE atc900=20990701");
        exec("DELETE FROM tc93 WHERE atc900=20990701");
        exec("DELETE FROM tj0a1 WHERE atc900=20990701");
        exec("DELETE FROM tj0b1 WHERE atc900=20990701");
        exec("DELETE FROM tj0d1 WHERE atc900=20990701");
        exec("DELETE FROM tc08 WHERE aac001=998072");
        exec("DELETE FROM tc90 WHERE atc900=20990701");
        exec("DELETE FROM tb93 WHERE atb930=20990971");
        exec("DELETE FROM tb90 WHERE atb900=20990972 AND aab001=20990971");
        exec("DELETE FROM ab01 WHERE aab001=20990971");
        ResultSet rl = c.createStatement().executeQuery(
            "SELECT (SELECT COUNT(*) FROM tc90 WHERE atc900=20990701)||'/'||" +
            "(SELECT COUNT(*) FROM ac10 WHERE atc900=20990701)||'/'||" +
            "(SELECT COUNT(*) FROM tc07 WHERE atc900=20990701)||'/'||" +
            "(SELECT COUNT(*) FROM tc93 WHERE atc900=20990701)||'/'||" +
            "(SELECT COUNT(*) FROM ab01 WHERE aab001=20990971) FROM dual");
        rl.next(); p("CLEANUP residue=" + rl.getString(1) + "(应0/0/0/0/0)");
        c.close();
        p("== END ==");
    }
}
