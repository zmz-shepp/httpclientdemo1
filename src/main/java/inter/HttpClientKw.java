package inter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import subsidiary.AutoLogger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HttpClientKw {

	// 是否使用cookie标志位，默认使用cookie
	private boolean useCookie = true;
	// cookieStore对象，httpclient用它来记录得到的cookie值
	public CookieStore cookieStore = new BasicCookieStore();
	// 成员变量headersMap，用于存放需要加载的头域参数。
	private Map<String, String> headersMap = new HashMap<String, String>();
	// 是否添加header，默认不添加
	private boolean addHeaderFlag = false;
	// 匹配unicode编码格式的正则表达式。
	private static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

	/**
	 * 查找字符串中的unicode编码并转换为中文。
	 *
	 * @param u
	 * @return
	 */
	public static String DeCode(String u) {
		try {
			Matcher m = reUnicode.matcher(u);
			StringBuffer sb = new StringBuffer(u.length());
			while (m.find()) {
				m.appendReplacement(sb, Character.toString((char) Integer.parseInt(m.group(1), 16)));
			}
			m.appendTail(sb);
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return u;
		}
	}

	/**
	 * SSLcontext用于绕过ssl验证，使发包的方法能够对https的接口进行请求。
	 */
	private static SSLContext createIgnoreVerifySSL() {

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
										   String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
										   String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSLv3");
			sc.init(null, new TrustManager[] { trustManager }, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sc;
	}

	/**
	 * 用于完成httpclient的创建
	 *
	 * @return 返回创建好的httpclient对象用于发包。
	 */
	private CloseableHttpClient createClient() {
		// 采用绕过验证的方式处理https请求
		SSLContext sslcontext = createIgnoreVerifySSL();
		// 设置协议http和https对应的处理socket链接工厂的对象
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		// 创建自定义的httpclient对象
		CloseableHttpClient client;
		// 基于useCookie标志位进行判断，如果为真则创建client的时候，使用cookieStore
		if (useCookie) {
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookieStore).build();
		} else {
			// 如果为假创建clients的时候，不使用cookieStore
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}
		// 当需要进行代理抓包时，启用下面的代码。
		// 设置代理地址，适用于需要用fiddler抓包时使用，不用时切记注释掉这句！
//		HttpHost proxy = new HttpHost("localhost", 8888, "http");
//		if (useCookie) {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager)
//					.setDefaultCookieStore(cookieStore).build();
//		} else {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).build();
//		}
		return client;
	}

	/**
	 * 通过httpclient实现get方法
	 *
	 * @param url   接口的url地址
	 * @param param 接口的参数列表。
	 */
	public String doGet(String url, String param) {
		// 创建httpclient对象
		CloseableHttpClient client = createClient();
		// result为最终返回结果
		String result = "";
		try {
			// 拼接url和param，形成最终请求的url格式。
			String urlWithParam = "";
			if (param.length() > 0) {
				urlWithParam = url + "?" + param;
			} else {
				urlWithParam = url;
			}
			// 创建get方式请求对象
			HttpGet get = new HttpGet(urlWithParam);
			// 设置连接的超时时间
			// setsocketTImeout指定收发包过程中的超时上线是15秒，connectTime指定和服务器建立连接，还没有发包时的超时上限为10秒。
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			get.setConfig(config);

			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = headersMap.keySet();
				for (String key : headerKeys) {
					get.setHeader(key, headersMap.get(key));
				}
			}
			// httpclient执行httpget方法。
			CloseableHttpResponse response = client.execute(get);
			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换返回实体为String类型
				result = EntityUtils.toString(entity, "UTF-8");
			}
			// 对结果中可能出现的unicode编码进行转换，转成中文
			result = DeCode(result);
			// 释放返回实体
			EntityUtils.consume(entity);
			// 关闭返回包
			response.close();
		} catch (Exception e) {
			// 当出现报错时，将报错信息记录作为结果。
			AutoLogger.log.error(e, e.fillInStackTrace());
			result = e.fillInStackTrace().toString();
		} finally {
			// 关闭httpclient对象，释放资源
			try {
				client.close();
			} catch (IOException e) {
				AutoLogger.log.error(e, e.fillInStackTrace());
			}
		}
		return result;
	}

	/**
	 * 通过httpclient实现的以x-www-form-urlencoded格式传参的post方法
	 *  @param url   接口的url地址
	 * @param param 接口的参数列表。
	 * @param token
	 * @param a
	 */
	public String doPost(String url, String param, String token, String a) {
		// 创建httpclient对象
		CloseableHttpClient client = createClient();
		// result为最终返回结果
		String result = "";
		try {
			// 创建post方式请求对象
			HttpPost post = new HttpPost(url);
			// 设置连接的超时时间
			// setsocketTImeout指定收发包过程中的超时上线是15秒，connectTime指定和服务器建立连接，还没有发包时的超时上限为10秒。
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			post.setConfig(config);
			// 创建urlencoded格式的请求实体，设置编码为utf8
			StringEntity postParams = new StringEntity(param);
			postParams.setContentType("application/x-www-form-urlencoded");
			postParams.setContentEncoding("UTF-8");
			// 添加请求体到post请求中
			post.setEntity(postParams);
			post.setHeader(token,a);

			// 判断头域添加标志为是否为真，为真则添加头域，否则不添加
			if (addHeaderFlag = true) {
				// 遍历存储头域的map，将每个键值对都调用一次setHeader方法，完成头域添加
				for (String key : headersMap.keySet()) {
					post.setHeader(key, headersMap.get(key));
				}
			}
			// 执行请求操作，并获取返回包
			CloseableHttpResponse response = client.execute(post);
			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换返回实体为String类型
				result = EntityUtils.toString(entity, "UTF-8");
			}
			// 对结果中可能出现的unicode编码进行转换，转成中文
			result = DeCode(result);
			// 释放返回实体
			EntityUtils.consume(entity);
			// 关闭返回包
			response.close();
		} catch (Exception e) {
			// 当出现报错时，将报错信息记录作为结果。
			AutoLogger.log.error(e, e.fillInStackTrace());
			result = e.fillInStackTrace().toString();
		} finally {
			// 关闭httpclient对象，释放资源
			try {
				client.close();
			} catch (IOException e) {
				AutoLogger.log.error(e, e.fillInStackTrace());
			}
		}
		return result;
	}


	/**
	 * 通过httpclient实现的以x-www-form-urlencoded格式传参的post方法
	 *
	 * @param url   接口的url地址
	 * @param param 接口的参数列表。
	 */
	public String doPost(String url, String param) {
		// 创建httpclient对象
		CloseableHttpClient client = createClient();
		// result为最终返回结果
		String result = "";
		try {
			// 创建post方式请求对象
			HttpPost post = new HttpPost(url);
			// 设置连接的超时时间
			// setsocketTImeout指定收发包过程中的超时上线是15秒，connectTime指定和服务器建立连接，还没有发包时的超时上限为10秒。
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			post.setConfig(config);
			// 创建urlencoded格式的请求实体，设置编码为utf8
			StringEntity postParams = new StringEntity(param);
			postParams.setContentType("application/x-www-form-urlencoded");
			postParams.setContentEncoding("UTF-8");
			// 添加请求体到post请求中
			post.setEntity(postParams);
			// 判断头域添加标志为是否为真，为真则添加头域，否则不添加
			if (addHeaderFlag = true) {
				// 遍历存储头域的map，将每个键值对都调用一次setHeader方法，完成头域添加
				for (String key : headersMap.keySet()) {
					post.setHeader(key, headersMap.get(key));
				}
			}
			// 执行请求操作，并获取返回包
			CloseableHttpResponse response = client.execute(post);
			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换返回实体为String类型
				result = EntityUtils.toString(entity, "UTF-8");
			}
			// 对结果中可能出现的unicode编码进行转换，转成中文
			result = DeCode(result);
			// 释放返回实体
			EntityUtils.consume(entity);
			// 关闭返回包
			response.close();
		} catch (Exception e) {
			// 当出现报错时，将报错信息记录作为结果。
			AutoLogger.log.error(e, e.fillInStackTrace());
			result = e.fillInStackTrace().toString();
		} finally {
			// 关闭httpclient对象，释放资源
			try {
				client.close();
			} catch (IOException e) {
				AutoLogger.log.error(e, e.fillInStackTrace());
			}
		}
		return result;
	}


	public String doxmlPost(String url, String param) {
		// 创建httpclient对象
		CloseableHttpClient client = createClient();
		// result为最终返回结果
		String result = "";
		try {
			// 创建post方式请求对象
			HttpPost post = new HttpPost(url);
			// 设置连接的超时时间
			// setsocketTImeout指定收发包过程中的超时上线是15秒，connectTime指定和服务器建立连接，还没有发包时的超时上限为10秒。
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			post.setConfig(config);
			// 创建urlencoded格式的请求实体，设置编码为utf8
			StringEntity postParams = new StringEntity(param);
			postParams.setContentType("text/xml");
			postParams.setContentEncoding("UTF-8");
			// 添加请求体到post请求中
			post.setEntity(postParams);
			// 判断头域添加标志为是否为真，为真则添加头域，否则不添加
			if (addHeaderFlag = true) {
				// 遍历存储头域的map，将每个键值对都调用一次setHeader方法，完成头域添加
				for (String key : headersMap.keySet()) {
					post.setHeader(key, headersMap.get(key));
				}
			}
			// 执行请求操作，并获取返回包
			CloseableHttpResponse response = client.execute(post);
			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换返回实体为String类型
				result = EntityUtils.toString(entity, "UTF-8");
			}
			// 对结果中可能出现的unicode编码进行转换，转成中文
			result = DeCode(result);
			// 释放返回实体
			EntityUtils.consume(entity);
			// 关闭返回包
			response.close();
		} catch (Exception e) {
			// 当出现报错时，将报错信息记录作为结果。
			AutoLogger.log.error(e, e.fillInStackTrace());
			result = e.fillInStackTrace().toString();
		} finally {
			// 关闭httpclient对象，释放资源
			try {
				client.close();
			} catch (IOException e) {
				AutoLogger.log.error(e, e.fillInStackTrace());
			}
		}
		return result;
	}








	public String doPostJson(String url, String param) {
		// 创建httpclient对象
		CloseableHttpClient client = createClient();
		// result为最终返回结果
		String result = "";
		try {
			// 创建post方式请求对象
			HttpPost post = new HttpPost(url);
			// 设置连接的超时时间
			// setsocketTImeout指定收发包过程中的超时上线是15秒，connectTime指定和服务器建立连接，还没有发包时的超时上限为10秒。
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			post.setConfig(config);
			// 创建urlencoded格式的请求实体，设置编码为utf8
			StringEntity postParams = new StringEntity(param);
			postParams.setContentType("application/json");
			postParams.setContentEncoding("UTF-8");
			// 添加请求体到post请求中
			post.setEntity(postParams);
			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = headersMap.keySet();
				for (String key : headerKeys) {
					post.setHeader(key, headersMap.get(key));
				}
			}
			// 执行请求操作，并获取返回包
			CloseableHttpResponse response = client.execute(post);
			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换返回实体为String类型
				result = EntityUtils.toString(entity, "UTF-8");
			}
			// 对结果中可能出现的unicode编码进行转换，转成中文
			result = DeCode(result);
			// 释放返回实体
			EntityUtils.consume(entity);
			// 关闭返回包
			response.close();
		} catch (Exception e) {
			// 当出现报错时，将报错信息记录作为结果。
			AutoLogger.log.error(e, e.fillInStackTrace());
			result = e.fillInStackTrace().toString();
		} finally {
			// 关闭httpclient对象，释放资源
			try {
				client.close();
			} catch (IOException e) {
				AutoLogger.log.error(e, e.fillInStackTrace());
			}
		}
		return result;
	}



	/**
	 * 文件上传的方法，使用json来存储fileParam和TextParam的键值对
	 * 由于textParam非必须，所以用可变参数。
	 * @param url
	 * @param fileParam json格式的键值对内容，在方法中完成解析
	 * @param textParam 可变参数，可以填0到n个参数。处理的时候，textParam是一个数组。
	 * 比如调用doUpload的时候传：doUpload("http://abc.com", "filename is this") 表示没有textParam参数，textParam数组长度为0
	 * 比如调用doUpload的时候传：doUpload("http://abc.com", "filename is this","textname is roy")textParam数组长度为1，里面的元素是["textname is roy"]
	 * 比如调用doUpload的时候传：doUpload("http://abc.com", "filename is this","textname is roy","textname2 is will") textParam数组长度为2，里面的元素是["textname is roy","textname2 is will"]
	 * 使用参数textParam的时候，用数组来进行调用。
	 * @return
	 */
	public String doUpload(String url, String fileParam, String... textParam) {
		// 创建httpclient对象
		CloseableHttpClient client = createClient();
		// result为最终返回结果
		String result = "";
		try {
			// 创建post方式请求对象
			HttpPost post = new HttpPost(url);
			// 设置连接的超时时间
			// setsocketTImeout指定收发包过程中的超时上线是15秒，connectTime指定和服务器建立连接，还没有发包时的超时上限为10秒。
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			post.setConfig(config);
			// 创建multipartentitybuild用于完成multipart参数添加
			MultipartEntityBuilder mteb = MultipartEntityBuilder.create();
			// 由于textParam是可变参数，判断是否传入了textParam
			if (textParam.length > 0) {
				// 解析textParam为json对象格式
				JSONObject textJson = JSON.parseObject(textParam[0]);
				// 遍历json对象中的键值对，作为textbody参数添加
				for (String key : textJson.keySet()) {
					mteb.addTextBody(key, textJson.get(key).toString());
				}
			}
			// 解析fileParam为json对象格式
			JSONObject fileJson = JSON.parseObject(fileParam);
			// 遍历filejson对象中的键值对，作为binarybody添加
			for (String key : fileJson.keySet()) {
				mteb.addBinaryBody(key, new File(fileJson.get(key).toString()));
			}
			// 将创建好的multipartentity作为请求体发送
			post.setEntity(mteb.build());
			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = headersMap.keySet();
				for (String key : headerKeys) {
					post.setHeader(key, headersMap.get(key));
				}
			}
			// 执行请求操作，并获取返回包
			CloseableHttpResponse response = client.execute(post);
			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换返回实体为String类型
				result = EntityUtils.toString(entity, "UTF-8");
			}
			// 对结果中可能出现的unicode编码进行转换，转成中文
			result = DeCode(result);
			// 释放返回实体
			EntityUtils.consume(entity);
			// 关闭返回包
			response.close();
		} catch (Exception e) {
			// 当出现报错时，将报错信息记录作为结果。
			AutoLogger.log.error(e, e.fillInStackTrace());
			result = e.fillInStackTrace().toString();
		} finally {
			// 关闭httpclient对象，释放资源
			try {
				client.close();
			} catch (IOException e) {
				AutoLogger.log.error(e, e.fillInStackTrace());
			}
		}
		return result;
	}

	/**
	 * 通过saveCookie来使用成员变量的cookieStore 通过一个成员变量标志位useCookie来进行判断
	 */
	public void saveCookie() {
		// 设置useCookie的状态为真
		useCookie = true;
	}

	/**
	 * 清理掉成员变量cookieStore池中所有的cookie
	 * 并且通过指定成员变量标志位useCookie为false，不让创建client调用已有的cookieStore
	 */
	public void clearCookie() {
		// 设置不使用已有的cookie
		useCookie = false;
		// 重新完成实例化，将cookieStore清空。
		cookieStore = new BasicCookieStore();

	}

	/**
	 * 设置添加头域标志位为true，并且通过传递头域map，实例化成员变量headers
	 *
	 * @param
	 */
	public void addHeader(Map<String, String> headerMap) {
		headersMap = headerMap;
		addHeaderFlag = true;
	}

	/**
	 * 设置添加头域标志位为false，并重置成员变量headers
	 */
	public void clearHeader() {
		addHeaderFlag = false;
		headersMap = new HashMap<String, String>();
	}






}
