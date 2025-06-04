通过此链接下载minio.exe
https://dl.min.io/server/minio/release/windows-amd64/minio.exe
文件目录下运行命令(win)：
minio.exe server C:\minio\data --console-address ":9001"
##C:\minio\data 路径可改为存放文件的位置
此时文件存储服务已启动，Springboot API可使用

可使用客户端进一步管理查看文件
客户端默认用户：
账号minioadmin密码minioadmin
访问客户端：
http://localhost:9001