import java.sql.*;
import java.util.*;

/** 名单表族历史搬迁(一次性, 可重跑): 备份库 Oracle → PG, 仅 aae002<=202610 历史月。
 *  202611+ = 新系统自留地, 本工具不碰; 每晚同步亦同口径(OracleSyncJob Spec.maxAae002)。
 *  搬完自动抬 PG 序列: setval(seq, GREATEST(max(pk)+1, 10000001)) 防 11 月后新发号撞历史主键。
 *  用法: java -cp .:ojdbc14.jar:postgresql.jar BackfillRoster */
public class BackfillRoster {
    static final String ORA_URL = "jdbc:oracle:thin:@172.16.0.60:1521:orcl";
    static final String ORA_U = "ccrcpq", ORA_P = "ccrc";
    static final String PG_URL = "jdbc:postgresql://localhost:5432/rainbow_rcpq?reWriteBatchedInserts=true";
    static final String PG_U = "rcpq", PG_P = "rcpq_dev";
    static final int CUTOFF = 202610;
    static final String[][] TABLES = { // 表, 主键, PG 序列
        {"TC9W",  "ATC9W0", "seq_tc9w_atc9w0"},
        {"TC9W1", "ATC9W0", "seq_tc9w1_atc9w0"},
        {"TC9W2", "ATC9W0", "seq_tc9w2_atc9w0"},
        {"TC9W3", "ATC9W0", "seq_tc9w3_atc9w0"},
        {"TC9W4", "ATC9W0", "seq_tc9w4_atc9w0"},
        {"TC9U",  "ATC9U0", "seq_tc9u_atc9u0"},
    };

    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Class.forName("org.postgresql.Driver");
        Connection ora = DriverManager.getConnection(ORA_URL, ORA_U, ORA_P);
        Connection pg = DriverManager.getConnection(PG_URL, PG_U, PG_P);
        pg.setAutoCommit(false);
        for (String[] t : TABLES) {
            String tab = t[0], pk = t[1], seq = t[2];
            // 列交集: Oracle user_tab_columns ∩ PG information_schema, 保 Oracle 顺序
            List<String> cols = new ArrayList<>();
            PreparedStatement pc = ora.prepareStatement(
                "SELECT column_name FROM user_tab_columns WHERE table_name=? ORDER BY column_id");
            pc.setString(1, tab);
            ResultSet rc = pc.executeQuery();
            Set<String> pgCols = new HashSet<>();
            PreparedStatement pd = pg.prepareStatement(
                "SELECT upper(column_name) FROM information_schema.columns WHERE table_name=?");
            pd.setString(1, tab.toLowerCase());
            ResultSet rd = pd.executeQuery();
            while (rd.next()) pgCols.add(rd.getString(1));
            while (rc.next()) if (pgCols.contains(rc.getString(1))) cols.add(rc.getString(1));
            if (!cols.contains(pk)) { System.out.println(tab + "|主键列缺失, 跳过"); continue; }
            int ins = 0, skip = 0;
            String colList = String.join(",", cols);
            String ph = String.join(",", Collections.nCopies(cols.size(), "?"));
            String upsql = "INSERT INTO " + tab.toLowerCase() + " (" + colList + ") VALUES (" + ph + ") ON CONFLICT (" + pk + ") DO NOTHING";
            PreparedStatement insPs = pg.prepareStatement(upsql);
            PreparedStatement selPs = ora.prepareStatement(
                "SELECT " + colList + " FROM " + tab + " WHERE aae002<=? ORDER BY " + pk);
            selPs.setInt(1, CUTOFF);
            ResultSet r = selPs.executeQuery();
            int n = 0;
            while (r.next()) {
                for (int i = 0; i < cols.size(); i++) insPs.setObject(i + 1, r.getObject(i + 1));
                insPs.addBatch();
                if (++n >= 500) { int[] res = insPs.executeBatch(); pg.commit();
                    for (int x : res) if (x == Statement.SUCCESS_NO_INFO) ins++; else if (x == 0) skip++; else if (x > 0) ins += x;
                    n = 0; }
            }
            if (n > 0) { int[] res = insPs.executeBatch(); pg.commit();
                for (int x : res) if (x == Statement.SUCCESS_NO_INFO) ins++; else if (x == 0) skip++; else if (x > 0) ins += x; }
            System.out.println(tab + "|历史行入库 ins=" + ins + " 已存在跳过=" + skip);
            // 序列抬段: GREATEST(max(pk)+1, 10000001)
            ResultSet mx = pg.createStatement().executeQuery(
                "SELECT COALESCE(max(" + pk.toLowerCase() + "),0)+1 FROM " + tab.toLowerCase());
            mx.next();
            long next = Math.max(mx.getLong(1), 10000001L);
            try {
                ResultSet sv = pg.createStatement().executeQuery("SELECT setval('" + seq + "', " + next + ")");
                if (sv.next()) System.out.println(tab + "|序列 " + seq + " 抬至 " + next);
            } catch (Exception e) { System.out.println(tab + "|序列 " + seq + " 抬段失败: " + e.getMessage()); }
        }
        pg.setAutoCommit(true);
        pg.close(); ora.close();
        System.out.println("搬迁完成(仅历史月 <=202610; 202611+ 为新系统自留地)");
    }
}
