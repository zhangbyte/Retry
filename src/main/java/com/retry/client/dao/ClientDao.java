package com.retry.client.dao;

import com.retry.client.entity.InvokeMsg;
import org.apache.ibatis.annotations.Delete;
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

    /**
     * 查询远端调用信息
     * @param tablename
     * @return
     */
    @Select("select uuid,interfc,method,args from ${tablename}")
    List<InvokeMsg> selectAll(@Param("tablename") String tablename);

    /**
     * 删除已处理的远端调用
     * @param tablename
     * @param uuid
     * @return
     */
    @Delete("delete from ${tablename} where uuid = #{uuid}")
    int deleteById(@Param("tablename") String tablename, @Param("uuid") String uuid);
}
