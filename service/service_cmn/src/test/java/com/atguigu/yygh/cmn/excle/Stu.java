package com.atguigu.yygh.cmn.excle;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class Stu {

    //设置表头名称
    //设置列对应的属性
    @ExcelProperty(value = "学生编号",index = 0)
    private int sno;

    //设置表头名称
    //设置列对应的属性
    @ExcelProperty(value = "学生姓名",index = 1)
    private String name;
}
