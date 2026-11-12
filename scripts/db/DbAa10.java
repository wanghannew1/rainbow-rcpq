import java.sql.*;
public class DbAa10 {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection ora = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        ResultSet t = ora.createStatement().executeQuery(
            "SELECT column_name, data_type, char_length FROM user_tab_columns WHERE table_name='AA10' ORDER BY column_id");
        while (t.next()) System.out.println("col " + t.getString(1) + " " + t.getString(2) + "(" + t.getInt(3) + ")");
        PreparedStatement p = ora.prepareStatement("SELECT aaa100, aaa102, aaa103 FROM (SELECT aaa100, aaa102, aaa103 FROM aa10 WHERE aaa100='AAC005' ORDER BY aaa102) WHERE ROWNUM <= 3");
        ResultSet r = p.executeQuery();
        while (r.next()) System.out.println("ora: [" + r.getString(1) + "][" + r.getString(2) + "][" + r.getString(3) + "]");
    }
}
