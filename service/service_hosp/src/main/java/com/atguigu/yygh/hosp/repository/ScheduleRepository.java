package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    //根据医院编码、排班编码查询排班信息
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    //根据医院编号、排班编号查询科室
    Schedule getDepartmentByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> getScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDateToDate);
}
