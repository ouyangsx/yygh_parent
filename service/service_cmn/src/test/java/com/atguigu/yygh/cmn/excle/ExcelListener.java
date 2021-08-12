package com.atguigu.yygh.cmn.excle;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;

import java.util.Map;

public class ExcelListener extends AnalysisEventListener<Stu> {
    //一行一行读取excle内容
    @Override
    public void invoke(Stu stu, AnalysisContext analysisContext) {
        System.out.println("stu = " + stu);
    }

    //读取excle表头信息
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("headMap = " + headMap);
    }

    //读取完成后执行
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
