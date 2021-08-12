package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

//使用MongoRepository
@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    //判断医院信息是否存在
    Hospital getHospitalByHoscode(String hoscode);
}
