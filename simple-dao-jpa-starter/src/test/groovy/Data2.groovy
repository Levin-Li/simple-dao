import java.util.concurrent.atomic.AtomicLong

def dir = new File("/Users/llw/Downloads/2019年订单/2019交易清单")

def outDir = new File(dir, "data")


def fileMap = [:]

dir.eachFile {

    if (!it.isFile()
            || !it.name.contains('月')) {
        println "忽略的文件" + it
        return
    }

    println it

    def s = it.name.split('月')[0]


    def list = fileMap[s + '月'] as List

    if (list == null) {
        list = new ArrayList()
        fileMap[s + '月'] = list
    }

    list.add(it.name)

}

println fileMap


outDir.mkdirs()


final AtomicLong total = new AtomicLong()

final AtomicLong totalRows = new AtomicLong()

def monthRows = [:]
def alipayMonthlySum = [:]
def wxpayMonthlySum = [:]

fileMap.entrySet()
        .parallelStream()
        .map({ entry ->

            double monthSummary = 0
            Random random = new Random()

            println "开始统计 $entry.key ..."

            (entry.value as List).each { fn ->

                new File(dir, fn).readLines("utf-8")
                        .stream()
                        .filter({ it.length() > 0 })
                        .map({ v -> v.replace("测试", "") })
                        .forEach(
                                {

                                    def rows = it.split(',')

                                    rows[0] = UUID.randomUUID().toString()

                                    double amount = Double.parseDouble(rows[2])

                                    if (amount < 1) {
                                        amount = 7.0 + random.nextInt(100)
                                        rows[2] = "" + amount
                                    }

                                    total.addAndGet(amount.longValue())
                                    totalRows.incrementAndGet()

                                    def strings = rows[3].split('-')

                                    String key = strings[0] + "-" + strings[1]

                                    Map dataMap = alipayMonthlySum

                                    //偶数行是微信
                                    if (totalRows.longValue() % 2 == 0) {
                                        dataMap = wxpayMonthlySum
                                    }

                                    def sum = dataMap.get(key) as AtomicLong

                                    if (sum == null) {
                                        sum = new AtomicLong()
                                        dataMap.put(key, sum)
                                    }

                                    sum.addAndGet(amount.longValue())


                                    def n = monthRows.get(key) as AtomicLong
                                    if (n == null) {
                                        n = new AtomicLong()
                                        monthRows.put(key, n)
                                    }

                                    n.incrementAndGet()

                                    // outFile.append(line + "\n", "utf-8")
                                }
                        )


                // println "$fn : $monthSummary"

            }

            //  println "$entry.key :  " + monthSummary.longValue()

        }).count()


def statFile = new File(dir, "2019年统计数据.csv")

statFile.write("月份 , 支付宝交易金额 , 微信交易金额 ,  交易笔数\n")

alipayMonthlySum.each {

    def line = it.key + "," + it.value +" ," + wxpayMonthlySum.get(it.key) + "," + monthRows.get(it.key) + "\n"

    statFile.append(line)
    print line
}

statFile.append("总计，$total ，$totalRows \n")

println "total: $total , $totalRows"



