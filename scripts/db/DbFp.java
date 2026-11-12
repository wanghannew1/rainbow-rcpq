import java.sql.*;
import java.math.BigDecimal;
/** 复现 INCR 指纹: 两库同列 ORDER BY ikey 前 5 行, 打印行指纹与首行逐列对比 */
public class DbFp {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Class.forName("org.postgresql.Driver");
        String cols = a[0]; // 逗号分隔(顺序一致)
        String pk = a[1];
        Connection ora = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        Connection pg = DriverManager.getConnection("jdbc:postgresql://localhost:5432/rainbow_rcpq", "rcpq", "rcpq_dev");
        PreparedStatement po = ora.prepareStatement("SELECT " + cols + " FROM (SELECT t.*, ROW_NUMBER() OVER (PARTITION BY " + pk + " ORDER BY aae036 DESC NULLS LAST, ROWID) rn FROM tc90_list t) WHERE rn = 1 ORDER BY " + pk);
        po.setFetchSize(2000);
        ResultSet ro = po.executeQuery();
        PreparedStatement pp = pg.prepareStatement("SELECT " + cols + " FROM tc90_list ORDER BY " + pk);
        pp.setFetchSize(2000);
        ResultSet rp = pp.executeQuery();
        int n = 0, firstDiff = -1;
        String[] co = cols.split(",");
        long scanned = 0;
        while (ro.next() && rp.next() && n < 3) {
            System.out.println("--- 行 " + (n + 1) + " ---");
            for (int i = 0; i < co.length; i++) {
                Object vo = ro.getObject(i + 1), vp = rp.getObject(i + 1);
                String so = vo == null ? "N" : vo instanceof BigDecimal ? ((BigDecimal)vo).stripTrailingZeros().toPlainString() : vo instanceof Timestamp ? "T" + ((Timestamp)vo).getTime() : vo instanceof java.sql.Date ? "D" + ((java.sql.Date)vo).getTime() : String.valueOf(vo);
                String sp = vp == null ? "N" : vp instanceof BigDecimal ? ((BigDecimal)vp).stripTrailingZeros().toPlainString() : vp instanceof Timestamp ? "T" + ((Timestamp)vp).getTime() : vp instanceof java.sql.Date ? "D" + ((java.sql.Date)vp).getTime() : String.valueOf(vp);
                if (!so.equals(sp)) {
                    System.out.println("  不同: " + co[i].trim() + " ORA[" + so + "] PG[" + sp + "]");
                    if (firstDiff < 0) firstDiff = i;
                }
            }
            scanned++; n++;
        }
        System.out.println("比对行数=" + scanned + " 首个差异列=" + (firstDiff < 0 ? "无" : co[firstDiff]));
    }
}
