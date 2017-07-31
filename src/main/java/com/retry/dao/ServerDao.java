package com.retry.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * Created by zbyte on 17-7-20.
 */
public interface ServerDao {
    /**
     * 插入uuid
     * @param uuid
     * @return
     */
    @Insert("insert ignore into ${tablename} values(#{uuid})")
    int insert(@Param("tablename") String tablename, @Param("uuid") String uuid);
}
