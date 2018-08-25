package cn.itcast.bos.web.action.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
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

import cn.itcast.bos.domain.base.Area;
import cn.itcast.bos.service.base.AreaService;
import cn.itcast.bos.utils.PinYin4jUtils;

@ParentPackage("json-default")
@Namespace("/")
@Controller
@Scope("prototype")

public class AreaAction extends ActionSupport implements ModelDriven<Area> {
	private Area area = new Area();

	@Override
	public Area getModel() {
		return area;
	}

	// 接收上传文件
	private File file;
	// 注入业务层对象
	@Autowired
	private AreaService areaService;

	public void setFile(File file) {
		this.file = file;
	}

	// 接收页面传过来的请求页数和每行显示数量
	private int rows;
	private int page;

	public void setRows(int rows) {
		this.rows = rows;
	}

	public void setPage(int page) {
		this.page = page;
	}

	// 批量区域文件导入
	@Action(value = "area_batchImport")
	public String batchImport() {
		try {
			// 用于存放表格每一行数据
			List<Area> areas = new ArrayList<Area>();
			// 给予xls解析
			// 加载Excel文件
			HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(file));
			// 获取第一页
			HSSFSheet sheet = book.getSheetAt(0);
			// 遍历每一行数据
			for (Row row : sheet) {
				if (row.getRowNum() == 0) {
					continue;
				}
				if (row.getCell(0) == null || StringUtils.isBlank(row.getCell(0).getStringCellValue())) {
					continue;
				}
				// 将表格数据存入对象
				Area area = new Area();
				area.setId(row.getCell(0).getStringCellValue());
				area.setProvince(row.getCell(1).getStringCellValue());
				area.setCity(row.getCell(2).getStringCellValue());
				area.setDistrict(row.getCell(3).getStringCellValue());
				area.setPostcode(row.getCell(4).getStringCellValue());
				// 使用pinyin4j获取城市拼音
				String province = area.getProvince();
				province = province.substring(0, province.length() - 1);
				String city = area.getCity();
				city = city.substring(0, city.length() - 1);
				String district = area.getDistrict();
				district = district.substring(0, district.length() - 1);
				String[] headArr = PinYin4jUtils.getHeadByString(province + city + district);
				StringBuffer buffer = new StringBuffer();
				for (String str : headArr) {
					buffer.append(str);
				}
				area.setShortcode(buffer.toString());
				String citycode = PinYin4jUtils.hanziToPinyin(city, "");
				area.setCitycode(citycode);

				areas.add(area);
			}

			areaService.saveBatch(areas);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return NONE;
	}

	// 分页查询
	@Action(value = "area_pageQuery", results = { @Result(name = "success", type = "json") })
	public String pageQuery() {

		Specification<Area> specification = new Specification<Area>() {
			@Override
			// 构造条件查询对象
			public Predicate toPredicate(Root<Area> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				// 添加省份查询条件
				if (StringUtils.isNotBlank(area.getProvince())) {
					Predicate p1 = cb.like(root.get("province").as(String.class), "%" + area.getProvince() + "%");
					list.add(p1);
				}
				// 添加城市条件
				if (StringUtils.isNotBlank(area.getCity())) {
					Predicate p2 = cb.like(root.get("city").as(String.class), "%" + area.getCity() + "%");
					list.add(p2);
				}

				// 添加区域条件
				if (StringUtils.isNotBlank(area.getDistrict())) {
					Predicate p3 = cb.like(root.get("district").as(String.class), "%" + area.getDistrict() + "%");
					list.add(p3);
				}
				// 返回结合的查询条件
				return cb.and(list.toArray(new Predicate[0]));
			}
		};
		// 构造分页查询对象
		Pageable pageable = new PageRequest(page - 1, rows);
		// 调用业务层完成查询
		Page<Area> pageData = areaService.findPageData(specification, pageable);
		//根据查询结构,封装返回页面的参数
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", pageData.getTotalElements());
		map.put("rows", pageData.getContent());
		//压入值栈返回页面
		ActionContext.getContext().getValueStack().push(map);
		return SUCCESS;
	}

}
