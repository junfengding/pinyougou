package cn.itcast.bos.domain.page;

import java.util.List;
/**
 * 自定义分页封装对象
 */
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import cn.itcast.bos.domain.take_delivery.Promotion;

@XmlRootElement
@XmlSeeAlso(Promotion.class)
public class PageBean<T> {
	private Long totalCount;
	private List<T> PageData;

	public Long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}

	public List<T> getPageData() {
		return PageData;
	}

	public void setPageData(List<T> pageData) {
		PageData = pageData;
	}

}
