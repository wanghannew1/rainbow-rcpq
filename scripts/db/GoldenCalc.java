import java.sql.*;
import java.io.*;

/** 只读黄金对照: 匿名块调用原 PKG_人才派遣公共.PRC_计算险金(计算过程本身无写入),
 *  输出 atc900/code/msg/关键字段 CSV。用法: java GoldenCalc <年月> <数量> */
public class GoldenCalc {
    public static void main(String[] a) throws Exception {
        String ym = a[0];
        int limit = Integer.parseInt(a[1]);
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection(
            "jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            System.getenv().getOrDefault("ORA_USER", "ccrcpq"),
            System.getenv("ORA_PASS"));
        // 取有结算单元且非劳务的样本
        ResultSet rs = c.createStatement().executeQuery(
            "SELECT atc900 FROM (SELECT t.atc900 FROM tc90 t, tb93 b WHERE t.atb930 = b.atb930 " +
            "AND t.aae00p <> '2' AND ROWNUM <= " + (limit * 3) + " ORDER BY dbms_random.value) WHERE ROWNUM <= " + limit);
        java.util.List<Long> ids = new java.util.ArrayList<>();
        while (rs.next()) ids.add(rs.getLong(1));
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream("work/golden_calc_" + ym + ".csv"), "UTF-8"));
        w.println("atc900,code,msg,aaa301,aaa302,aaa303,aaa304,aaa305,aaa306,bba001,bba002,bba003,bba004,bba005,atb90t,baa001,baa002,baa003,daa001,daa002,caa001,caa002,bba000,baa000,bba200,baa200,atc9eh,atb90k,atb90l,atb90m,atb90n,atb90p,atb90q,atb90r,atb90s_dw,atb90s_gr");
        int n = 0, errs = 0;
        for (Long atc900 : ids) {
            try {
                CallableStatement cs = c.prepareCall(
                    "DECLARE " +
                    " v_atc900 number := ?; " +
                    " r_tc90 tc90%rowtype; r_xjbl tc01%rowtype; r_out tc01%rowtype; " +
                    " p_code number; p_msg varchar2(500); " +
                    "BEGIN " +
                    " SELECT * INTO r_tc90 FROM tc90 WHERE atc900 = v_atc900; " +
                    " PKG_人才派遣公共.PRC_计算险金(r_tc90, ?, r_xjbl, r_out, p_code, p_msg); " +
                    " ? := v_atc900; ? := p_code; ? := p_msg; " +
                    " ? := r_out.aaa301; ? := r_out.aaa302; ? := r_out.aaa303; ? := r_out.aaa304; ? := r_out.aaa305; ? := r_out.aaa306; " +
                    " ? := r_out.bba001; ? := r_out.bba002; ? := r_out.bba003; ? := r_out.bba004; ? := r_out.bba005; ? := r_out.atb90t; " +
                    " ? := r_out.baa001; ? := r_out.baa002; ? := r_out.baa003; ? := r_out.daa001; ? := r_out.daa002; " +
                    " ? := r_out.caa001; ? := r_out.caa002; ? := r_out.bba000; ? := r_out.baa000; ? := r_out.bba200; ? := r_out.baa200; ? := r_out.atc9eh; " +
                    " ? := r_out.atb90k; ? := r_out.atb90l; ? := r_out.atb90m; ? := r_out.atb90n; ? := r_out.atb90p; ? := r_out.atb90q; ? := r_out.atb90r; " +
                    " ? := r_out.atb90s_dw; ? := r_out.atb90s_gr; " +
                    "END;");
                cs.setLong(1, atc900);
                cs.setString(2, ym);
                cs.registerOutParameter(3, Types.NUMERIC);
                cs.registerOutParameter(4, Types.NUMERIC);
                cs.registerOutParameter(5, Types.VARCHAR);
                for (int i = 6; i <= 38; i++) cs.registerOutParameter(i, Types.NUMERIC);
                cs.execute();
                StringBuilder sb = new StringBuilder();
                sb.append(cs.getLong(3)).append(',')
                  .append(cs.getInt(4)).append(',')
                  .append(cs.getString(5) == null ? "" : cs.getString(5).replace(",", ";"));
                for (int i = 6; i <= 38; i++) {
                    Object v = cs.getObject(i);
                    sb.append(',').append(v == null ? "" : v.toString());
                }
                w.println(sb);
                n++;
                if (cs.getInt(4) < 0) errs++;
            } catch (Exception e) {
                System.out.println("skip " + atc900 + ": " + e.getMessage());
                errs++;
            }
        }
        w.close();
        System.out.println("golden rows=" + n + " errs=" + errs + " -> work/golden_calc_" + ym + ".csv");
        c.close();
    }
}
