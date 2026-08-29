#!/bin/bash

function setEnv() {
#    export PATH=$PATH:$HOME/.local/bin
#    export PATH=$PATH:/usr/local/bin
#    export PATH=$PATH:/usr/local/sbin
#    export PATH=$PATH:/usr/local/bin
#    export PATH=$PATH:/usr/bin
#    export PATH=$PATH:/usr/sbin
#    export PATH=$PATH:/sbin
#    export PATH=$PATH:/bin
#    export PATH=$PATH:/opt/bin
#    export PATH=$PATH:/opt/local/bin
#    export PATH=$PATH:/opt/local/sbin
   # export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64


    # 设置系统语言
    #export LANG=zh_CN.UTF-8
    #export LC_ALL=zh_CN.UTF-8

    echo "env: PATH=$PATH"
    echo "env: JAVA_HOME=$JAVA_HOME"
}

setEnv
