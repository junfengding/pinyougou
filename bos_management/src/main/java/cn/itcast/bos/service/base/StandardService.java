package cn.itcast.bos.service.base;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cn.itcast.bos.domain.base.Standard;

/**
 * 收派标准管理 
 * @author itcast
 * 
 */
public interface StandardService {
	public void save(Standard standard);

	public Page<Standard> findPageData(Pageable pageable);

	public List<Standard> finAll();
}
