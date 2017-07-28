package com.retry.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * Created by zbyte on 17-7-28.
 */
public interface ClientDao {

    /**
     * 持久化远端调用
     * @param tablename
     * @param uuid
     * @param interfc
     * @param method
     * @param args
     * @return
     */
    @Insert("insert into ${tablename} values(#{uuid}, #{interfc}, #{method}, #{args})")
    int insert(@Param("tablename") String tablename, @Param("uuid") String uuid,
               @Param("interfc") String interfc, @Param("method") String method, @Param("args") byte[] args);
}
