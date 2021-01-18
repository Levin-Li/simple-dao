package com.levin.commons.dao.util;


import com.levin.commons.dao.domain.Group;
import com.levin.commons.dao.domain.User;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.domain.ServiceResponse;
import com.levin.commons.utils.MapUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.core.ResolvableType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

//import org.jsoup.Jsoup;

/**
 * Created by echo on 2016/8/10.
 */
public class ObjectUtilTest {

    static class D<T, R> {

        T field1;
        T field2;

        R value;

        int anInt;

        String string;

        Date date;

        List<T[]> listArray;

        R[] resp;

        R unFoundField;

        List<String> stringList;

        List<Integer> integerList;

        long[] longArray;

        @Desc(code = "longArray")
        List<Long> LongList;

        Map unknowTypeMap;

        @Desc(code = "unknowTypeMap")
        Map<String, Number> stringNumberMap;

        Collection noGenericList;

        //递归测试
        List<Object> endlessloopList;
    }


    static class S<T, R> {

        T field1;
        T field2;

        R value;

        String anInt = "123456789";

        int string = 1234567890;

        Date date = new Date();


        Collection<T[]> listArray = new HashSet<>();


        List<R> resp = new ArrayList<>();


        Integer[] stringList = {1, 2, 3, 4, 5, 6};


        String[] integerList = {"11", "22", "33"};

        String longArray = "1,2,3,4,5,6,7";


        List noGenericList = new ArrayList();


        Map unknowTypeMap = new HashMap();


        List endlessloopList = new ArrayList();

        {
            unknowTypeMap.put("12321312", 2142412341234L);
            unknowTypeMap.put(242341412, "12342341.234213");
            unknowTypeMap.put(new Date(), new Date().getTime());
            unknowTypeMap.put(1343324.23f, "123456.12312");
            unknowTypeMap.put(124234, "123456.12312");


            noGenericList.add("123456789");
            noGenericList.add("987654321");
            noGenericList.add(new Object());
            noGenericList.add(new Date());
            noGenericList.add(1232132.3425f);


            endlessloopList.add(this);
            endlessloopList.add(endlessloopList);

        }


    }

    public static class RESP extends ServiceResponse<Group> {

        public RESP() {
            super(null);
        }

        public RESP(Group data) {
            super(data);
        }
    }

    public static class Info extends D<User, RESP> {

        public Info() {
        }
    }

    static class Source extends S<User, RESP> {

        {
            Group g = new Group();
            g.setId(100L);
            g.setName("g100");

            field1 = new User();
            field1.setName("u100");
            field1.setId(100L);


            field1.setGroup(g);

            value = new RESP(g);

            listArray.add(new User[]{field1});
            listArray.add(new User[]{field1});
            listArray.add(new User[]{field1});
            listArray.add(new User[]{field1});

            resp.add(value);
            resp.add(value);
            resp.add(value);
            resp.add(value);

        }
    }



    @Test
    public void testSpel() throws Exception {

       String expr = "#_val != null and (!(#_val instanceof T(CharSequence)) ||  #_val.trim().length() > 0)";

        Object t = ObjectUtil.evalSpEL(null,
                expr,
                MapUtils.put("_val",(Object) 1)
                        .put("aaa", "bbb")
                        .build());

        System.out.println(t);

    }


    @Test
    public void testCopy() throws Exception {


        Info copy = ObjectUtil.copy(new Source(), null, Info.class, 2, "");

        ResolvableType resolvableType = ResolvableType.forType(copy.listArray.getClass());

        System.out.println(resolvableType.resolve());

        System.out.println(copy);

    }


    @Test
    public void testCopyFail() throws Exception {


        Source source = new Source();


        Info copy = ObjectUtil.copy(source, Info.class, "");

        ResolvableType resolvableType = ResolvableType.forType(copy.listArray.getClass());

        System.out.println(resolvableType.resolve());

        System.out.println(copy);

    }

    private static List<String> foundColumn(String defaultResult, String selectColumns) {

        // count(*) as cc, t.aa as ta,fun(a,b) as aa,fun(c,b) bb

        List<String> r = new ArrayList<>();

        for (String col : selectColumns.split(",")) {

            col = col.trim();

            int idx = col.lastIndexOf(" ");

            String colAlais = "";

            //如果没有空格，表示一定没有别名
            if (idx == -1) {
                colAlais = col;
            } else {
                colAlais = col.substring(idx);
            }

            if (!colAlais.contains("(")
                    && !colAlais.contains(")"))
                r.add(colAlais);
        }

        return r;
    }



//    @Test
    public void genQrCode() throws IOException {


        String prefix = "https://vip.dallasylmr.com/c/";


        int c = 0;


        String logo = "https://note.youdao.com/yws/public/resource/812e07354d111477f96c6ca302c8c724/xmlnote/2C00DD26DD744F1098D39AEE3D4F6A57/18240";

        logo = URLEncoder.encode(logo, "utf-8");


        int f = 0;

        while (f++ < 3) {

            c = f * 1000;

            int s = 0;

            while (s++ < 50) {

                c += 1;

                while (("" + c).contains("4")) {
                    c += 1;
                }


                URL url = new URL("http://qr.topscan.com/api.php?text=" + URLEncoder.encode(prefix + c, "utf-8") + "&logo=" + logo);


                System.out.print(c + " URL:" + url + " ...");


                int r = 100;

                while (r-- > 0) {

                    try {
                        FileUtils.copyURLToFile(url, new File("./二维码/C" + c + ".png"));
                        r = 0;
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }

                }


                System.out.println("Ok");

            }

        }

    }

    @Test
    public void testIgnore() {


//
//        Long convert = conversionService.convert("123456", long.class);
//
//        long[] values = conversionService.convert("123456,2345678,56789", long[].class);
//
//        String txtValue = conversionService.convert(123456789L, String.class);


        //   boolean id = ObjectUtil.isIgnore("id", "org.id.adf");


        boolean b = "id".startsWith("org.id.adf");
        String prefix = "org..issd.adf.12312312312.";
        b = "id".startsWith(prefix);


        String[] split = prefix.split("\\.");

        //  Assert.assertFalse(id);

    }

    @Test
    public void testConvert() throws Exception {


        //System.out.println(foundColumn2("no data", " max ( D.dd , E.cc ) ,f.A.B as aa,f.B bb,Count ( * ) cc"));
    }


    @Test
    public void clearFile() throws Exception {






    }

}