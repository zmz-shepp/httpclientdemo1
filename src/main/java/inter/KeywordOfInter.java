package inter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import subsidiary.AutoLogger;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordOfInter {
	// httpclientkw对象，方便所有的关键字方法，调用httpclientkw封装好的方法
	public HttpClientKw client;
	// 存储参数所用的map
	public Map<String, String> paramMap;
	// 每次发包之后的返回结果,在下一次调用请求方法之前都不会发生变化，但是会从中进行一些提取。
	public String tmpResponse;

	public KeywordOfInter() {
		client = new HttpClientKw();
		paramMap = new HashMap<String, String>();
	}

	public String testGet(String url, String param) {
		url = toParam(url);
		param = toParam(param);

		tmpResponse = client.doGet(url, param);
		return tmpResponse;
	}

	public String testPost(String url, String param) {
		//首先进行传入参数中如果有已经存储的之前获取到的参数，进行替换
		url = toParam(url);
		param = toParam(param);
		//替换完成之后再发包，注意如果没有要替换的内容，不会有任何影响。
		tmpResponse = client.doPost(url, param);
		AutoLogger.log.info("在keyword中调试输出返回的结果是"+tmpResponse);
		return tmpResponse;
	}
	
	public String testxmlPost(String url, String param) {
		//首先进行传入参数中如果有已经存储的之前获取到的参数，进行替换
		url = toParam(url);
		param = toParam(param);
		//替换完成之后再发包，注意如果没有要替换的内容，不会有任何影响。
		tmpResponse = client.doxmlPost(url, param);
		//正则表达式读取返回xml中的json内容，return这个元素，是wsdl文档中定义，根据返回实际情况编写。
		Pattern returnPattern=Pattern.compile("<return>(.*?)</return>");
		Matcher returnM=returnPattern.matcher(tmpResponse);
		if(returnM.find()) {
			//通过正则表达式，获取return元素中的json内容作为tmpResponse，方便之后进行解析和断言。
			tmpResponse=returnM.group(1);
		}
		AutoLogger.log.info("在keyword中调试输出返回的结果是"+tmpResponse);
		return tmpResponse;
	}
	

	public String testPostJson(String url, String jsonParam) {
		url = toParam(url);
		jsonParam = toParam(jsonParam);
		tmpResponse = client.doPostJson(url, jsonParam);
		AutoLogger.log.info(tmpResponse);
		return tmpResponse;
	}

	
	public void saveCookie() {
		client.saveCookie();
	}

	public void clearCookie() {
		AutoLogger.log.info("清空cookie池");
		client.clearCookie();
	}

	// 传递头域信息，以json格式字符串接收，解析为map格式之后，作为参数传递给httpclientkw的addheader方法使用
	public void addHeader(String headerJson) {
		try {
			// 在解析为map之前，先替换{参数名}为参数值
			headerJson = toParam(headerJson);
			// 创建map
			Map<String, String> headerMap = new HashMap<String, String>();
			// 以json格式的头域列表为基础，创建一个json类型的对象
			JSONObject json = JSON.parseObject(headerJson);
			for (String key : json.keySet()) {
				headerMap.put(key, json.get(key).toString());
			}
			// 转换出的map作为addheader所使用的map，来进行添加头域的操作。
			client.addHeader(headerMap);
		} catch (Exception e) {
			AutoLogger.log.error("头域格式错误，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
		}
	}

	public void clearHeader() {
		client.clearHeader();
	}

	/**
	 * 存储参数到parammap中
	 * 
	 * @param key      存储的参数的键名
	 * @param jsonPath 从json中通过jsonpath解析出来一个值，作为存储的参数的值
	 */
	public void saveParam(String key, String jsonPath) {
		String value;
		try {
			value = JSONPath.read(tmpResponse, jsonPath).toString();
			paramMap.put(key, value);
		} catch (Exception e) {
			AutoLogger.log.error("保存参数失败");
			AutoLogger.log.error(e, e.fillInStackTrace());
		}
	}

	/**
	 * 传入一个字符串，解析其中的键名，替换为parammap中对应的参数值。
	 * 
	 * @param origin 传入的字符串。
	 * @return
	 */
	public String toParam(String origin) {
		String param = origin;
		for (String key : paramMap.keySet()) {
			param = param.replaceAll("\\{" + key + "\\}", paramMap.get(key));
		}
		return param;
	}

	/**
	 * 通过jsonpath表达式解析返回中的内容，基于解析结果断言
	 * 
	 * @param value    预期结果
	 * @param jsonPath jsonpath表达式
	 * @return
	 */
	public boolean assertSame(String jsonPath,String  value) {
		boolean success = false;
		try {
			String actual = JSONPath.read(tmpResponse, jsonPath).toString();
			if (actual != null && actual.equals( value)) {
				AutoLogger.log.info("测试通过！");
				success = true;
				return success;
			} else {
				AutoLogger.log.info("测试失败！");
				success = false;
				return success;
			}
		} catch (Exception e) {
			AutoLogger.log.error("解析失败，请检查jsonPath表达式");
			AutoLogger.log.error(e, e.fillInStackTrace());
		}
		return success;
	}

	public String assertContains(String jsonPath, String value) {
		try {
			String actual = JSONPath.read(tmpResponse, jsonPath).toString();
			if (actual != null && actual.contains(value)) {
				AutoLogger.log.info("测试通过！");
			} else {
				AutoLogger.log.info("测试失败！");
			}
		} catch (Exception e) {
			AutoLogger.log.error("解析失败，请检查jsonPath表达式");
			AutoLogger.log.error(e, e.fillInStackTrace());
		}
		return jsonPath;
	}

	/**
	 * 如果返回不是json格式，直接把返回内容看做一个字符串进行判断，是否包含某些内容。
	 * 
	 * @param expect
	 */
	public void assertResponseContains(String expect) {
		try {

			if (tmpResponse != null && tmpResponse.contains(expect)) {
				AutoLogger.log.info("测试通过！");
			} else {
				AutoLogger.log.info("测试失败！");
			}
		} catch (Exception e) {
			AutoLogger.log.error(e, e.fillInStackTrace());
		}
	}
}
