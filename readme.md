# 项目功能

1. 开发一个视频同步播放的 Android 软件，方便我(me)和女友(gf)看电影
2. 一个 story 是：
   1. 打开本软件，同步播放电影文件
   2. 打开微信，开源进行声音交流
   3. 能够听到电影的声音和微信中的声音

# 总体架构设计

![](res/arch.png)

# 后端服务子系统

1. app 的数据使用 MySQL 来保存，需要保存的数据有
   1. 账户和密码
   2. 登录时间

## 数据结构及其功能: 
1. user前缀: [主账户名称] 用于保存用户的在线信息
   1. 统计在线用户
   2. 对请求进行鉴权
   ```
      user:name token
      hmset user:wangkaixuan token 122EE42F-FA57-4779-96D9-EA3821DFE4DE
      hmset user:chengangrrong token 7CF2532C-539F-4574-B767-75C868A51F42
   ```   
1. session:[主账户名称] 用户保存会话信息
   1. 显示会话信息
   2. 视频进度同步
   ```   
      session:master name
         videoSHA1: xxx
         progress: xxx
         slaves: xxx

      hmset session:wangkaixuan  videoSHA1 e855149c7e691bba168579461784cac2df328709fee2bcebd328994a06557b8 progress 3600 slaves xxx
   ```   
1. slave:[主账户名称] 用户保存会话中的从设备列表
   1. 保存一个主设备会话的从设备列表
   ```   
      lpush slaves:wangkaixuan chengangrong
   ```   

## 接口

| 接口   | 功能 | 备注                             |
| ------ | ---- | -------------------------------- |
| /login | 登录 | 返回一个 UUID 作为后续通信的凭证 |

# Android 子系统

1. 登录
2. 设置当前设备为 master or slave?
3. 当前角色为 master
   1. 选择视频
   2. 播放视频，并同步进度到 server
   3. 等待 slave 加入
4. 当前角色为 slave
   1. 选择正在播放视频的主设备
   2. 在本地选择相同的视频文件
   3. 发送 slave 视频信息到服务器，服务器判断其是否与 master 视频文件相同
      1. 相同则开始同步进度给 slave，slave 设置进度，开始观看
      2. 不相同则返回错误提示，重新选择视频

# TODO

> 1. client 指 Android APP
> 1. server 指后端服务器

1. [x] 登录
   1. client 传递注册信息到 server: account, password
   2. 如果没有该用户信息将自动注册，密码要加盐保存到 MySQL user 表
   3. 对比密码的[sha3](https://keccak.team/software.html)值判断登录是否成功，使用[lib](https://github.com/aelstad/keccakj)
2. [ ] 修改密码
3. [x] 忘记密码，不需要支持该功能，重新注册另外一个账户即可
4. [ ] 视频播放进度同步
   1. 假设账户 A 是主账户，B 是从账户
   2. 则视频的播放进度是有 A 控制的
   3. A 每秒上传进度 time1 到 server
   4. B 每秒从 server 获取 time1，B 的进度为 time2
   5. 如果 time1 和 time2 误差<=3 秒内，则 B 不调整进度
   6. 如果 time1 和 time2 误差>3 秒内，则把 B 的进度设置为 A 的进度 time1
5. [ ] Android端与server端通信使用https
   1. 先使用http，后续完善

# 简记词

1. 主设备=master
2. 从设备=slave
3. 服务器=server

# 参考文档

1. [连接 MySQL 数据库](https://www.programmersought.com/article/40737747463/)
