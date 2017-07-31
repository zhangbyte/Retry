package com.retry.dao;

import com.retry.entity.InvokeMsg;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    @Select("select uuid,interfc,method,args from ${tablename}")
    List<InvokeMsg> selectAll(@Param("tablename") String tablename);
}
