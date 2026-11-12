import java.sql.*;
import java.io.*;
/** 生成 V15: 对同步表把 PG 侧 date 列输出 ALTER TYPE timestamp */
public class DbDateCols {
    public static void main(String[] a) throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection pg = DriverManager.getConnection("jdbc:postgresql://localhost:5432/rainbow_rcpq", "rcpq", "rcpq_dev");
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream("/home/ubuntu/github/rainbow-rcpq/rcpq-web/src/main/resources/db/V15__ora_sync_date_ts.sql"), "UTF-8"));
        w.println("-- V15__ora_sync_date_ts.sql — 同步表 date 列对齐 Oracle DATE 语义(2026-11-12)");
        w.println("-- 原因: PG date 截断时间部分, Oracle DATE 存量含时间(如 tc90.atc90e 10:58:03),");
        w.println("--       增量比对按毫秒时大面积误判; date→timestamp 为无损扩展, 存量 00:00 不变");
        for (String t : a[0].split(",")) {
            PreparedStatement ps = pg.prepareStatement(
                "SELECT column_name FROM information_schema.columns WHERE table_name=? AND data_type='date' ORDER BY ordinal_position");
            ps.setString(1, t.toLowerCase());
            ResultSet r = ps.executeQuery();
            while (r.next())
                w.println("ALTER TABLE " + t.toLowerCase() + " ALTER COLUMN " + r.getString(1).toLowerCase() + " TYPE timestamp;");
        }
        w.close();
        System.out.println("done");
    }
}
