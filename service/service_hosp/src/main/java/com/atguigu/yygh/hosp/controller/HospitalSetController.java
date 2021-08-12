package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//医院设置接口
@RestController
@Api(description = "医院设置接口")
@RequestMapping("/admin/hosp/hospitalSet")
//@CrossOrigin
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("登录")
    @PostMapping("login")
    public R login() {
        //{"code":20000,"data":{"token":"admin-token"}}
        return R.ok().data("token","token-admin");
    }

    @ApiOperation("用户信息")
    @GetMapping("info")
    public R info() {
        /**
         * {"code":20000,"data":{"roles":["admin"],"introduction":"I am a super administrator",
         * "avatar":"https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif","name":"Super Admin"}}
         */

        Map<String,Object> map = new HashMap<>();
        map.put("roles","admin");
        map.put("introduction","I am a super administrator");
        map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        map.put("name","Super Admin");
        return R.ok().data(map);
    }


    //查询所有医院设置
    @ApiOperation(value = "查询所有医院设置")
    @GetMapping("/findAll")
    public R findAll() {
        try {
            int a = 10 / 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw new YyghException(20001,"发生了自定义异常");
        }

        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("list",list);
    }

    //删除医院设置
    @ApiOperation(value = "根据id删除医院设置")
    @DeleteMapping("{id}")
    public R delHospSetById(@PathVariable Long id) {
        boolean remove = hospitalSetService.removeById(id);
        return R.ok();
    }

    @ApiOperation(value = "分页查询医院设置")
    @GetMapping("pageList/{page}/{limit}")
    public R pageList(@PathVariable Long page,
                      @PathVariable Long limit) {
        Page<HospitalSet> pageParam = new Page<>(page,limit);
        hospitalSetService.page(pageParam);
        List<HospitalSet> records = pageParam.getRecords();
        long total = pageParam.getTotal();
        return R.ok().data("total",total).data("rows",records);
    }

    @ApiOperation(value = "带条件的分页查询医院设置")
    @PostMapping("pageQuery/{page}/{limit}")
    public R pageQuery(@PathVariable Long page,
                       @PathVariable Long limit,
                       @RequestBody HospitalSetQueryVo hospitalSetQueryVo) {

        //取出参数
        String hoscode = hospitalSetQueryVo.getHoscode();
        String hosname = hospitalSetQueryVo.getHosname();
        //验空，拼写sql
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(hosname)) {
            wrapper.like("hosname",hosname);
        }
        if (!StringUtils.isEmpty(hoscode)) {
            wrapper.eq("hoscode",hoscode);
        }
        //实现带条件分页查询
        Page<HospitalSet> pageParam = new Page<>(page,limit);
        hospitalSetService.page(pageParam,wrapper);

        List<HospitalSet> records = pageParam.getRecords();
        long total = pageParam.getTotal();

        return R.ok().data("total",total).data("rows",records);
    }

    /**
     * 新增医院设置接口
     * @param hospitalSet 封装的医院设置的类
     * @return
     */
    @ApiOperation(value = "新增医院设置")
    @PostMapping("save")
    public R save(@RequestBody HospitalSet hospitalSet) {
        boolean save = hospitalSetService.save(hospitalSet);
        if (save == true) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    /**
     * 实现数据查询回显接口
     * @param id 要查询的id
     * @return
     */
    @ApiOperation(value = "根据id查询医院设置")
    @GetMapping("getHospById/{id}")
    public R getHospById(@PathVariable Long id) {
        HospitalSet byId = hospitalSetService.getById(id);
        return R.ok().data("item",byId);
    }

    /**
     * 修改接口
     * @param hospitalSet
     * @return
     */
    @ApiOperation(value = "根据id修改医院设置")
    @PostMapping("update")
    public R update(@RequestBody HospitalSet hospitalSet) {
        boolean update = hospitalSetService.updateById(hospitalSet);
        if (update == true) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    @ApiOperation(value = "批量删除医院设置")
    @DeleteMapping("batchRemove")
    public R batchRemove(@RequestBody List<Long> ids) {
        boolean remove = hospitalSetService.removeByIds(ids);
        if (remove == true) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    @ApiOperation(value = "医院设置锁定和解锁")
        @PutMapping("lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable Long id,
                             @PathVariable Integer status) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        boolean result = hospitalSetService.updateById(hospitalSet);
        if (result == true) {
            return R.ok();
        } else {
            return R.error();
        }
    }

}
