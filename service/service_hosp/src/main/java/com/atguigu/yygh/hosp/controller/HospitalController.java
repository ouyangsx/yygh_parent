package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(description = "医院接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;

    @ApiOperation("带分页、条件查询医院信息列表")
    @GetMapping("getHospPage/{page}/{limit}")
    public R getHospPage(@PathVariable Integer page,@PathVariable Integer limit,HospitalQueryVo hospitalQueryVo) {//mongo的page和limit的类型为int
        Page<Hospital> pages = hospitalService.selectPage(page,limit,hospitalQueryVo);
        return R.ok().data("pages",pages);
    }

    @ApiOperation(value = "更新上线状态")
    @GetMapping("updateStatus/{id}/{status}")
    public R lock(@PathVariable("id") String id, @PathVariable("status") Integer status){
        hospitalService.updateStatus(id,status);
        return R.ok();
    }

    @ApiOperation(value = "获取医院详情")
    @GetMapping("show/{id}")
    public R show(@PathVariable("id") String id){
        Map<String,Object> map = hospitalService.getHospById(id);
        return R.ok().data(map);
    }
}
