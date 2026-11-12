import java.sql.*;
/** 看 Oracle TC90_LIST ikey=? 的全部重复行(aae036,rowid,aac003) */
public class DbDupRow {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        PreparedStatement p = c.prepareStatement(
            "SELECT ikey, aac003, aac002, aae036, rowid FROM tc90_list WHERE ikey=? ORDER BY aae036 DESC NULLS LAST, rowid");
        p.setObject(1, new java.math.BigDecimal(a[0]));
        ResultSet r = p.executeQuery();
        while (r.next()) System.out.println(r.getLong(1) + "|" + r.getString(2) + "|" + r.getString(3) + "|" + r.getString(4) + "|" + r.getString(5));
    }
}
