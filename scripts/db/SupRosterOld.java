import java.sql.*;
import java.io.*;

/** 桥单 20261127-045 花名册旧侧实弹(RCPQ_TEST): 隔离键同 041 探针(20990961/20990601),
 *  callable PKG_工资发放.PRC_工资花名册 → TC93 造册行全列 dump → 清场。 */
public class SupRosterOld {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            "rcpq_test", "rcpq_test_2026");
        out = new PrintWriter(new FileWriter("work/bc45_roster_old.txt"), true);
        exec(c, "DELETE FROM tc93 WHERE atb930=20990961");
        exec(c, "DELETE FROM tc90 WHERE atc900=20990601");
        exec(c, "DELETE FROM tb93 WHERE atb930=20990961");
        exec(c, "DELETE FROM tb90 WHERE atb900=20990962 AND aab001=20990961");
        exec(c, "DELETE FROM ab01 WHERE aab001=20990961");
        exec(c, "DECLARE r tb93%ROWTYPE; BEGIN SELECT * INTO r FROM tb93 WHERE atb930=39737; " +
            "r.atb930:=20990961; r.atb900:=20990962; r.aab001:=20990961; r.atb931:='补退探针单元卷41'; " +
            "INSERT INTO tb93 VALUES r; END;");
        exec(c, "DECLARE r tb90%ROWTYPE; BEGIN SELECT * INTO r FROM tb90 WHERE aab001=(SELECT aab001 FROM tb93 WHERE atb930=39737) AND ROWNUM=1; " +
            "r.atb900:=20990962; r.aab001:=20990961; INSERT INTO tb90 VALUES r; END;");
        exec(c, "DECLARE r ab01%ROWTYPE; BEGIN SELECT * INTO r FROM ab01 WHERE aab001=2239; " +
            "r.aab001:=20990961; r.aab004:='补退探针单位卷41'; INSERT INTO ab01 VALUES r; END;");
        exec(c, "DECLARE r tc90%ROWTYPE; BEGIN SELECT * INTO r FROM tc90 WHERE atc900=53031; " +
            "r.atc900:=20990601; r.aab001:=20990961; r.atb930:=20990961; r.atb900:=20990962; " +
            "r.aac001:=998072; r.aac002:='220183199906018801'; r.aac003:='补退探针员卷41'; " +
            "r.atb90r:='3'; r.atc90g:='1'; r.aae00p:='1'; r.atc90p:='2'; r.atc90w:='2'; " +
            "r.atc90pa:='2'; r.atc90pb:='2'; r.aac008:='11'; r.aae002:=209905; r.aae003:=209912; " +
            "INSERT INTO tc90 VALUES r; END;");
        CallableStatement cs = c.prepareCall("{call PKG_工资发放.PRC_工资花名册(?,?,?,?,?,?)}");
        cs.setString(1, "20990961"); cs.setString(2, "209906"); cs.setString(3, "1"); cs.setString(4, "jiaq");
        cs.registerOutParameter(5, Types.NUMERIC); cs.registerOutParameter(6, Types.VARCHAR);
        cs.execute();
        out.println("PROC code=" + cs.getInt(5) + " msg=" + cs.getString(6));
        PreparedStatement ps = c.prepareStatement(
            "SELECT atc930, atc900, aac003, atc931, atc937, atc93n, atc93g, atc93p, atc93w, " +
            "aae114, aae214, aae314, aae013, aae002, atc93u, atc93aa, atc93x, atb90a " +
            "FROM tc93 WHERE atb930=20990961 ORDER BY atc930");
        ResultSet r = ps.executeQuery();
        int n = 0;
        while (r.next()) {
            n++;
            out.println("[R#" + n + "] atc900=" + r.getString(2) + " aac003=" + r.getString(3) +
                " atc931=" + r.getString(4) + " atc937=" + r.getString(5) +
                " atc93n=" + r.getString(6) + " atc93g=" + r.getString(7) + " atc93p=" + r.getString(8) +
                " atc93w=" + r.getString(9) + " aae114=" + r.getString(10) + " aae214=" + r.getString(11) +
                " aae314=" + r.getString(12) + " aae013=" + r.getString(13) + " aae002=" + r.getString(14) +
                " atc93u=" + r.getString(15) + " atc93aa=" + r.getString(16) + " atc93x=" + r.getString(17) +
                " atb90a=" + r.getString(18));
        }
        out.println("ROWS=" + n);
        exec(c, "DELETE FROM tc93 WHERE atb930=20990961");
        exec(c, "DELETE FROM tc90 WHERE atc900=20990601");
        exec(c, "DELETE FROM tb93 WHERE atb930=20990961");
        exec(c, "DELETE FROM tb90 WHERE atb900=20990962 AND aab001=20990961");
        exec(c, "DELETE FROM ab01 WHERE aab001=20990961");
        ResultSet rl = c.createStatement().executeQuery(
            "SELECT (SELECT COUNT(*) FROM tc93 WHERE atb930=20990961)||'/'||(SELECT COUNT(*) FROM tc90 WHERE atc900=20990601) FROM dual");
        rl.next(); out.println("CLEANUP residue=" + rl.getString(1));
        c.close(); out.println("== END ==");
    }
    static PrintWriter out;
    static void exec(Connection c, String s) throws Exception {
        try { c.createStatement().execute(s); }
        catch (SQLException e) { out.println("SQL_ERR: " + e.getMessage().split("\n")[0]); }
    }
}
