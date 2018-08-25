package cn.itcast.bos.dao.take_delivery;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import cn.itcast.bos.domain.take_delivery.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
	@Query("update Promotion set status='2' where status='1' and endDate<?")
	@Modifying
	void updateStatus(Date date);

}
