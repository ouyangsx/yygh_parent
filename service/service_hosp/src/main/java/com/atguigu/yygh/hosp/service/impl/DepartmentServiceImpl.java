package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    //上传科室
    @Override
    public void save(Map<String, Object> paramMap) {
        //1.封装参数数据
        //先把map转化成json字符串，fastjson提供的功能
        String hospJsonStr = JSONObject.toJSONString(paramMap);
        //把json字符串转换成对象
        Department department = JSONObject.parseObject(hospJsonStr, Department.class);

        //2.根据医院编码+科室编码查询科室信息
        Department departmentExits = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());

        //3.判断是否为空
        if (departmentExits != null) {
            //更新
            department.setId(departmentExits.getId());
            department.setCreateTime(departmentExits.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        } else {
            //新增
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    /**
     * 分页查询某医院科室信息
     * @param page 当前页码
     * @param limit 每页记录数
     * @param departmentQueryVo 查询条件
     * @return
     */
    @Override
    public Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //创建分页对象pageable
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //springboot下的pageable,0为第一页
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        department.setIsDeleted(0);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher macher = ExampleMatcher.matching()//构建对象
        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)//改变默认字符串匹配方式：模糊查询
        .withIgnoreCase(true);//改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Department> example = Example.of(department,macher); //对象，查询条件构造器

        Page<Department> pages = departmentRepository.findAll(example,pageable);//分页对象，查询模板对象
        return pages;
    }

    //根据医院编号、科室编号删除科室
    @Override
    public void remove(String hoscode, String depcode) {
        //根据医院编号、科室编号查询科室
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode,depcode);
        //根据id删除
        if (department!=null){
            departmentRepository.deleteById(department.getId());
        }
    }

    //根据医院编号，查询医院所有科室列表
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //创建最终返回对象
        List<DepartmentVo> result = new ArrayList<>();
        //根据医院编号查询所有科室信息
        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        List<Department> departmentList = departmentRepository.findAll(example);
        //处理科室集合，根据大科室id进行划分，用流式计算
        //处理后的结果map分析：key=bigcode，value=大科室下所有小科室的集合
        Map<String, List<Department>> departmentMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //遍历map集合departmentMap
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            //封装大科室信息
            DepartmentVo bigDeptVo = new DepartmentVo();
            bigDeptVo.setDepcode(entry.getKey());
            bigDeptVo.setDepname(entry.getValue().get(0).getBigname());
            //封装小科室信息
            //创建封装小科室的集合
            List<DepartmentVo> deptVoList = new ArrayList<>();
            List<Department> deptList = entry.getValue();
            //遍历进行封装
            for (Department dept : deptList) {
                DepartmentVo departmentVo = new DepartmentVo();
                BeanUtils.copyProperties(dept,departmentVo);
                deptVoList.add(departmentVo);
            }
            //小科室集合存入大科室对象
            bigDeptVo.setChildren(deptVoList);
            //大科室对象存入最终返回结果
            result.add(bigDeptVo);
        }


        return result;
    }

    //根据参数获取科室名称
    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode,depcode);
        if (department!=null){
            return department.getDepname();
        }

        return "";
    }
}
