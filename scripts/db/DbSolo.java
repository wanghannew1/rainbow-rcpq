import java.sql.*;
/** 找一个无重复键的行并输出其全部列(ORA 视角, ojdbc11), 供与 PG 对照 */
public class DbSolo {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        PreparedStatement p = c.prepareStatement(
            "SELECT ikey FROM (SELECT ikey FROM tc90_list GROUP BY ikey HAVING count(*)=1) WHERE ROWNUM <= 1");
        ResultSet r = p.executeQuery(); r.next();
        long ikey = r.getLong(1);
        System.out.println("solo ikey=" + ikey);
        PreparedStatement q = c.prepareStatement("SELECT * FROM tc90_list WHERE ikey=?");
        q.setObject(1, new java.math.BigDecimal(ikey));
        ResultSet rs = q.executeQuery(); rs.next();
        ResultSetMetaData m = rs.getMetaData();
        for (int i = 1; i <= m.getColumnCount(); i++) {
            Object v = rs.getObject(i);
            if (v == null) continue;
            String s = v instanceof java.math.BigDecimal ? ((java.math.BigDecimal)v).stripTrailingZeros().toPlainString()
                : v instanceof Timestamp ? String.valueOf(((Timestamp)v).getTime()) : String.valueOf(v);
            System.out.println(m.getColumnName(i) + "\t" + s);
        }
    }
}
