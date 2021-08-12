package com.atguigu.yygh.hosp.testmongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mongo2")
public class TestMongo2 {
    @Autowired
    private UserRepository userRepository;

    //添加
    @GetMapping("create")
    public void createUser() {
        User user = new User();
        user.setAge(20);
        user.setName("zhangsan");
        user.setEmail("4932200@qq.com");

        User user1 = userRepository.save(user);
        System.out.println(user);
    }

    //查询所有
    @GetMapping("findAll")
    public void findAll() {
        List<User> all = userRepository.findAll();
        System.out.println("all = " + all);
    }

    //根据id查询
    @GetMapping("findId")
    public void findId() {
        User byId = userRepository.findById("610e458c33084d3a48582ccf").get();
        System.out.println("byId = " + byId);
    }

    //模糊查询
    @GetMapping("testLike")
    public void testLike() {
        List<User> users = userRepository.findByNameLike("zhang");
        System.out.println(users);
    }

    @GetMapping("testName")
    public void testName(){
        List<User> users = userRepository.findByName("张三");
        System.out.println(users);
    }
}
