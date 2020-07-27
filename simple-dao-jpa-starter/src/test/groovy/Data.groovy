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


def months = [:]

fileMap.entrySet()
        .parallelStream()
        .map({ entry ->

            double monthSummary = 0
            Random random = new Random()


            (entry.value as List).each { fn ->

                new File(dir, fn).readLines("utf-8")
                        .stream()
                        .filter({ it.length() > 0 })
                        .map({ v -> v.replace("测试", "") })
                        .forEach(
                                {

                                    //
                                    if (total.longValue() > (1183700000 + 24651)) {

                                        return
                                    }


                                    def rows = it.split(',')
                                    rows[0] = UUID.randomUUID().toString()

                                    double amount = Double.parseDouble(rows[2])

                                    if (amount < 1) {
                                        amount = 7.0 + random.nextInt(100)
                                        rows[2] = "" + amount
                                    }

                                    totalRows.addAndGet(1)

                                    def outFile = new File(outDir, entry.key + ".txt")

                                    def m = entry.key.toString()

                                    if (m.equals("1月")) {
                                        rows[1] = rows[1].replace("201901", "201909")
                                        rows[3] = rows[3]
                                                .replace("-01-", "-09-")
                                        outFile = new File(outDir, "9月.txt")
                                    } else if (m.equals("2月")) {
                                        rows[1] = rows[1].replace("201902", "201910")
                                        rows[3] = rows[3]
                                                .replace("-02-", "-10-")
                                        outFile = new File(outDir, "10月.txt")
                                    } else if (m.equals("3月")) {
                                        rows[1] = rows[1].replace("201903", "201911")
                                        rows[3] = rows[3]
                                                .replace("-03-", "-11-")
                                        outFile = new File(outDir, "11月.txt")
                                    } else if (m.equals("7月")) {

                                    }


                                    total.addAndGet(amount.longValue())

                                    monthSummary += amount

                                    def line = ""

                                    rows.each {
                                        if (line.length() > 0) {
                                            line += ","
                                        }

                                        line += it
                                    }

                                   // outFile.append(line + "\n", "utf-8")
                                }
                        )


                // println "$fn : $monthSummary"

            }

            println "$entry.key :  " + monthSummary.longValue()

        }).count()


println "total: $total , $totalRows"

