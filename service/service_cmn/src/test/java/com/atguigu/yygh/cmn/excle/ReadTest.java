package com.atguigu.yygh.cmn.excle;

import com.alibaba.excel.EasyExcel;

public class ReadTest {
    public static void main(String[] args) {
        String fileName = "D:\\test\\01.xlsx";
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(fileName,Stu.class,new ExcelListener()).sheet().doRead();
    }
}
