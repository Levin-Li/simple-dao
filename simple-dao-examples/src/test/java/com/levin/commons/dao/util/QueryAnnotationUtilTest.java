package com.levin.commons.dao.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by echo on 2016/8/1.
 */
public class QueryAnnotationUtilTest {


    public static String toSnapshotVersion(String version) {

        //notBlank( version, "version can neither be null, empty nor blank" );

        int lastHyphen = version.lastIndexOf('-');

        if (lastHyphen > 0) {
            int prevHyphen = version.lastIndexOf('-', lastHyphen - 1);
            if (prevHyphen > 0) {
                Matcher m = VERSION_FILE_PATTERN.matcher(version);
                if (m.matches()) {
                    return m.group(1) + "-" + SNAPSHOT_VERSION;
                }
            }
        }
        return version;
    }

    static final Pattern VERSION_FILE_PATTERN = Pattern.compile("^(.*)-([0-9]{8}\\.[0-9]{6})-([0-9]+)$");

    static final String RELEASE_VERSION = "RELEASE";

    static final String LATEST_VERSION = "LATEST";

    static final String SNAPSHOT_VERSION = "SNAPSHOT";


    private Map<String, Object> param = new LinkedHashMap<>();



    @Before
    public void init() {

        param.put("Q_name", "llw");
        param.put("nickName", "llw");
        param.put("Q_Like_name", "llw");
        param.put("Q_Gt_date1", new Date());
        param.put("Q_Lt_date2", new Date());
        param.put("Q_Gte_date3", new Date());
        param.put("Q_Lte_date4", new Date());
        param.put("Q_Not_gt_date5", new Date());
        param.put("Q_NotLike_date6", new Date());
        param.put("Q_NotEq_date7", new Date());

        param.put("Q_NotNull_date8", new Date());
        param.put("Q_NotLike_date9", new Date());


        param.put("Q_NotLike_", "llw");
        param.put("Q_name1", "llw");
        param.put("Q_Not_Contains_name2", "llw");
        param.put("Q_StartsWith_name3", "llw");

        param.put("Q_Not_EndsWith_name5", "llw");
        param.put("name6", "llw");


    }

    @Test
    public void testWalkMap() throws Exception {


        System.out.println(toSnapshotVersion("2.2.27-SNAPSHOT"));
        System.out.println(toSnapshotVersion("2.2.27-7f07bc4c8e-1"));
        System.out.println(toSnapshotVersion("2.2.27-SNAPSHOT"));
        System.out.println(toSnapshotVersion("2.2.27-SNAPSHOT"));


    }


    @Test
    public void testSqlParser() throws Exception {


        String selectColumns = "(new java.lang.Number(mm,t.aa,c.dd)) as aaa ,to_Cate('aaa',toChar('yy-mmdd')) as aa, count( * ) , aa, sum(a.cc), t.bb, df_aa , method()";

        //String aa = SelectDaoImpl.foundColumn("test", selectColumns);


//

//        List<String[]> test = QLUtils.parseSelectColumns(null, selectColumns, "testM,'adsfasdfads',123213,354654,-123123,cnt");

//        System.out.println(test);

    }

}