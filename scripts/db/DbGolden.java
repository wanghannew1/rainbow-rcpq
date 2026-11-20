import java.sql.*;
/** 东软侧黄金实测: 备份库直调 PRC_社保人员增减(flag='1'), 验证回盘锁文案与零写库 */
public class DbGolden {
    public static void main(String[] a) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection c = DriverManager.getConnection("jdbc:oracle:thin:@172.16.0.60:1521:orcl",
            "ccrcpq", "ccrc");
        String aaa030 = "1", aae002 = "202610", flag = "1", aae011 = "zhanghsh", aaa020 = "220100000000";
        PreparedStatement s = c.prepareStatement(
            "SELECT count(*) n1, sum(case when atc9w8='4' then 1 else 0 end) n4 FROM tc9w " +
            "WHERE aaa030=? AND aae002=? AND aaa100=?");
        s.setString(1, aaa030); s.setString(2, aae002); s.setString(3, aaa020);
        ResultSet r = s.executeQuery(); r.next();
        System.out.println("调用前 TC9W 总数=" + r.getLong(1) + " 其中'4'=" + r.getLong(2));
        CallableStatement cs = c.prepareCall(
            "{call PKG_保险人员增减.PRC_社保人员增减(?,?,?,?,?,?,?)}");
        cs.setString(1, aaa030); cs.setString(2, aae002); cs.setString(3, flag);
        cs.setString(4, aae011); cs.setString(5, aaa020);
        cs.registerOutParameter(6, Types.NUMERIC); cs.registerOutParameter(7, Types.VARCHAR);
        cs.execute();
        System.out.println("PRM_CODE=" + cs.getLong(6));
        System.out.println("PRM_MSG=" + cs.getString(7));
        r = s.executeQuery(); r.next();
        System.out.println("调用后 TC9W 总数=" + r.getLong(1) + " 其中'4'=" + r.getLong(2));
        // 对照组: 空户头 999999(第一守卫 AA03 即失败)
        cs.setString(1, "999999"); cs.setString(2, "209901");
        cs.execute();
        System.out.println("对照组(空户头) PRM_CODE=" + cs.getLong(6) + " PRM_MSG=" + cs.getString(7));
        c.close();
    }
}
