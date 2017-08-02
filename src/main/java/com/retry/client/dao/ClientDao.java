package com.retry.client.dao;

import com.retry.client.entity.InvokeMsg;
import org.apache.ibatis.annotations.*;

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
    @Insert("insert into ${tablename} (uuid, interfc, method, args) values(#{uuid}, #{interfc}, #{method}, #{args})")
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

    /**
     * 更新处理次数
     * @param tablename
     * @param uuid
     * @return
     */
    @Update("update ${tablename} set times = times + 1 where uuid = #{uuid}")
    int updateTimes(@Param("tablename") String tablename, @Param("uuid") String uuid);
}
