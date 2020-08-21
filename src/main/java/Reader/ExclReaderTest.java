package Reader;

import inter.KeywordOfInter;
import subsidiary.AutoLogger;
import subsidiary.ExcelReader;

import java.util.List;

public class ExclReaderTest {
    public static KeywordOfInter inter = new KeywordOfInter();

    public static void main(String[] args) {


        ExcelReader cases = new ExcelReader("E:\\httpclientdemo\\case\\juheLogin.xlsx");

        for (int sheetIndex = 0; sheetIndex < cases.getTotalSheetNo(); sheetIndex++) {

            cases.useSheetByIndex(sheetIndex);
            //System.out.print("++++++++++++++++++++++++当前使用的sheet页是"+cases.getSheetName(sheetIndex)+"+++++++++++++++++++++++++++++");
            //读取当前sheet页中的每一行信息
            for (int rowIndex = 0; rowIndex < cases.rows; rowIndex++) {
                //读取每一行,存储到List中
                List<String> rowContent = cases.readLine(rowIndex);
                System.out.print(rowContent);
                //基于每个list的第四个元素也就是每一行第四个单元格内容决定执行那个关键字。

                switch (rowContent.get(2)) {
                    case "get":
                        inter.testGet(rowContent.get(3), rowContent.get(4));
                        break;
                    default:
                        AutoLogger.log.info("没有匹配到关键字");
                        break;
                }

                switch (rowContent.get(5)) {
                    case "checkout ":
                        boolean result = inter.assertSame(rowContent.get(6), rowContent.get(7));
                        if (result) {
                            AutoLogger.log.info("外部断言通过");
                        } else {
                            AutoLogger.log.info("外部断言失败");
                        }
                }
            }
        }
    }
}
