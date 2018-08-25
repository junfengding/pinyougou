package cn.itcast.bos.web.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import javax.print.attribute.standard.Media;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument.Config;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;

import com.opensymphony.xwork2.ActionContext;

import cn.itcast.bos.constant.Constants;
import cn.itcast.bos.domain.page.PageBean;
import cn.itcast.bos.domain.take_delivery.Promotion;
import freemarker.template.Configuration;
import freemarker.template.Template;

@ParentPackage("json-default")
@Namespace("/")
@Controller
@Scope("prototype")
public class PromotionAction extends BaseAction<Promotion> {
	/**
	 * 封装页面需要的totalCount,pageData json数据返回
	 */
	@Action(value = "promotion_pageQuery", results = { @Result(name = "success", type = "json") })
	public String pageQuery() {
		PageBean pageBean = WebClient.create(Constants.BOS_MANAGEMENT_URL
				+ "/bos_management/services/promotionService/pageQuery?page=" + page + "&rows=" + rows)
				.accept(MediaType.APPLICATION_JSON).get(PageBean.class);
		ActionContext.getContext().getValueStack().push(pageBean);
		return SUCCESS;
	}

	@Action(value = "promotion_showDetail")
	public String showDetail() throws Exception {
		// 获取请求的id
		String htmlRealPath = ServletActionContext.getServletContext().getRealPath("freemarker");
		File htmlFile = new File(htmlRealPath, "/" + model.getId() + ".html");
		if (!htmlFile.exists()) {
			// 创建Configuration，生成模板实例
			Configuration configuration = new Configuration(Configuration.VERSION_2_3_0);
			configuration.setDirectoryForTemplateLoading(
					new File(ServletActionContext.getServletContext().getRealPath("/WEB-INF/freemarker_templates")));
			// 获取模板
			Template template = configuration.getTemplate("promotion_detail.ftl");
			// 获取动态数据
			Promotion promotion = WebClient.create(Constants.BOS_MANAGEMENT_URL
					+ "/bos_management/services/promotionService/promotion/" + model.getId())
					.accept(MediaType.APPLICATION_JSON).get(Promotion.class);
			// 填充数据
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("promotion", promotion);
			// 合并输出
			template.process(map, new OutputStreamWriter(new FileOutputStream(htmlFile), "utf-8"));
		}
		// 存在 ，直接将文件返回
		ServletActionContext.getResponse().setContentType("text/html;charset=utf-8");// 解决乱码问题
		FileUtils.copyFile(htmlFile, ServletActionContext.getResponse().getOutputStream());
		return NONE;
	}
}
