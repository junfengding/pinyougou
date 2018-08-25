package cn.itcast.bos.service.take_delivery.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.itcast.bos.dao.take_delivery.PromotionRepository;
import cn.itcast.bos.domain.page.PageBean;
import cn.itcast.bos.domain.take_delivery.Promotion;
import cn.itcast.bos.service.take_delivery.PromotionService;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {
	// 注入dao
	@Autowired
	private PromotionRepository promotionRepository;

	// 保存操作
	@Override
	public void save(Promotion promotion) {
		promotionRepository.save(promotion);
	}

	// 查询请求页码的返回参数
	@Override
	public Page<Promotion> findPageData(Pageable pageable) {
		return promotionRepository.findAll(pageable);
	}

	// 根据rows page得到分页数据返回
	@Override
	public PageBean<Promotion> findPageData(int page, int rows) {
		// 创建分页查询条件对象
		Pageable pageable = new PageRequest(page - 1, rows);
		// 根据分页条件查询获得数据
		Page<Promotion> pageData = promotionRepository.findAll(pageable);
		// 创建返回分页数据对象
		PageBean<Promotion> pageBean = new PageBean<Promotion>();
		long totalCount = pageData.getTotalElements();
		List<Promotion> content = pageData.getContent();
		// 封装数据
		pageBean.setTotalCount(totalCount);
		pageBean.setPageData(content);
		// 返回对象
		return pageBean;
	}

	// 根据id查询
	@Override
	public Promotion showDetail(int id) {
		return promotionRepository.findOne(id);
	}

	@Override
	public void updateStatus(Date date) {
		promotionRepository.updateStatus(date);
	}

}
