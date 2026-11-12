import java.sql.*;
/** Oracle 侧中文对照样本: java DbZh */
public class DbZh {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        ResultSet r = c.createStatement().executeQuery(
            "SELECT atc900, aac003, aab004 FROM tc90 WHERE atc900 = 375");
        while (r.next()) System.out.println(r.getLong(1) + "|" + r.getString(2) + "|" + r.getString(3));
    }
}
