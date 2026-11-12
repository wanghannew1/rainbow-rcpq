import java.sql.*;
public class DbIkeyType {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        ResultSet r = c.createStatement().executeQuery(
            "SELECT column_name, data_type FROM user_tab_columns WHERE table_name='TC90_LIST' AND column_name IN ('IKEY','ID')");
        while (r.next()) System.out.println(r.getString(1) + " " + r.getString(2));
    }
}
