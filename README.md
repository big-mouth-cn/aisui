# AI版记账程序

### 原理
首先程序对接了GPT模型接口，并给GPT开放了数据库操作、查询现实世界时间的能力。然后人类只需要通过自然语言与GPT交流即可实现日常记账、查账、统计等需求。

### 快速开始
1、 首先配置GPT和数据库信息：

- 设置GPT访问密钥：com.github.bigmouthcn.AisuiMain.ACCESS_KEY
- 设置数据库连接信息：com.github.bigmouthcn.executor.SqlExecutorFunction
- 创建数据库表：
```sql
CREATE TABLE `bill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `type` smallint(2) DEFAULT '2' COMMENT '交易类型。1：收入，2：支出',
  `classification` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '分类',
  `account` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '账户',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间',
  `description` varchar(256) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_classification` (`classification`) USING BTREE,
  KEY `idx_account` (`account`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

2、启动主程序：

- 运行 com.github.bigmouthcn.AisuiMain.main
- 自然语言的方式在命令行输入任何需求即可。

### 视频演示
[https://www.bilibili.com/video/BV17Z421h7mY/](https://www.bilibili.com/video/BV17Z421h7mY/)
