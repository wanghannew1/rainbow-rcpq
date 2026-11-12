import java.sql.*;
/** 列出表主键列, 用法: java DbPk T1,T2,... */
public class DbPk {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        for (String t : a[0].split(",")) {
            PreparedStatement ps = c.prepareStatement(
                "SELECT cc.column_name FROM user_constraints k JOIN user_cons_columns cc " +
                "ON k.constraint_name=cc.constraint_name WHERE k.table_name=? AND k.constraint_type='P' ORDER BY cc.position");
            ps.setString(1, t.toUpperCase());
            ResultSet r = ps.executeQuery();
            StringBuilder sb = new StringBuilder(t + ": ");
            boolean any = false;
            while (r.next()) { sb.append(r.getString(1)).append(","); any = true; }
            System.out.println(any ? sb.substring(0, sb.length() - 1) : t + ": (无主键)");
        }
    }
}
