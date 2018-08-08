package cn.itcast.bos.web.action.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.base.Courier;
import cn.itcast.bos.domain.base.Standard;
import cn.itcast.bos.service.base.CourierService;

@ParentPackage("json-default")
@Namespace("/")
@Controller
@Scope("prototype")
public class CourierAction extends ActionSupport implements ModelDriven<Courier> {

	private Courier courier = new Courier();

	@Override
	public Courier getModel() {
		return courier;
	}

	private Integer page;
	private Integer rows;

	public void setPage(Integer page) {
		this.page = page;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	@Autowired
	private CourierService courierService;

	@Action(value = "courier_save", results = {
			@Result(name = "success", type = "redirect", location = "./pages/base/courier.html") })
	public String save() {

		courierService.save(courier);
		return SUCCESS;
	}

	@Action(value = "courier_pageQuery", results = { @Result(name = "success", type = "json") })
	public String pageQuery() {
		
		Specification<Courier>specification=new Specification<Courier>(){

			@Override
			public Predicate toPredicate(Root<Courier> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
			
				List<Predicate> list = new ArrayList<Predicate>();

				// 简单单表查询
				if (StringUtils.isNotBlank(courier.getCourierNum())) {
					Predicate p1 = cb.equal(
							root.get("courierNum").as(String.class),
							courier.getCourierNum());
					list.add(p1);
				}
				if (StringUtils.isNotBlank(courier.getCompany())) {
					Predicate p2 = cb.like(
							root.get("company").as(String.class),
							"%" + courier.getCompany() + "%");
					list.add(p2);
				}
				if (StringUtils.isNotBlank(courier.getType())) {
					Predicate p3 = cb.equal(root.get("type").as(String.class),
							courier.getType());
					list.add(p3);
				}
				// 多表查询
				Join<Courier, Standard> standardJoin = root.join("standard",
						JoinType.INNER);
				if (courier.getStandard() != null
						&& StringUtils.isNotBlank(courier.getStandard()
								.getName())) {
					Predicate p4 = cb.like(
							standardJoin.get("name").as(String.class), "%"
									+ courier.getStandard().getName() + "%");
					list.add(p4);
				}
				return cb.and(list.toArray(new Predicate[0]));
			}
		};
	/*	Pageable pageable = new PageRequest(page-1, rows);
		Page<Courier> page=courierService.findPageData(pageable);
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("total", page.getTotalElements());
		map.put("rows", page.getContent());
		ActionContext.getContext().getValueStack().push(map);*/
		Pageable pageable = new PageRequest(page-1, rows);
		// 调用业务层 ，返回 Page
		Page<Courier> pageData = courierService.findPageData(specification,
				pageable);
		// 将返回page对象 转换datagrid需要格式
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("total", pageData.getTotalElements());
		result.put("rows", pageData.getContent());
		// 将结果对象 压入值栈顶部
		ActionContext.getContext().getValueStack().push(result);
		return SUCCESS;
	}

}
