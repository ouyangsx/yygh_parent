package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Autowired
    private DictListener dictListener;

    //根据数据id查询子数据列表
    @Override
    public List<Dict> findChildData(Long id) {
        //根据id查询子数据列表
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        List<Dict> dictList = baseMapper.selectList(wrapper);

        //遍历集合
        for (Dict dict : dictList) {
            Long dictId = dict.getId();
            //判断有没有子数据
            boolean hasChildren = this.hasChildrenById(dictId);
            dict.setHasChildren(hasChildren);
        }

        return dictList;
    }

    //判断是否有子数据
    private boolean hasChildrenById(Long dictId) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",dictId);

        Integer integer = baseMapper.selectCount(wrapper);
        return integer>0;
    }

    //导出数据
    @Override
    public void exportData(HttpServletResponse response) {
        //1.设置文件参数
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            //2. 查询数据
            List<Dict> dictList = baseMapper.selectList(null);

            //3.遍历封装，将entity实体转为vo实体
            List<DictEeVo> dictEeVoList = new ArrayList<>();
            for (Dict dict : dictList) {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictEeVo);
                dictEeVoList.add(dictEeVo);
            }

            //4.写入数据
            EasyExcel.write(response.getOutputStream(),DictEeVo.class).sheet("数据字典").doWrite(dictEeVoList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //导入数据字典
    @Override
    public void importData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,dictListener).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
            throw new YyghException(20001,"导入失败");
        }
    }

    //根据参数获取数据字典名称
    @Override
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {
        //1.判断parentDictCode是否为空，如果是空，直接用value查询
        //情况一：value能唯一确定一条记录，例如省市区
        if (StringUtils.isEmpty(parentDictCode)) {
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            if (dict != null) {
                return dict.getName();
            }
        } else {
            //2.根据两个参数查询
            //2.1.根据dtict_code查询分类的一级信息
            Dict parenDict = this.getDictByDictCode(parentDictCode);
            //2.2.根据 paren_id和value查询唯一数据
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id",parenDict.getId()).eq("value",value));
            if (dict != null) {
                return dict.getName();
            }
        }
        return "";
    }

    //根据dict_code查询分类一级信息
    private Dict getDictByDictCode(String parentDictCode) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code",parentDictCode);
        Dict dict = baseMapper.selectOne(queryWrapper);
        return dict;
    }

    //根据dictCode获取下级节点
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict parentDict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", dictCode));
        List<Dict> dictList = this.findChildData(parentDict.getId());
        return dictList;
    }
}