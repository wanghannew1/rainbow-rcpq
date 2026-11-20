import java.sql.*;
import java.util.*;

/** 死表体检: 共有表中 哪些数据停在远古(最新时间戳<2020) / 东软近期SQL不再引用
 *  输出三类: ALIVE(近期有引用) / STALE(数据陈旧) / NODATE(无日期列,无法判定,默认保留同步) */
public class DeadTableProbe {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection ora = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl", "ccrcpq", "ccrc");
        // 共有表清单(带主键, 简版: 复用 PK 发现)
        List<String> tabs = new ArrayList<>();
        Map<String, List<String>> dateCols = new HashMap<>();
        ResultSet r = ora.createStatement().executeQuery(
            "SELECT cc.table_name, LISTAGG(cc.column_name, ',') WITHIN GROUP (ORDER BY cc.position) " +
            "FROM user_constraints k JOIN user_cons_columns cc ON k.constraint_name=cc.constraint_name " +
            "WHERE k.constraint_type='P' GROUP BY cc.table_name");
        while (r.next()) tabs.add(r.getString(1));
        // 日期列(每表 DATE/TIMESTAMP 类列, 最多取 3 个)
        PreparedStatement dc = ora.prepareStatement(
            "SELECT column_name FROM user_tab_columns WHERE table_name=? AND (data_type='DATE' OR data_type LIKE 'TIMESTAMP%') ORDER BY column_id");
        for (String t : tabs) {
            dc.setString(1, t);
            ResultSet rd = dc.executeQuery();
            List<String> d = new ArrayList<>();
            while (rd.next() && d.size() < 3) d.add(rd.getString(1));
            if (!d.isEmpty()) dateCols.put(t, d);
        }
        // 东软近期 SQL 引用(v$sql 全文, 一次装载)
        StringBuilder sqlText = new StringBuilder();
        try {
            ResultSet sv = ora.createStatement().executeQuery(
                "SELECT sql_fulltext FROM v$sql WHERE sql_fulltext IS NOT NULL");
            while (sv.next()) sqlText.append(sv.getClob(1).getSubString(1, 2000)).append('\n');
        } catch (Exception e) { System.out.println("v$sql 不可读: " + e.getMessage()); }
        String pool = sqlText.toString().toUpperCase();
        // 逐表判定
        int alive = 0, stale = 0, nodate = 0;
        List<String> staleList = new ArrayList<>();
        for (String t : tabs) {
            List<String> d = dateCols.getOrDefault(t, Collections.emptyList());
            String newest = null;
            for (String c : d) {
                try {
                    ResultSet x = ora.createStatement().executeQuery(
                        "SELECT to_char(max(" + c + "),'YYYY-MM-DD') FROM " + t +
                        " WHERE " + c + " IS NOT NULL AND ROWNUM=1 HAVING max(" + c + ") IS NOT NULL");
                    // HAVING 无 GROUP 配合需 WHERE+GROUP 改写; 直接简化:
                } catch (Exception ig) {}
                try {
                    ResultSet x = ora.createStatement().executeQuery(
                        "SELECT to_char(max(" + c + "),'YYYY-MM-DD') FROM (SELECT " + c + " FROM " + t +
                        " WHERE " + c + " IS NOT NULL ORDER BY " + c + " DESC) WHERE ROWNUM=1");
                    if (x.next() && x.getString(1) != null) {
                        if (newest == null || x.getString(1).compareTo(newest) > 0) newest = x.getString(1);
                    }
                } catch (Exception ig) {}
            }
            boolean inSql = pool.contains(t.toUpperCase());
            if (newest == null && !inSql) { nodate++; continue; }
            boolean old = newest != null && newest.compareTo("2020-01-01") < 0;
            if (old && !inSql) { stale++; staleList.add(t + "|最新=" + newest); }
            else alive++;
        }
        System.out.println("ALIVE(近期业务在用)=" + alive + "  STALE(数据陈旧且东软近期SQL不引用)=" + stale
            + "  NODATE(无日期列,默认保留)=" + nodate);
        System.out.println("--- STALE 清单(建议不同步) ---");
        for (String s : staleList) System.out.println(s);
        ora.close();
    }
}
