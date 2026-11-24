import java.sql.*;
public class MicroProbe2 {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            "ccrcpq", System.getenv().getOrDefault("ORA_PASS", "ccrc"));
        ResultSet r = c.createStatement().executeQuery(
            "SELECT DISTINCT name, type FROM user_source WHERE UPPER(text) LIKE '%PROCEDURE PRC_GET_MONEY%'");
        boolean f=false; while (r.next()) { System.out.println("PRC_GET_MONEY* 声明: "+r.getString(1)+" / "+r.getString(2)); f=true; }
        if(!f) System.out.println("PRC_GET_MONEY*: 全库无声明");
        r = c.createStatement().executeQuery(
            "SELECT DISTINCT name FROM user_source WHERE UPPER(text) LIKE '%PRC_GET_MONEY%'");
        f=false; while (r.next()) { System.out.println("引用者: "+r.getString(1)); f=true; }
        if(!f) System.out.println("引用者: 无");
        r = c.createStatement().executeQuery("SELECT count(*) FROM user_tab_columns WHERE table_name='TC92'");
        r.next(); System.out.println("TC92 列数="+r.getString(1));
        r = c.createStatement().executeQuery("SELECT count(*) FROM user_tab_columns WHERE table_name='TC90'");
        r.next(); System.out.println("TC90 列数="+r.getString(1));
        c.close();
    }
}
