

//注解示例
class UserStatDTO {
@GroupBy(havingOp=" like ")
String name;

//以下字段多个标题注解
@Sum(havingOp=" > ")
@Avg
int num;

@Avg
int age;

``

@Like
String txt;
}

#将生成以下语句:
#SELECT name,sum(num),avg(num),avg(age) FROM t_table WHERE txt like ? group by name HAVING name LIKE name AND sum(num) > ?

#参数值：
#txt，num




