import java.sql.*;
import java.io.*;

/** W0 裁决证据查询（只读）：公积金终态/省医保TC9W2在用性/省工伤报减炸点/回盘活跃度
 *  凭据走环境变量: ORA_PASS(必填) ORA_USER(默认ccrcpq) ORA_HOST(默认172.16.0.60) */
public class DbW0 {
    public static void main(String[] args) throws Exception {
        String user = System.getenv().getOrDefault("ORA_USER", "ccrcpq");
        String pass = System.getenv("ORA_PASS");
        if (pass == null || pass.isEmpty()) { System.err.println("请设置 ORA_PASS"); System.exit(2); }
        String host = System.getenv().getOrDefault("ORA_HOST", "172.16.0.60");
        PrintStream out = System.out;
        Class.forName("oracle.jdbc.driver.OracleDriver");
        try (Connection c = DriverManager.getConnection("jdbc:oracle:thin:@" + host + ":1521:orcl", user, pass)) {
            Statement s = c.createStatement();
            q(out, s, "W0#1 TC9U 公积金名单按审核状态分布+最晚年月",
               "SELECT NVL(TO_CHAR(ATC9U8),'NULL'), COUNT(*), MAX(AAE002) FROM TC9U GROUP BY ATC9U8 ORDER BY 2 DESC");
            q(out, s, "W0#1 TC9U 按年份活跃度",
               "SELECT SUBSTR(TO_CHAR(AAE002),1,4), COUNT(*) FROM TC9U GROUP BY SUBSTR(TO_CHAR(AAE002),1,4) ORDER BY 1 DESC");
            q(out, s, "W0#1 TC9W_BP02 公积金异动回盘桶活跃度(总量/最晚年月/最近经办)",
               "SELECT COUNT(*), MAX(AAE002), TO_CHAR(MAX(AAE036),'YYYY-MM-DD') FROM TC9W_BP02");
            q(out, s, "W0#1 TC9U_BP02 公积金封存回盘桶活跃度",
               "SELECT COUNT(*), MAX(AAE002), TO_CHAR(MAX(AAE036),'YYYY-MM-DD') FROM TC9U_BP02");
            q(out, s, "W0#2 TC9W2 省医保名单按业务状态分布+最晚年月",
               "SELECT NVL(TO_CHAR(ATC9W2),'NULL'), COUNT(*), MAX(AAE002), TO_CHAR(MAX(AAE036),'YYYY-MM-DD') FROM TC9W2 GROUP BY ATC9W2 ORDER BY 2 DESC");
            q(out, s, "W0#2 TC9W2 按年份x状态",
               "SELECT SUBSTR(TO_CHAR(AAE002),1,4), NVL(TO_CHAR(ATC9W2),'NULL'), COUNT(*) FROM TC9W2 GROUP BY SUBSTR(TO_CHAR(AAE002),1,4), ATC9W2 ORDER BY 1 DESC, 2");
            q(out, s, "W0#2 TC9W2 按审核状态(ATC9W8)",
               "SELECT NVL(TO_CHAR(ATC9W8),'NULL'), COUNT(*) FROM TC9W2 GROUP BY ATC9W8 ORDER BY 2 DESC");
            q(out, s, "W0#3 TC9W3 省工伤名单 异动类型x审核状态+最晚年月",
               "SELECT NVL(TO_CHAR(ATC9W1),'NULL'), NVL(TO_CHAR(ATC9W8),'NULL'), COUNT(*), MAX(AAE002) FROM TC9W3 GROUP BY ATC9W1, ATC9W8 ORDER BY 3 DESC");
            q(out, s, "W0#3 报减候选(ATB90R=5且AAA404=1且AAA305>0)性别码异常行数=ORA-01403炸点",
               "SELECT COUNT(*) FROM TC90 A WHERE A.ATB90R='5' AND A.AAA404='1' AND A.AAA305>0 AND (A.AAC004 IS NULL OR NOT EXISTS (SELECT 1 FROM AA10 D WHERE D.AAA100='AAC004' AND D.AAA102=A.AAC004))");
            q(out, s, "W0#3 报减候选总量",
               "SELECT COUNT(*) FROM TC90 A WHERE A.ATB90R='5' AND A.AAA404='1' AND A.AAA305>0");
            q(out, s, "W0#3 SYS_ERR_LOG ORA-01403 次数/最近一次",
               "SELECT COUNT(*), TO_CHAR(MAX(AAE036),'YYYY-MM-DD HH24:MI') FROM SYS_ERR_LOG WHERE errlog LIKE '%01403%'");
            q(out, s, "W0#1 SYS_ERR_LOG 公积金相关最近10条",
               "SELECT * FROM (SELECT TO_CHAR(AAE036,'YYYY-MM-DD HH24:MI'), AAE011, SUBSTR(errlog,1,80) FROM SYS_ERR_LOG WHERE errlog LIKE '%公积金%' ORDER BY AAE036 DESC) WHERE ROWNUM<=10");
            q(out, s, "W0#3 SYS_ERR_LOG 省工伤相关最近10条",
               "SELECT * FROM (SELECT TO_CHAR(AAE036,'YYYY-MM-DD HH24:MI'), AAE011, SUBSTR(errlog,1,80) FROM SYS_ERR_LOG WHERE errlog LIKE '%工伤%' ORDER BY AAE036 DESC) WHERE ROWNUM<=10");
        }
    }
    static void q(PrintStream out, Statement s, String title, String sql) {
        out.println("==== " + title);
        try (ResultSet r = s.executeQuery(sql)) {
            ResultSetMetaData m = r.getMetaData();
            StringBuilder h = new StringBuilder();
            for (int i = 1; i <= m.getColumnCount(); i++) h.append(m.getColumnLabel(i)).append(" | ");
            out.println(h);
            int n = 0;
            while (r.next() && n++ < 40) {
                StringBuilder b = new StringBuilder();
                for (int i = 1; i <= m.getColumnCount(); i++) b.append(r.getString(i)).append(" | ");
                out.println(b);
            }
            if (n == 0) out.println("(0 rows)");
        } catch (Exception e) { out.println("ERR: " + e.getMessage()); }
    }
}
