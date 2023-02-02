package com.levin.commons.dao.codegen.db;

import com.levin.commons.dao.codegen.db.util.FieldUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * SQL上下文,这里可以取到表,字段信息<br>
 * 最终会把SQL上下文信息放到velocity中
 */
public class SQLContext {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final LocalDateTime localDateTime = LocalDateTime.now();

    /**
     * 表结构定义
     */
    private final TableDefinition tableDefinition;
    private final JavaColumnDefinition javaPkColumn;
    /**
     * 包名
     */
    private String packageName;

    /**
     * 包的子路径
     */
    private String packageSubPath;

    /**
     * 删除的前缀
     */
    private String delPrefix;

    /**
     * 数据库名
     */
    private String dbName;

    /**
     * 作者名
     */
    private String author;

    public SQLContext(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
        // 默认为全字母小写的类名
        this.packageName = getJavaBeanName().toLowerCase();
        this.javaPkColumn = (JavaColumnDefinition) this.tableDefinition.getPkColumn();
    }

    public String getDatetime() {
        return localDateTime.format(DATE_TIME_FORMATTER);
    }

    public String getDate() {
        return localDateTime.format(DATE_FORMATTER);
    }

    public String getTime() {
        return localDateTime.format(TIME_FORMATTER);
    }

    public int getRandomInt() {
        return RandomUtils.nextInt(1,Integer.MAX_VALUE);
    }

    public long getRandomLong() {
        return RandomUtils.nextLong();
    }

    public boolean getRandomBoolean() {
        return RandomUtils.nextBoolean();
    }

    public String getUuid() {
        return UUID.randomUUID().toString();
    }

    public long getNextId() {
        return IdWorker.getInstance().nextId();
    }

    /**
     * 返回Java类名
     *
     * @return
     */
    public String getJavaBeanName() {
        return getClassName();
    }

    /**
     * 返回类名
     * @return
     */
    public String getClassName() {
        String tableName = getJavaBeanNameLF();
        return FieldUtil.upperFirstLetter(tableName);
    }

    /**
     * 返回Java类名且首字母小写
     *
     * @return
     */
    public String getJavaBeanNameLF() {
        String tableName = tableDefinition.getTableName();
        if(delPrefix != null){
            String[] split = delPrefix.split("\\s*,\\s*");
            for (String prefix : split){
                tableName = StringUtils.removeStart(tableName, prefix);
            }
        }

        tableName = FieldUtil.underlineFilter(tableName);
        tableName = FieldUtil.dotFilter(tableName);
        return FieldUtil.lowerFirstLetter(tableName);
    }

    /**
     * 返回Java类名全小写
     *
     * @return
     */
    public String getJavaBeanNameL() {
        return getJavaBeanNameLF().toLowerCase();
    }

    /**
     * 返回Java类名驼峰转横杠
     *
     * @return
     */
    public String getJavaBeanNameHB() {
        String tableName = tableDefinition.getTableName();
        if(delPrefix != null){
            String[] split = delPrefix.split("\\s*,\\s*");
            for (String prefix : split){
                tableName = StringUtils.removeStart(tableName, prefix);
            }
        }

        tableName = tableName.replace("_","-");
        tableName = FieldUtil.dotFilter(tableName);
        return tableName;
    }

    public String getPkName() {
        if (javaPkColumn != null) {
            return javaPkColumn.getColumnName();
        }
        return "";
    }

    public String getPkCamelCaseName() {
        if (javaPkColumn != null) {
            return javaPkColumn.getCamelCaseName();
        }
        return "";
    }

    public String getJavaPkType() {
        if (javaPkColumn != null) {
            return javaPkColumn.getJavaType();
        }
        return "";
    }

    public String getMybatisPkType() {
        if (javaPkColumn != null) {
            return javaPkColumn.getMybatisJdbcType();
        }
        return "";
    }

    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDelPrefix() {
        return delPrefix;
    }

    public void setDelPrefix(String delPrefix) {
        this.delPrefix = delPrefix;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPackageSubPath() {
        return packageSubPath;
    }

    public void setPackageSubPath(String packageSubPath) {
        this.packageSubPath = packageSubPath;
    }
}
