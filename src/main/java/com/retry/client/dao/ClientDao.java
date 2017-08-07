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
    @Insert("insert into ${tablename} (uuid, interfc, method, args, args_str) values(#{uuid}, #{interfc}, #{method}, #{args}, #{args_str})")
    int insert(@Param("tablename") String tablename, @Param("uuid") String uuid,
               @Param("interfc") String interfc, @Param("method") String method, @Param("args") byte[] args, @Param("args_str") String args_str);

    /**
     * 查询远端调用信息
     * @param tablename
     * @return
     */
    @Select("select uuid,interfc,method,args from ${tablename} where state = 0")
    List<InvokeMsg> selectAll(@Param("tablename") String tablename);

    /**
     * 逻辑删除已处理的远端调用
     * @param tablename
     * @param uuid
     * @return
     */
    @Update("update ${tablename} set state = 1 where uuid = #{uuid}")
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
