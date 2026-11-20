import java.sql.*;
import java.util.*;

/** 业务触碰探针(修正版): v$sql 只取 .70 应用会话(JDBC 模块)的 SQL, 排除 dmp 导入噪音。
 *  输出: 应用近期引用过的表 / 从未引用的表(=业务不碰, 建议不同步, 附行数) */
public class AppSqlProbe {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection ora = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl", "ccrcpq", "ccrc");
        String poolS = "";
        PreparedStatement ps = ora.prepareStatement(
            "SELECT sql_fulltext FROM v$sql WHERE module LIKE '%JDBC%' AND sql_fulltext IS NOT NULL");
        ResultSet rs = ps.executeQuery();
        int n = 0;
        while (rs.next()) {
            String s = rs.getClob(1).getSubString(1, 3000);
            if (s.length() > 30) { poolS += s.toUpperCase() + "\n"; n++; }
        }
        System.out.println("应用会话 SQL 条数=" + n + " 文本量=" + (poolS.length() / 1024) + "KB");
        // 共有表清单
        List<String> tabs = new ArrayList<>();
        Map<String, String> pks = new HashMap<>();
        ResultSet r = ora.createStatement().executeQuery(
            "SELECT cc.table_name, LISTAGG(cc.column_name, ',') WITHIN GROUP (ORDER BY cc.position) " +
            "FROM user_constraints k JOIN user_cons_columns cc ON k.constraint_name=cc.constraint_name " +
            "WHERE k.constraint_type='P' GROUP BY cc.table_name");
        while (r.next()) { tabs.add(r.getString(1)); pks.put(r.getString(1), r.getString(2)); }
        Collections.sort(tabs);
        int touched = 0, untouched = 0;
        List<String> un = new ArrayList<>();
        for (String t : tabs) {
            boolean hit = poolS.contains(t.toUpperCase() + " ") || poolS.contains(t.toUpperCase() + "\n");
            if (hit) touched++;
            else {
                untouched++;
                long cnt = -1;
                try { ResultSet c = ora.createStatement().executeQuery("SELECT count(*) FROM " + t); c.next(); cnt = c.getLong(1); } catch (Exception ig) {}
                un.add(t + "|ora行=" + cnt);
            }
        }
        System.out.println("应用近期引用=" + touched + "  未引用(业务不碰)=" + untouched);
        System.out.println("--- 未引用清单 ---");
        for (String s : un) System.out.println(s);
        ora.close();
    }
}
