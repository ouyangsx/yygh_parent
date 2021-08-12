package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private DictFeignClient dictFeignClient;

    //保存医院信息
    @Override
    public void save(Map<String, Object> paramMap) {
        //1.封装参数数据
        //先把map转化成json字符串，fastjson提供的功能
        String hospJsonStr = JSONObject.toJSONString(paramMap);
        //把json字符串转换成对象
        Hospital hospital = JSONObject.parseObject(hospJsonStr, Hospital.class);

        //2.判断医院信息是否存在
        Hospital targetHospital = hospitalRepository.getHospitalByHoscode(hospital.getHoscode());
        if (targetHospital != null){
            //3.mongo里有医院信息，更新
            hospital.setStatus(targetHospital.getStatus());
            hospital.setCreateTime(targetHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            //注意此项
            hospital.setId((targetHospital.getId()));
            hospitalRepository.save(hospital);
        } else {
            //3.mongo里没有医院信息，进行新增操作
            //0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    //查询医院信息
    @Override
    public Hospital getByHoscode(String hoscode) {

        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }

    //带分页、条件查询医院信息列表
    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //1.创建排序对象
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //2.设置分页对象，0是第一页
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        //3.设置查询条件
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        //4.设置查询规则
        ExampleMatcher macher = ExampleMatcher.matching()//构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)//改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true);//改变默认大小写忽略方式：忽略大小写

        //5.创建模板实例
        Example<Hospital> example = Example.of(hospital,macher);
        Page<Hospital> hospitalPage = hospitalRepository.findAll(example, pageable);

        //跨模块调用查询字典服务，翻译编码信息
        //遍历封装医院等级数据、地址相关信息
        hospitalPage.getContent().stream().forEach(item->{//stream流
            this.packHospital(item);//将封装的对象返回到stream流中
        });
        return hospitalPage;
    }

    //更新上线状态
    @Override
    public void updateStatus(String id, Integer status) {
        if (status.intValue() == 0 || status.intValue() == 1){
            //根据id查询
            Hospital hospital = hospitalRepository.findById(id).get();
            //更新属性、时间
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    //获取医院详情
    @Override
    public Map<String, Object> getHospById(String id) {
        Map<String, Object> result = new HashMap<>();
        //根据id查询数据（进行字段编码翻译）
        Hospital hospital = this.packHospital(hospitalRepository.findById(id).get());
        //医院基本信息（包含医院等级）
        result.put("hospital",hospital);
        //单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return result;
    }

    //根据编码获取医院名称
    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = this.getByHoscode(hoscode);
        if (hospital!=null){
            return hospital.getHosname();
        }

        return "";
    }

    //字段编码翻译拼接
    private Hospital packHospital(Hospital hospital) {
        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(),hospital.getHostype());
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());

        //getParam为Hospital集成的父类中声明的字段，可将额外的参数封装到对象中进行返回
        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());
        return hospital;
    }
}
