import java.sql.*;
/** 查 Oracle 表指定列重复值, 用法: java DbDup 表 列 [排序列] */
public class DbDup {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        String t = a[0], k = a[1], ord = a.length > 2 ? a[2] : null;
        PreparedStatement ps = c.prepareStatement(
            "SELECT " + k + ", count(*) n" + (ord == null ? "" : ", max(" + ord + ") latest") +
            " FROM " + t + " GROUP BY " + k + " HAVING count(*)>1 ORDER BY n DESC");
        ResultSet r = ps.executeQuery();
        int n = 0;
        while (r.next() && n < 10) { System.out.println(r.getString(1) + " x" + r.getLong(2) + (ord == null ? "" : " latest=" + r.getString(3))); n++; }
        r.last(); System.out.println("总重复键数=" + r.getRow());
        // 约束状态
        PreparedStatement p2 = c.prepareStatement(
            "SELECT constraint_name, status, validated FROM user_constraints WHERE table_name=? AND constraint_type='P'");
        p2.setString(1, t.toUpperCase());
        ResultSet r2 = p2.executeQuery();
        while (r2.next()) System.out.println("PK: " + r2.getString(1) + " " + r2.getString(2) + "/" + r2.getString(3));
    }
}
