package cn.itcast.bos.web.action.base;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionContext;

import cn.itcast.bos.domain.base.Courier;
import cn.itcast.bos.domain.base.Standard;
import cn.itcast.bos.service.base.CourierService;
import cn.itcast.bos.web.action.common.BaseAction;

@ParentPackage("json-default")
@Namespace("/")
@Controller
@Scope("prototype")
public class CourierAction extends BaseAction<Courier> {

	@Autowired
	private CourierService courierService;

	// 保存和修改操作
	@Action(value = "courier_save", results = {
			@Result(name = "success", type = "redirect", location = "./pages/base/courier.html")})
	public String save() {
		courierService.save(model);
		return SUCCESS;
	}

	@Action(value = "courier_pageQuery", results = { @Result(name = "success", type = "json") })
	public String pageQuery() {

		Specification<Courier> specification = new Specification<Courier>() {

			@Override
			public Predicate toPredicate(Root<Courier> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				List<Predicate> list = new ArrayList<Predicate>();

				// 简单单表查询
				if (StringUtils.isNotBlank(model.getCourierNum())) {
					Predicate p1 = cb.equal(root.get("courierNum").as(String.class), model.getCourierNum());
					list.add(p1);
				}
				if (StringUtils.isNotBlank(model.getCompany())) {
					Predicate p2 = cb.like(root.get("company").as(String.class), "%" + model.getCompany() + "%");
					list.add(p2);
				}
				if (StringUtils.isNotBlank(model.getType())) {
					Predicate p3 = cb.equal(root.get("type").as(String.class), model.getType());
					list.add(p3);
				}
				// 多表查询
				Join<Courier, Standard> standardJoin = root.join("standard", JoinType.INNER);
				if (model.getStandard() != null && StringUtils.isNotBlank(model.getStandard().getName())) {
					Predicate p4 = cb.like(standardJoin.get("name").as(String.class),
							"%" + model.getStandard().getName() + "%");
					list.add(p4);
				}
				return cb.and(list.toArray(new Predicate[0]));
			}
		};
		Pageable pageable = new PageRequest(page - 1, rows);
		// 调用业务层 ，返回 Page
		Page<Courier> pageData = courierService.findPageData(specification, pageable);
		// 将返回page对象 转换datagrid需要格式
		this.pushPageDataToValueStack(pageData);
		return SUCCESS;
	}

	// 属性驱动
	private String ids;

	public void setIds(String ids) {
		this.ids = ids;
	}

	// 批量作废
	@Action(value = "courier_delBatch", results = {
			@Result(name = "success", type = "redirect", location = "./pages/base/courier.html") })
	public String delBatch() {
		String[] idArr = ids.split(",");
		courierService.delBatch(idArr);
		return SUCCESS;
	}

	// 查询所有快递员courier_findnoassociation

	@Action(value = "courier_findnoassociation", results = { @Result(name = "success", type = "json") })
	public String findnoassociation() {

		List<Courier> list = courierService.findnoassociation();
		ActionContext.getContext().getValueStack().push(list);

		return SUCCESS;
	}

}
