package inter;

import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelResult {
	private ExcelReader excel;

	/*
	 * 统计结果文件中的内容，生成测试报告，输入参数为测试报告文件路径、及测试开始时间。
	 */
	public List<Map<String, String>> Sumarry(String path, String StartTime) {
		try {
			/**
			 * 用于存放测试结果内容的list，每个成员均为一个map
			 * map中包含各个分组信息的执行详情（包括分组信息名、分组中的用例条数、用例执行成功条数、分组执行是否成功）
			 * 最后一个map为测试集的总体情况（包括测试状态、测试通过率、测试开始时间和结束时间）
			 * 最终返回的结果即为这个list，从list中逐个读取内容，写入报告模板中，生成最终测试报告。
			 */
			List<Map<String, String>> detaillist = new ArrayList<Map<String, String>>();
			excel = new ExcelReader(path);
			// 用于记录分组信息名称
			String caseName = "";
			// 用于记录执行结果，逻辑为，只要分组中出现一个失败用例，则认为该分组执行失败，与flag联合使用。
			String status = "Fail";
			boolean flag = true;
			// 统计测试用例集的用例总条数
			int totalcount = 0;
			// 统计所有用例中通过用例的条数数
			int totalpass = 0;
			// 统计当前分组下的用例总数
			int detailcount = 0;
			// 统计当前分组下用例通过的条数
			int detailpass = 0;
			// 统计通过率
			float passrate = 100f;
			// 当开始进行统计时，用例执行已经完成，即为用例执行结束时间。
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String endtime = df.format(new Date());

			// 创建一个map，分组信息和最终结果均使用该map存放
			Map<String, String> map = new HashMap<String, String>();
			// 循环遍历每一个sheet页
			for (int sheetNo = 0; sheetNo < excel.getTotalSheetNo(); sheetNo++) {
				excel.useSheetByIndex(sheetNo);
				// 在每个sheet开始统计时，重置所有的统计数据。
				detailpass = 0;
				status = "Fail";
				detailcount = 0;
				flag = true;
				map = new HashMap<String, String>();
				/*
				 * 从第二行开始循环遍历每一行，如果找到分组信息为空的，则视为用例。
				 * 针对是用例的行，统计用例数和通过的用例数（通过自加的方式），如果不为空，则判断是否是分组信息行，设置名称，结束统计。
				 */
				for (int row = 1; row < excel.rows; row++) {
					// 读取结果文件中每一行的内容
					List<String> line = excel.readLine(row);
					try {
						// 查找记录了分组信息的行
						// 如果第一列（分组信息）和第二列（类别或用例名）不同时为空,则不是用例，执行非用例的操作
						if (!((line.get(0) == null || line.get(0).length() == 0 || line.get(0).trim().equals(""))
								)) {
							System.out.println(line + "非用例");
							// 如果第一列（分组信息）不为空则该行为分组信息行
							if (!(line.get(0) == null || line.get(0).length() < 2)) {
								System.out.println(line + "分组信息");
								// 由于第一个分组信息行是第2行，如果行数大于3，则表示读取到了第二个分组信息行，之前的用例统计结束
								if (row > 2) {
									// row>2判断开始
									// 如果flg依然为真，则状态为通过
									if (flag) {
										status = "Pass";
									}
									// 将分组信息放入map
									map.put("name", caseName);
									// 将执行状态放入map
									map.put("status", status);
									// 将该分组中的用例条数放入map
									map.put("count", detailcount + "");
									// 将该分组中的通过条数放入map
									map.put("pass", detailpass + "");
									// 上面四步，将当前分组执行情况全部存放到了map中
									// 将这个map放入最终结果的list中
									detaillist.add(map);
									// 重置所有分组信息统计项的内容，以对之后的分组进行统计
									detailpass = 0;
									detailcount = 0;
									status = "Fail";
									flag = true;
									// 清空map，这时上一个map在list当中，仅清空临时变量map
									map = new HashMap<String, String>();
								} // row>2判断结束
								/*
								 * 这里分两种情况，如果找到的是第一个分组信息，则跳过了上面的row>2中的内容,将第一个分组信息的名称赋值给name
								 * 如果找到的是row>2即第二个或者之后的分组信息，则执行了上面的row>2中的内容之后，将第二个或者之后的分组信息名称赋值给name
								 */
								caseName = excel.getSheetName(sheetNo) + "->" + line.get(0);
							}
							// 如果第一列为空，则该行为类别或者用例名行，不做任何操作（这里可以根据各位自己的需求定制）
							else {
								;
							}
						} // 非用例行判断结束

						// 第一列信息和第二列信息均为空的行，即用例行，这时开始进行用例数、通过数、状态的统计。
						else {
							// 判断执行结果列，如果为空，将flag置为false,视为该行有误，
							// 不纳入用例数量计算，最终结果会出现用例统计数和excel中的用例行数不一致，看到这个情况
							// 就知道是用例编写有误或者关键字有误，而不是测试对象的问题。
							System.out.println(line+"用例");
							if (line.get(10) == null || line.get(10).length() < 2) {

								flag = false;
							}
							// 执行结果不为空，则将用例统计数自增
							else {
								System.out.println(line.get(9));
								totalcount++;
								detailcount++;
//							System.out.println(line);
								// 如果通过，则通过数和总通过数均自增
								if (line.get(10).equals("PASS")) {
									totalpass++;
									detailpass++;
								} else {
									// 出现了用例执行结果不是PASS的情况，则视为当前分组执行失败。
									flag = false;
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} // sheet页内部所有行的for循环结束

				if (flag) {
					status = "Pass";
				}
				map.put("name", caseName);
				map.put("status", status);
				map.put("count", detailcount + "");
				map.put("pass", detailpass + "");
				detaillist.add(map);

			} // 所有sheet页循环结束
				// 由于最后一个分组执行完后，没有下一个分组信息行，所以最后一步需要统计最后一个分组信息行的执行情况
			if (totalcount > 0) {
				map = new HashMap<String, String>();
				// 计算执行通过率，先计算出pass数和总用例数相除的浮点数结果，然后乘以10000变成一个四位数，再除以100，
				// 得到精确到小数点后两位的结果。
				passrate = ((int) (((float) totalpass / totalcount) * 10000)) / 100f;
				// 用例总数
				map.put("casecount", totalcount + "");
				// 通过率
				map.put("passrate", passrate + "");
				// 执行开始时间
				map.put("starttime", StartTime);
				// 执行结束的时间
				map.put("endtime", endtime);
			} // 判断用例数是否大于0的if结束
				// 将整体情况的字典加到list中，list的最后一个是整体的情况，前面的都是业务模块的情况。
			detaillist.add(map);
			// 返回list
			return detaillist;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
