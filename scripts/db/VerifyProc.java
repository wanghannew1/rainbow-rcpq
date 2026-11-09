import java.sql.*;
public class VerifyProc {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String url = "jdbc:oracle:thin:@172.16.0.60:1521:orcl";
        String[] schemas = {System.getenv().getOrDefault("ORA_USER", "ccrcpq"), "ccrcpqu33"};
        for (String s : schemas) {
            Connection c = DriverManager.getConnection(url, s, System.getenv("ORA_PASS"));
            System.out.println("===== schema: " + s + " =====");
            // 1. 对象表: 精确名 + 模糊
            ResultSet r = c.createStatement().executeQuery(
                "SELECT object_name, object_type FROM user_objects WHERE object_name LIKE '%个人信息变更%'");
            int n = 0; while (r.next()) { System.out.println("  user_objects: " + r.getString(1) + " " + r.getString(2)); n++; }
            if (n == 0) System.out.println("  user_objects: 无匹配对象");
            // 2. 过程表(含包内成员)
            r = c.createStatement().executeQuery(
                "SELECT object_name, procedure_name FROM user_procedures WHERE procedure_name LIKE '%个人信息变更%' OR object_name LIKE '%个人信息变更%'");
            n = 0; while (r.next()) { System.out.println("  user_procedures: " + r.getString(1) + "." + r.getString(2)); n++; }
            if (n == 0) System.out.println("  user_procedures: 无匹配过程");
            // 3. 全部源码文本: 任何提到该名字的 PL/SQL
            r = c.createStatement().executeQuery(
                "SELECT DISTINCT name, type FROM user_source WHERE UPPER(text) LIKE '%个人信息变更%'");
            n = 0; while (r.next()) { System.out.println("  user_source 引用: " + r.getString(1) + " " + r.getString(2)); n++; }
            if (n == 0) System.out.println("  user_source: 全库源码零引用");
            // 4. PKG_基本信息 包内全部过程清单(证明搜索覆盖了正确的包)
            r = c.createStatement().executeQuery(
                "SELECT DISTINCT procedure_name FROM user_procedures WHERE object_name='PKG_基本信息' ORDER BY 1");
            StringBuilder sb = new StringBuilder("  PKG_基本信息 全部成员: ");
            while (r.next()) sb.append(r.getString(1)).append(", ");
            System.out.println(sb.substring(0, Math.min(sb.length(), 300)));
            c.close();
        }
    }
}
