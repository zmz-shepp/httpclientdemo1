package subsidiary;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

	/*
	 * 完成对excel文件的读取
	 * 
	 * @method ExcelReader构造函数，读取excel文件内容到workbook中 useSheet读取指定sheet页
	 * close完成文件读取，释放资源 readNextLine读取当前行，并将焦点移动到下一行 readLine读取指定行
	 * getCellValue针对单元格内容不同格式进行读取
	 */
	// xlsx格式的工作簿
	private XSSFWorkbook xssfWorkbook;
	// xls格式的工作簿
	private HSSFWorkbook hssfWorkbook;
	// 工作的sheet页
	private Sheet sheet;
	// 当前sheet页的最大行数
	public int rows = 0;

	// 构造函数，用来打开Excel
	public ExcelReader(String path) {
		// 截取后缀名
		String type = path.substring(path.lastIndexOf("."));
		// 初始化文件流
		FileInputStream in = null;
		try {
			// 通过文件流打开excel文件
			in = new FileInputStream(new File(path));
		} catch (FileNotFoundException e1) {
			// 读取失败则给出Excel读取失败的提示，并停止
			e1.printStackTrace();
			return;
		}
		// 判断是xls还是xlsx格式
		if (type.equals(".xlsx")) {
			try {
				// 如果是xlsx格式，通过文件流，在内存中创建xssfworkbook工作簿
				xssfWorkbook = new  XSSFWorkbook(in);
				// 初始化sheet页
				sheet = xssfWorkbook.getSheetAt(0);
				// 获取最大行数
				rows = sheet.getPhysicalNumberOfRows();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 如果是xls格式，则创建hssf类型工作簿
		if (type.equals(".xls")) {
			try {
				hssfWorkbook = new HSSFWorkbook(in);
				sheet = hssfWorkbook.getSheetAt(0);
				rows = sheet.getPhysicalNumberOfRows();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 关闭文件输入流
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 如果没有sheet，则输出打开Excel失败的提示
		if (sheet == null)
			System.out.println("Excel文件打开失败！");
	}

	//获取当前sheet页的行数
	public int getRows() {
		return this.rows;
	}
	
	// 设置读取的sheet页
	public void useSheet(String sheetName) {
		if (sheet != null) {
			// 根据打开的工作簿类型，在其中初始化sheet页
			if (hssfWorkbook != null)
				sheet = hssfWorkbook.getSheet(sheetName);
			else
				sheet = xssfWorkbook.getSheet(sheetName);
			//更新当前sheet页的行数
			rows = sheet.getPhysicalNumberOfRows();
		} else
			System.out.println("未打开Excel文件！");
	}

	// 获取当前Excel的所有sheet页的个数
	public int getTotalSheetNo() {
		int sheets = 0;
		if (hssfWorkbook != null)
			sheets = hssfWorkbook.getNumberOfSheets();
		else
			sheets = xssfWorkbook.getNumberOfSheets();
		return sheets;
	}

	// 获取当前sheet页面的名字
	public String getSheetName(int sheetIndex) {
		String sheetname = "";
		if (hssfWorkbook != null)
			sheetname = hssfWorkbook.getSheetName(sheetIndex);
		else
			sheetname = xssfWorkbook.getSheetName(sheetIndex);
		return sheetname;
	}

	//根据sheet序号指定使用的sheet
	public void useSheetByIndex(int sheetIndex) {
		if (sheet != null) {
			try {
				if (hssfWorkbook != null)
					sheet = hssfWorkbook.getSheetAt(sheetIndex);
				else
					sheet = xssfWorkbook.getSheetAt(sheetIndex);

				rows = sheet.getPhysicalNumberOfRows();
			} catch (Exception e) {
				System.out.println("error::sheet页面不存在！");
				System.out.println(e.fillInStackTrace());
			}
		} else
			System.out.println("error::未打开Excel文件！");
	}

	/**
	 * 读取excel中指定行的内容为一个list《String》
	 * @param rowNo 需要读取的行。
	 * @return
	 */
	public List<String> readLine(int rowNo) {
		List<String> line = new ArrayList<String>();
		//获取指定的行
		Row row = sheet.getRow(rowNo);
		//获取这一行有多少单元格
		int cellCount = row.getPhysicalNumberOfCells();
		for (int c = 0; c < cellCount; c++) {
			line.add(getCellValue(row.getCell(c)));
		}
		return line;
	}

	// 读取指定列
	public List<String> readColumn(int colNo) {
		List<String> column = new ArrayList<String>();
		for (int i = 0; i < rows; i++) {
			Row row = sheet.getRow(i);
			column.add(getCellValue(row.getCell(colNo)));
		}
		return column;
	}

	// 读取指定单元格
	public String readCell(int rowNo, int column) {
		String content;
		Row row = sheet.getRow(rowNo);
		content = getCellValue(row.getCell(column));
		return content;
	}
	
	//以二维数组形式读取excel文件内容
	public Object[][] readAsMatrix(){
		//获取当前sheet页中第一行的最大单元格数。
		int cellcount=sheet.getRow(0).getPhysicalNumberOfCells();
		//二维数组的下标，由excel的最大行数决定，以及最大列数决定。
		//由于默认第一行是表头行，不需要作为用例信息获取，所以大小是rows-1
		Object[][] matrix=new Object[rows-1][cellcount];
		//用例从excel中的第2行开始读取，遍历到最后一行
		for(int rowNo =1;rowNo<rows;rowNo++) {
			//遍历行中所有的单元格
			for(int colNo=0;colNo<cellcount;colNo++)
			{
				matrix[rowNo-1][colNo]=readCell(rowNo, colNo);
			}
		}
		//完成循环之后，二维数组已经存储好了对应的值，返回该二维数组。
		return matrix;
	}

	// 针对单元格内容不同格式进行读取
	private String getCellValue(Cell cell) {
		String cellValue = "";
		//如果单元格对象为null，则可能是xls文件转xlsx文件格式问题导致读取空单元格时，读到null
		if (cell == null)
			return cellValue;
		//基于不同格式，读取单元格内容并处理。
		try {
			//获取单元格类型。
			CellType cellType = cell.getCellType();
			// 将所有格式转为字符串读取到cellValue
			switch (cellType) {
			case STRING: // 文本
				cellValue = cell.getStringCellValue();
				break;
			case NUMERIC: // 数字、日期
				if (DateUtil.isCellDateFormatted(cell)) {
					//日期型以年-月-日格式存储
					SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
					cellValue = fmt.format(cell.getDateCellValue()); 
				} else {
					//数字保留两位小数
					Double d = cell.getNumericCellValue();
					DecimalFormat df = new DecimalFormat("#.##");
					cellValue = df.format(d);
				}
				break;
			case BOOLEAN: // 布尔型
				cellValue = String.valueOf(cell.getBooleanCellValue());
				break;
			case BLANK: // 空白
				cellValue = cell.getStringCellValue();
				break;
			case ERROR: // 错误
				cellValue = "错误";
				break;
			case FORMULA: // 公式
				FormulaEvaluator eval;
				if (hssfWorkbook != null)
					eval=hssfWorkbook.getCreationHelper().createFormulaEvaluator();
				else
					eval=xssfWorkbook.getCreationHelper().createFormulaEvaluator();
				cellValue = getCellValue(eval.evaluateInCell(cell));
				break;
			case _NONE:
				cellValue="";
			default:
				cellValue = "错误";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cellValue;
	}

	// 读取完成，关闭Excel
	public void close() {
		try {
			if (hssfWorkbook != null)
				hssfWorkbook.close();
			else
				xssfWorkbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
