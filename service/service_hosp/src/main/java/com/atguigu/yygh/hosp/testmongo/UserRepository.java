package com.atguigu.yygh.hosp.testmongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User,String> {
    List<User> findByNameLike(String zhang);

    List<User> findByName(String 张三);
}
