package ingress.mig;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import ingress.mig.model.AnnotationMapping.TYPE;
import ingress.mig.model.IngressVO;

public class ExcelExporter {

    public void export(List<Converter> converters, TYPE... types) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        
        HSSFFont summaryFont = createSummaryFont(workbook);
        
        for (Converter converter : converters) {
            createSheet(workbook, converter, summaryFont, types);
        }
        
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream("ing-mig.xls");
            bos = new BufferedOutputStream(fos);
            workbook.write(bos);
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) { 
                try {
                    bos.close();
                } catch (IOException e) {
                } 
            }
        }
    }
    
    private void createSheet(HSSFWorkbook workbook, Converter converter, HSSFFont summaryFont, TYPE... types) {
        HSSFSheet sheet = workbook.createSheet(getSheetName(converter.getName()));
        makeHeader(sheet, summaryFont);
        List<IngressVO> ingressList = converter.getIngressList();
        
        int rowNum = 1;
        for (IngressVO ingVo : ingressList) {
            if (!ingVo.hasType(types)) {
                continue;
            }
            
            createDataRow(sheet, converter, ingVo, rowNum, types);
        }
        
        int lastCellNum = sheet.getRow(0).getLastCellNum();
        for (int i = 0 ; i <= lastCellNum ; i++) {
            sheet.autoSizeColumn(i);
        }
        
    }
    
    private void makeHeader(HSSFSheet sheet, HSSFFont summaryFont) {
        HSSFRow headerRow = sheet.createRow(0);
        
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setFont(summaryFont);
        
        HSSFCell name = headerRow.createCell(0);
        name.setCellStyle(style);
        name.setCellValue("Namespace / Name");
        
        HSSFCell before = headerRow.createCell(1);
        before.setCellStyle(style);
        before.setCellValue("Before");
        
        HSSFCell after = headerRow.createCell(2);
        after.setCellStyle(style);
        after.setCellValue("After");
        
        HSSFCell annos = headerRow.createCell(3);
        annos.setCellStyle(style);
        annos.setCellValue("Annotations");
        
    }
    
    protected void createDataRow(HSSFSheet sheet, Converter converter, IngressVO ingVo, int rowNum, TYPE... types) {
        HSSFRow row = sheet.createRow(rowNum);
        
        HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        
        HSSFCell nameCell = row.createCell(0);
        nameCell.setCellStyle(style);
        nameCell.setCellValue(String.format("%s / %s", ingVo.getNamespace(), ingVo.getName()));
        
        HSSFCell beforeCell = row.createCell(1);
        beforeCell.setCellStyle(style);
        beforeCell.setCellValue(converter.getOriginalYaml(ingVo));
        
        HSSFCell afterCell = row.createCell(2);
        afterCell.setCellStyle(style);
        afterCell.setCellValue(converter.getConvertedYaml(ingVo));
        
        HSSFCell annoCell = row.createCell(3);
        annoCell.setCellStyle(style);
        annoCell.setCellValue(ingVo.logString(types));
        
//        HSSFCell aveCell = row.createCell(1);
//        aveCell.setCellStyle(style);
//        aveCell.setCellFormula(String.format("AVERAGE(C%d:Z%d)", rowNum + 1, rowNum + 1));
//        
//        List<TimeValue> values = pod.getValues();
//        TimeValue firstData = values.get(0);
//        int index = attachPreEmptyData(row, firstData);
//        
//        index++;
//        
//        for (TimeValue tv : values) {
//            HSSFCell cell = row.createCell(++index);
//            cell.setCellValue(tv.getValue());
//        }
    }
    
    private String getSheetName(String clusterName) {
        return StringUtils.substringAfter(clusterName, "cloudzcp-");
    }

    private HSSFFont createSummaryFont(HSSFWorkbook workbook) {
        HSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        
        return font;
    }
}
