package ingress.mig.view;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
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
import org.apache.poi.ss.usermodel.CellStyle;

import ingress.mig.core.Converter;
import ingress.mig.model.AnnotationMapping.TYPE;
import ingress.mig.model.IngressVO;

public class ExcelExporter {
    
    private String filePath = "ing-mig.xls";
    
    
    public ExcelExporter withFilepath(String filepath) {
        this.filePath = filepath;
        return this;
    }
    
    public void export(List<Converter> converters, TYPE... types) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        
        HSSFCellStyle headerStyle = getHeaderStyle(workbook);
        HSSFCellStyle dataStyle = getDataStyle(workbook);
        
        for (Converter converter : converters) {
            createSheet(workbook, converter, headerStyle, dataStyle, types);
        }
        
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
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
    
    private HSSFCellStyle getDataStyle(HSSFWorkbook workbook) {
        HSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        style.setWrapText(true);
        
        HSSFFont font = workbook.createFont();
        font.setFontName("Consolas");
        
        style.setFont(font);
        
        return style;        
    }
    
    private void createSheet(HSSFWorkbook workbook, Converter converter, HSSFCellStyle headerStyle, HSSFCellStyle dataStyle, TYPE... types) {
        HSSFSheet sheet = workbook.createSheet(getSheetName(converter.getName()));
        
        makeHeader(sheet, headerStyle);
        List<IngressVO> ingressList = converter.getIngressList();
        
        int rowNum = 1;
        for (IngressVO ingVo : ingressList) {
            if (!ingVo.hasType(types)) {
                continue;
            }
            
            createDataRow(sheet, dataStyle, converter, ingVo, rowNum++, types);
        }
        
        int lastCellNum = sheet.getRow(0).getLastCellNum();
        for (int i = 0 ; i <= lastCellNum ; i++) {
            sheet.autoSizeColumn(i);
        }
        
    }
    
    private HSSFCellStyle getHeaderStyle(HSSFWorkbook workbook) {
        HSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        
        HSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        
        style.setFont(font);
        
        return style;
    }
    
    private void makeHeader(HSSFSheet sheet, HSSFCellStyle style) {
        HSSFRow headerRow = sheet.createRow(0);
        
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
    
    protected void createDataRow(HSSFSheet sheet, HSSFCellStyle style, Converter converter, IngressVO ingVo, int rowNum, TYPE... types) {
        HSSFRow row = sheet.createRow(rowNum);
        
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
        
    }
    
    private String getSheetName(String clusterName) {
        return StringUtils.substringAfter(clusterName, "cloudzcp-");
    }

}
