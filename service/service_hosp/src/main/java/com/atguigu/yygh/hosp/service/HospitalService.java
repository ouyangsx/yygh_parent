package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;

public interface HospitalService {

    //保存医院信息
    void save(Map<String, Object> paramMap);

    //查询医院信息
    Hospital getByHoscode(String hoscode);

    //带分页、条件查询医院信息列表
    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    //更新上线状态
    void updateStatus(String id, Integer status);

    //获取医院详情
    Map<String, Object> getHospById(String id);

    //根据编码获取医院名称
    String getHospName(String hoscode);
}
