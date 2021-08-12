package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
    //根据医院编码+科室编码查询科室信息
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
