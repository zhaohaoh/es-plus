FROM openjdk:8u222-jdk
MAINTAINER hzh
#时区问题
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
#用docker-compose.yml入口位置改变,添加app.jar进容器
ADD ./*.jar app.jar
ADD ./arthas/ /arthas
ADD ./config /config
# COPY ./lib /lib
#-Xmn300m  年轻代大小。让G1自动分配
#-XX:MetaspaceSize=256M  元空间
#-XX:MaxGCPauseMillis=200  年轻代最大停顿时间  200毫秒 达不到会进行多次年轻gc 速度跟不上反而会导致fullgc增加
#-XX:+UseG1GC  使用G1垃圾回收器
#-XX:MaxGCPauseMillis=200  最大停顿毫秒200，只能尽可能达到 　G1会通过调整Java堆尺寸大小来满足设定的暂停时间目标，暂停时间目标越短，年轻代空间越小，老年代空间相对越大
#-XX:MaxTenuringThreshold=15   年龄15的对象进老年代
#-XX:G1HeapRegionSize= G1分区大小

#-XX:+UseConcMarkSweepGC 使用cms垃圾收集器
#-XX:CMSMaxAbortablePrecleanTime=1000   cms停顿时间为1秒
#-XX:CMSScheduleRemarkEdenPenetration=20  代表Eden区使用比例超过20%就结束该阶段进入remark 因为cmsgc之前肯定已经走过年轻代gc了
#-XX:+CMSScavengeBeforeRemark  cmsremark前先进行一次年轻gc

#-XX:+HeapDumpOnOutOfMemoryError \  堆异常日志
#-XX:HeapDumpPath=/logs/heap/ \ 堆异常日志
#-XX:+PrintGC 输出GC日志
#-XX:+PrintGCDetails 输出GC的详细日志
#-XX:+PrintGCTimeStamps 输出GC的时间戳（以基准时间的形式）
#-XX:+PrintGCDateStamps 输出GC的时间戳（以日期的形式，如 2013-05-04T21:53:59.234+0800）
#-XX:+PrintHeapAtGC 在进行GC的前后打印出堆的信息
#-Xloggc:/logs/gc/seckill_gc-%t.log \  输出日志
#-XX:+UseGCLogFileRotation \     循环输出
#-XX:NumberOfGCLogFiles=5 \    5个文件
#-XX:GCLogFileSize=20M \     一个20兆
#Xms Xmx最小最大堆内存，-Xmn新生代内存，推荐总堆3/8 高并发服务器适度提高。-XX:SurvivorRatio 新生代比例1/8
#Xss是一个线程内存，设置过小在执行递归，或者for循环。就是一个线程频繁执行会不够，抛异常。太大创建线程数变少
#打印oom异常到logs下的heap

ENV JAVA_OPTS="\
-server \
-Xms556m \
-Xmx556m \
-Xmn300m \
-XX:MetaspaceSize=256M \
-Xss256k \
-XX:SurvivorRatio=8 \
-XX:MaxGCPauseMillis=200 \
-XX:+UseConcMarkSweepGC \
-XX:+CMSScavengeBeforeRemark \
-XX:CMSMaxAbortablePrecleanTime=1000 \
-XX:CMSScheduleRemarkEdenPenetration=20 \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/logs/heap/ \
"
#gc日志暂时不放进去
#-XX:+PrintGCDetails \
#-XX:+PrintGCDateStamps \
#-XX:+PrintHeapAtGC \
#-Xloggc:/logs/shop-admin/gc/gc-%t.log \
#-XX:+UseGCLogFileRotation \
#-XX:NumberOfGCLogFiles=5 \
#-XX:GCLogFileSize=20M \

#ENV JAVA_AGENT="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
#5个日志文件 一个20M轮流
# ${JAVA_AGENT}
ENTRYPOINT java ${JAVA_OPTS} -jar -Dserver.port=8808 -Dspring.config.location=/config/ -Dloader.path=/lib -Djava.security.egd=file:/dev/./urandom app.jar