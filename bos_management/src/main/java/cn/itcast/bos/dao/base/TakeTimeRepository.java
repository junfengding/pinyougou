package cn.itcast.bos.dao.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import cn.itcast.bos.domain.base.Area;
import cn.itcast.bos.domain.base.TakeTime;

public interface TakeTimeRepository extends JpaRepository<TakeTime, Integer>, JpaSpecificationExecutor<TakeTime> {

}
