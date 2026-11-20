import java.sql.*;
import java.util.*;

/** 全库表发现: Oracle(备份库)∩PG 共有表清单+主键+行量, 供同步集扩展决策 */
public class DiscoverShared {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Class.forName("org.postgresql.Driver");
        Connection ora = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl", "ccrcpq", "ccrc");
        Connection pg = DriverManager.getConnection("jdbc:postgresql://localhost:5432/rainbow_rcpq", "rcpq", "rcpq_dev");
        // Oracle 表+PK
        Map<String, String> oraPk = new TreeMap<>();
        ResultSet r = ora.createStatement().executeQuery(
            "SELECT cc.table_name, LISTAGG(cc.column_name, ',') WITHIN GROUP (ORDER BY cc.position) pk " +
            "FROM user_constraints k JOIN user_cons_columns cc ON k.constraint_name=cc.constraint_name " +
            "WHERE k.constraint_type='P' GROUP BY cc.table_name");
        while (r.next()) oraPk.put(r.getString(1), r.getString(2));
        // PG 表+PK
        Map<String, String> pgPk = new HashMap<>();
        ResultSet p = pg.createStatement().executeQuery(
            "SELECT t.table_name, string_agg(kcu.column_name, ',' ORDER BY kcu.ordinal_position) " +
            "FROM information_schema.tables t JOIN information_schema.table_constraints tc " +
            " ON tc.table_name=t.table_name AND tc.constraint_type='PRIMARY KEY' " +
            " JOIN information_schema.key_column_usage kcu " +
            " ON kcu.constraint_name=tc.constraint_name AND kcu.table_name=tc.table_name " +
            "WHERE t.table_schema='public' GROUP BY t.table_name");
        while (p.next()) pgPk.put(p.getString(1).toUpperCase(), p.getString(2));
        // 共有表
        List<String> common = new ArrayList<>();
        for (String t : oraPk.keySet()) if (pgPk.containsKey(t)) common.add(t);
        Collections.sort(common);
        System.out.println("共有表(带双方主键) = " + common.size() + " 张:");
        for (String t : common) {
            long n1 = 0, n2 = 0;
            try { ResultSet r1 = ora.createStatement().executeQuery("SELECT count(*) FROM " + t); r1.next(); n1 = r1.getLong(1); } catch (Exception e) { n1 = -1; }
            try { ResultSet r2 = pg.createStatement().executeQuery("SELECT count(*) FROM \"" + t.toLowerCase() + "\""); r2.next(); n2 = r2.getLong(1); } catch (Exception e) { n2 = -1; }
            System.out.println(t + "|PK=" + oraPk.get(t) + "|ora=" + n1 + "|pg=" + n2);
        }
        pg.close(); ora.close();
    }
}
