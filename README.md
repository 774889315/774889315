# 774889315

10.2 2:18
能单线程完成文件下载任务
能分析文件大小、文件名、文件后缀

10.2 23:36
能计算下载进度速度
每秒更新一次下载进度速度
可多任务下载
自动重命名文件防止覆盖

10.3 21:51
下载时会在通知栏显示下载进度
下载结束后通知栏有通知，有提示音和振动
能监听剪切板，并判断其内容是否可能为URL
能修改保存目录，当目录存在时才可修改成功

10.4 3:12
可多线程下载（目前设定为3）
加了暂停、继续功能
但是存在bug：下载的文件特别是压缩文件有时打开会出错；断点继续的文件会变得比原文件大。问题出在哪有待研究

10.5 23:30
修复了些bug，解决了些闪退问题。但仍存在些bug，在设法解决
加了一个服务类，能在服务中下载
添加了删除下载任务的功能

10.7 3:26
修复了多线程下载bug
添加了下载后打开文件的按钮
下载完后也可从通知栏打开文件
重整了整个程序，真正实现了下载过程在服务中进行
重整过程中顺便在服务中对Task内的变量进行了封装
修改了之前部分不太规范的代码
