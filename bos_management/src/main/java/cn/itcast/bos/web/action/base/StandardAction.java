package cn.itcast.bos.web.action.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

import cn.itcast.bos.domain.base.Standard;
import cn.itcast.bos.service.base.StandardService;

@ParentPackage("json-default")
@Namespace("/")
@Controller
@Scope("prototype")
public class StandardAction extends ActionSupport implements ModelDriven<Standard> {

	// 模型驱动
	private Standard standard = new Standard();

	@Override
	public Standard getModel() {
		return standard;
	}

	// Page属性驱动
	private Integer page;
	private Integer rows;

	public void setPage(Integer page) {
		this.page = page;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	// 注入Service对象
	@Autowired
	private StandardService standardService;

	// 添加操作
	@Action(value = "standard_save", results = {
			@Result(name = "success", type = "redirect", location = "./pages/base/standard.html") })
	public String save() {
		System.out.println("添加收派标准....");
		standardService.save(standard);
		return SUCCESS;
	}

	@Action(value = "standard_pageQuery", results = { @Result(name = "success", type = "json") })
	public String pageQuery() {
		Pageable pageable = new PageRequest(page - 1, rows);
		Page<Standard> page = standardService.findPageData(pageable);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", page.getTotalElements());
		map.put("rows", page.getContent());
		// 将map压入栈顶,自动转换为json对象 需配合json插件
		ActionContext.getContext().getValueStack().push(map);

		return SUCCESS;
	}

	@Action(value = "standard_findAll", results = { @Result(name = "success", type = "json") })
	public String findAll() {
		List<Standard> list = standardService.finAll();
		ActionContext.getContext().getValueStack().push(list);
		return SUCCESS;
	}

}
