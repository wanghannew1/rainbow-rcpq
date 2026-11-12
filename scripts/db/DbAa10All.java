import java.sql.*;
import java.io.*;
public class DbAa10All {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection ora = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        ResultSet r = ora.createStatement().executeQuery("SELECT aaa100, aaa102, aaa103 FROM aa10 ORDER BY aaa100, aaa102");
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream("/home/ubuntu/github/rainbow-rcpq/work/aa10_ora.txt"), "UTF-8"));
        while (r.next()) w.println(r.getString(1) + "|" + r.getString(2) + "|" + r.getString(3));
        w.close(); System.out.println("ora dumped");
    }
}
