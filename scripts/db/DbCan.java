import java.sql.*;
/** 诊断: 取 Oracle 表某行逐列类型/值, 与 PG 同行比对指纹差异 */
public class DbCan {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection ora = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"), System.getenv("ORA_PASS"));
        Class.forName("org.postgresql.Driver");
        Connection pg = DriverManager.getConnection("jdbc:postgresql://localhost:5432/rainbow_rcpq", "rcpq", "rcpq_dev");
        String t = a[0], pk = a[1], key = a[2];
        PreparedStatement po = ora.prepareStatement("SELECT * FROM " + t + " WHERE " + pk + "=?");
        po.setObject(1, new java.math.BigDecimal(key));
        ResultSet ro = po.executeQuery(); ro.next();
        ResultSetMetaData mo = ro.getMetaData();
        PreparedStatement pp = pg.prepareStatement("SELECT * FROM " + t.toLowerCase() + " WHERE " + pk + "=?");
        pp.setObject(1, new java.math.BigDecimal(key));
        ResultSet rp = pp.executeQuery(); rp.next();
        ResultSetMetaData mp = rp.getMetaData();
        int diff = 0;
        for (int i = 1; i <= mo.getColumnCount(); i++) {
            String name = mo.getColumnName(i);
            Object vo = ro.getObject(name), vp = rp.getObject(name.toLowerCase());
            String so = vo == null ? "\\0" : vo.getClass().getSimpleName() + ":" + (vo instanceof java.math.BigDecimal ? ((java.math.BigDecimal)vo).stripTrailingZeros().toPlainString() : vo instanceof Timestamp ? String.valueOf(((Timestamp)vo).getTime()) : vo instanceof java.sql.Date ? String.valueOf(((java.sql.Date)vo).getTime()) : vo);
            String sp = vp == null ? "\\0" : vp.getClass().getSimpleName() + ":" + (vp instanceof java.math.BigDecimal ? ((java.math.BigDecimal)vp).stripTrailingZeros().toPlainString() : vp instanceof Timestamp ? String.valueOf(((Timestamp)vp).getTime()) : vp instanceof java.sql.Date ? String.valueOf(((java.sql.Date)vp).getTime()) : vp);
            if (!so.equals(sp)) {
                System.out.println(name + " ORA[" + so + "] PG[" + sp + "] oraType=" + mo.getColumnTypeName(i));
                if (++diff >= 12) break;
            }
        }
        System.out.println("diffCols=" + diff);
    }
}
