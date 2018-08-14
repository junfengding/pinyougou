package cn.itcast.bos.web.action.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
/**
 * Action的重构
 * @author Administrator
 *
 * @param <T>
 */
public class BaseAction<T> extends ActionSupport implements ModelDriven<T> {
	protected T model;
	protected int rows;
	protected int page;

	public void setRows(int rows) {
		this.rows = rows;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public T getModel() {
		return model;
	}
	/**
	 * 在构造器中实例化model
	 */
	public BaseAction() {
		Type superclass = this.getClass().getGenericSuperclass();
		ParameterizedType type = (ParameterizedType) superclass;
		Type[] types = type.getActualTypeArguments();
		Class<T> clazz = (Class<T>) types[0];
		try {
			model = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			System.out.println("模型构造失败!");
		}

	}

	/**
	 * 将分页得到的值压入值栈
	 */
	protected void pushPageDataToValueStack(Page<T> pageData) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", pageData.getTotalElements());
		map.put("rows", pageData.getContent());
		ActionContext.getContext().getValueStack().push(map);
	}

}
