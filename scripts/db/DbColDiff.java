import java.sql.*;
import java.io.*;
/** 比对 Oracle 与 PG 列定义差异(长度/精度), 输出 PG 扩列 ALTER 语句 */
public class DbColDiff {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection ora = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        Class.forName("org.postgresql.Driver");
        Connection pg = DriverManager.getConnection("jdbc:postgresql://localhost:5432/rainbow_rcpq", "rcpq", "rcpq_dev");
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream("/home/ubuntu/github/rainbow-rcpq/rcpq-web/src/main/resources/db/V13__ora_sync_widen.sql"), "UTF-8"));
        w.println("-- V13__ora_sync_widen.sql — 同步表列宽对齐 Oracle(2026-11-12, 迁移时缩列致回盘写入超长)");
        w.println("-- 生成器: scripts/db/DbColDiff.java; 规则: varchar 长度/numeric 精度取 Oracle 侧");
        for (String t : a[0].split(",")) {
            PreparedStatement po = ora.prepareStatement(
                "SELECT column_name, data_type, char_length, data_precision, data_scale FROM user_tab_columns WHERE table_name=? ORDER BY column_id");
            po.setString(1, t);
            ResultSet ro = po.executeQuery();
            while (ro.next()) {
                String col = ro.getString(1), type = ro.getString(2);
                int clen = ro.getInt(3); int prec = ro.getInt(4); int scale = ro.getInt(5);
                PreparedStatement pp = pg.prepareStatement(
                    "SELECT data_type, character_maximum_length, numeric_precision FROM information_schema.columns WHERE table_name=? AND upper(column_name)=?");
                pp.setString(1, t.toLowerCase()); pp.setString(2, col);
                ResultSet rp = pp.executeQuery();
                if (!rp.next()) continue;
                String ptype = rp.getString(1); int plen = rp.getInt(2); int pprec = rp.getInt(3);
                if (type.startsWith("VARCHAR") && plen > 0 && clen > plen)
                    w.println("ALTER TABLE " + t.toLowerCase() + " ALTER COLUMN " + col.toLowerCase() + " TYPE varchar(" + clen + ");");
                else if (type.equals("NUMBER") && prec > 0 && pprec > 0 && (prec > pprec || scale > 0 && false))
                    w.println("ALTER TABLE " + t.toLowerCase() + " ALTER COLUMN " + col.toLowerCase() + " TYPE numeric(" + prec + "," + scale + ");");
                else if (type.equals("NUMBER") && prec == 0 && !ptype.equals("numeric"))
                    w.println("-- 注意 " + t + "." + col + ": Oracle NUMBER(无精度)→PG " + ptype);
            }
        }
        w.close();
        System.out.println("done");
    }
}
