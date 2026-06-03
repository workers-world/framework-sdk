package com.books.framework.utils;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel工具类
 *
 * @author zero
 * @description 提供Excel文件读取和处理的常用操作方法
 * @date 2024-11-28
 */
@Slf4j
public class ExcelUtils {

    /**
     * 读取Excel xlsx 文件
     *
     * @param path 文件路径
     * @return Workbook对象
     * @throws IOException IO异常
     */
    public static Workbook readExcel(String path) throws IOException {
        int dotIndex = path.lastIndexOf(".");
        String extension = path.substring(dotIndex + 1);
        if (!"xlsx".equalsIgnoreCase(extension)) {
            throw new AppException(ErrorCodeEnum.FILE_FORMAT_ERROR, "只支持 xlsx 格式");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new AppException(ErrorCodeEnum.FILE_NOT_FOUND, "文件不存在：" + path);
        }
        return readExcel(new FileInputStream(path));
    }

    /**
     * 读取Excel xlsx 文件
     *
     * @param inputStream 输入流
     * @return Workbook对象
     * @throws IOException IO异常
     */
    public static Workbook readExcel(InputStream inputStream) throws IOException {
        return new XSSFWorkbook(inputStream);
    }

    /**
     * 解析Excel文件，获取所有工作表的数据
     *
     * @param workbook Workbook对象
     * @return Map<sheetIndex, List<Row>> 工作表数据
     */
    public static Map<String, List<Row>> parseExcel(Workbook workbook) {
        if (workbook == null) {
            log.error("Workbook对象为空");
            return null;
        }

        Map<String, List<Row>> sheetMap = new HashMap<>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            List<Row> rowList = new ArrayList<>();

            for (int j = 0; j <= sheet.getLastRowNum(); j++) {
                Row row = sheet.getRow(j);
                if (row != null) {
                    rowList.add(row);
                }
            }

            sheetMap.put(String.valueOf(i), rowList);
        }

        return sheetMap;
    }

    /**
     * 将单元格值转换为字符串
     *
     * @param cell 单元格
     * @return 字符串值
     */
    public static String convertCellValueToString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // 处理数字类型，避免科学计数法
                DecimalFormat df = new DecimalFormat("0");
                return df.format(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }

    /**
     * 获取行数据为字符串列表
     *
     * @param row 行对象
     * @return 字符串列表
     */
    public static List<String> getRowValues(Row row) {
        List<String> values = new ArrayList<>();
        if (row == null) {
            return values;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String cellValue = convertCellValueToString(cell);
            values.add(cellValue);
        }

        return values;
    }

    /**
     * 验证Excel文件格式
     *
     * @param path 文件路径
     * @return 是否有效
     */
    public static boolean isValidExcelFile(String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }

        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return false;
        }

        String extension = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
        return "xlsx".equals(extension) || "xls".equals(extension);
    }
}
