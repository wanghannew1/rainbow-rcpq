import java.sql.*;
public class MicroProbe {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            "ccrcpq", System.getenv().getOrDefault("ORA_PASS", "ccrc"));
        ResultSet r = c.createStatement().executeQuery(
            "SELECT table_name,column_name,data_type,data_length FROM user_tab_columns WHERE " +
            "(table_name='AA10' AND column_name IN ('AAA100','AAA102')) OR (table_name='AB01' AND column_name='AAB019')");
        while (r.next()) System.out.println(r.getString(1)+"."+r.getString(2)+" "+r.getString(3)+"("+r.getString(4)+")");
        r = c.createStatement().executeQuery(
            "SELECT DISTINCT name,type FROM user_source WHERE UPPER(text) LIKE '%INTO AA93%'");
        boolean f=false; while (r.next()) { System.out.println("INTO AA93 writer: "+r.getString(1)+"/"+r.getString(2)); f=true; }
        if(!f) System.out.println("INTO AA93: 全库无(含跨行风格)");
        r = c.createStatement().executeQuery(
            "SELECT COUNT(*) FROM user_source WHERE name='PKG_IMP' AND UPPER(text) LIKE '%TC90_BP.ATC90T1%'");
        r.next(); System.out.println("PKG_IMP 中 'TC90_BP.ATC90T1' 显式引用行数="+r.getString(1));
        c.close();
    }
}
