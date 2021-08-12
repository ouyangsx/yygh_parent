package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    //上传排班信息
    @Override
    public void save(Map<String, Object> paramMap) {
        //1.封装参数数据
        //先把map转化成json字符串，fastjson提供的功能
        String hospJsonStr = JSONObject.toJSONString(paramMap);
        //把json字符串转换成对象
        Schedule schedule = JSONObject.parseObject(hospJsonStr, Schedule.class);
        //根据医院编码、排班编码查询排班信息
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        //判断
        if (scheduleExist!=null) {
            //更新
            schedule.setId(scheduleExist.getId());
            schedule.setCreateTime(scheduleExist.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        } else {
            //新增
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }
    }

    //获取排班分页列表
    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        //创建分页对象pageable
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //springboot下的pageable,0为第一页
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);
        schedule.setIsDeleted(0);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher macher = ExampleMatcher.matching()//构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)//改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true);//改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Schedule> example = Example.of(schedule,macher); //对象，查询条件构造器

        Page<Schedule> pages = scheduleRepository.findAll(example,pageable);//分页对象，查询模板对象
        return pages;
    }

    //删除排班信息
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //根据医院编号、排班编号查询科室
        Schedule schedule = scheduleRepository.getDepartmentByHoscodeAndHosScheduleId(hoscode,hosScheduleId);
        //根据id删除
        if (schedule!=null){
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    //根据医院编号 和 科室编号 ，查询排班规则数据、分页数据
    @Override
    public Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode) {
        //根据查询条件，分页、统计查询规则数据
        //1.1设置查询条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //1.2分组统计
        Aggregation agg = Aggregation.newAggregation(
                //1.3匹配筛选条件
                Aggregation.match(criteria),
                //1.4分组统计
                Aggregation.group("workDate")//分组字段
                //分组统计结果
                .first("workDate").as("workDate")
                //统计医生数量
                .count().as("docCount")
                //号源数量相加
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //1.5排序
                Aggregation.sort(Sort.Direction.ASC,"workDate"),
                //1.6实现分页
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );
        
        //查询需要的数据
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();
        
        //2.根据查询条件，查询统计规则数据的总数（获取total）
        Aggregation totalAgg = Aggregation.newAggregation(
                //1.3匹配筛选条件
                Aggregation.match(criteria),
                //1.4分组统计
                Aggregation.group("workDate"));
        AggregationResults<BookingScheduleRuleVo> aggregateTotal = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoTotal = aggregateTotal.getMappedResults();

        int size = bookingScheduleRuleVoTotal.size();
        
        //3.借助工具计算出周几
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //4.封装数据
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        result.put("total",size);

        //获取医院名称
        String hosName = hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosName);
        result.put("baseMap",baseMap);
        return result;

    }

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        //1.转化workDate类型：String->Date
        Date workDateToDate = new DateTime(workDate).toDate();
        //2.调用接口方法查询数据
        List<Schedule> scheduleList = scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,workDateToDate);
        //3.根据编码查询具体名称用于展示
        scheduleList.stream().forEach(item->{
            this.packageSchedule(item);
        });

        return scheduleList;
    }

    private void packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname",hospitalService.getHospName(schedule.getHoscode()));
        //设置科室名称
        schedule.getParam().put("depname",
                departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
