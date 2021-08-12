package com.atguigu.yygh.cmn.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
//开启事务支持
@EnableTransactionManagement
//在配置类上指定扫描的mapper包
@MapperScan("com.atguigu.yygh.cmn.mapper")
public class CmnConfig {
}
