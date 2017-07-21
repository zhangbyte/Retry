package com.retry.dao.impl;

import com.retry.dao.RetryDao;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zbyte on 17-7-20.
 */
@Component
public class RetryDaoImpl implements RetryDao {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public int insert(String tablename, String uuid) {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            return session.getMapper(RetryDao.class).insert(tablename, uuid);
        } finally {
            session.close();
        }
    }
}
