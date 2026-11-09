import java.sql.*;
/** 读 user_sequences 当前值（V2 序列对齐用） */
public class SeqVal {
    public static void main(String[] a) throws Exception {
        if (System.getenv("ORA_PASS") == null || System.getenv("ORA_PASS").isEmpty()) { System.err.println("请设置 ORA_PASS"); System.exit(2); }
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = conn(System.getenv().getOrDefault("ORA_HOST", "172.16.0.60"), System.getenv().getOrDefault("ORA_SVC", "orcl"),
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        Statement s = c.createStatement();
        ResultSet r = s.executeQuery("select sequence_name, last_number from user_sequences where sequence_name in ('SEQ_AA01_AAA010','SEQ_SYS_QUESTION_QSEQ') order by sequence_name");
        while (r.next()) System.out.println(r.getString(1) + "\t" + r.getLong(2));
        r.close(); c.close();
    }
    static Connection conn(String h, String svc, String u, String p) throws SQLException {
        return DriverManager.getConnection("jdbc:oracle:thin:@" + h + ":1521:" + svc, u, p);
    }
}
