#��������

echo start crawl
_heapMem="-Xms256m -Xmx512m"
#java $_heapMem -jar ServerManage.jar
nohup java $_heapMem -jar ServerManage.jar > crawler.log 2>&1 &
