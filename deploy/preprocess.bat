@rem %1 -- source dir
@rem %2 -- dest dir

@REM 把源目录的html文件格式简化，并修正图片、链接路径，放到目的目录

java -jar htmlprocessor.jar  -i ../ -l ../ -s %1 -d %2
