import java.sql.*;
public class DbOne {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        ResultSet r = c.createStatement().executeQuery(
            "SELECT atc900 FROM (SELECT atc900 FROM tc90 WHERE atc900 IS NOT NULL ORDER BY atc900) WHERE ROWNUM <= 3");
        while (r.next()) System.out.println(r.getLong(1));
    }
}
