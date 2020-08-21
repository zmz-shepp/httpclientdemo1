package Writer;

import inter.KeywordOfInter;
import subsidiary.AutoLogger;
import subsidiary.ExcelReader;
import subsidiary.ExcelWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExclWriterTest {

    public static void main(String[] args) {
        KeywordOfInter inter = new KeywordOfInter();

                //打开excel文件，读取workbook对象
                ExcelReader cases = new ExcelReader("E:\\httpclientdemo\\case\\juheLogin.xlsx");

                //添加时间戳
                SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd-HHmm");
                Date now=new Date();
                String nowTime=sdf.format(now);
                ExcelWriter res=new ExcelWriter( "E:\\httpclientdemo\\case\\juheLogin.xlsx","E:\\httpclientdemo\\case\\zhangmuzhuo"+nowTime+".xlsx");




                //遍历sheet页,基于sheet页个数进行遍历
                for (int sheetIndex = 0; sheetIndex < cases.getTotalSheetNo(); sheetIndex++) {
                    //指定使用当前的sheet页
                    cases.useSheetByIndex(sheetIndex);

                    //同时也要指定结果文件切换sheet页
                    res.useSheetByIndex(sheetIndex);


                    //读取当前sheet页中的每一行信息
                    for (int rowIndex = 0; rowIndex < cases.rows; rowIndex++) {
                        //读取每一行,存储到List中
                        List<String> rowContent = cases.readLine(rowIndex);
                        System.out.print(rowContent);
                        if (rowContent.get(3)!=null && rowContent.get(3).length()>0){
                            //基于每个list的第四个元素也就是每一行第四个单元格内容决定执行那个关键字。
                            switch (rowContent.get(2)) {
                                case "get":
                                    String postResp=  inter.testPost(rowContent.get(3), rowContent.get(4));
                                    res.writeCell(rowIndex,9,postResp);
                                    break;
                                default:
                                    AutoLogger.log.info("没有匹配到关键字");
                                    break;
                            }

                            switch (rowContent.get(5)) {
                                case "checkout ":
                                    boolean result = inter.assertSame(rowContent.get(6), rowContent.get(7));

                                    if (result) {

                                        res.writeCell(rowIndex,8,"PASS");
                                        AutoLogger.log.info("外部断言通过");
                                    } else {
                                        res.writeFailCell(rowIndex,8,"FAIL");
                                        AutoLogger.log.info("外部断言失败");
                                    }
                                    break;
                            }
                        }

                    }
                }
                res.save();
            }

    }

