package com.isesol.mes.ismes.pl.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;

public class FileUtil {
	
	private static Logger log4j = Logger.getLogger(FileUtil.class);
	 /** 
     * @param sourceFilePath : 需要打包的file文件集合
     * @param zipFilePath :压缩后存放路径 
     * @param fileName :压缩后文件的名称 
     * @return 
     */  
	public static Map<String,Object> fileToZip(List<File> fileList,String zipFilePath,String fileName){  
		Map<String,Object> returnMap = new HashMap<String, Object>();
    	if(CollectionUtils.isEmpty(fileList)){
    		return returnMap;
    	}
    	InputStream is_zip = null;
        InputStream fis = null;  
        BufferedInputStream bis = null;  
        FileOutputStream fos = null;  
        ZipOutputStream zos = null;  
        java.io.File zipFile = null;
        try {  
        	zipFile = java.io.File.createTempFile(zipFilePath + "/" + fileName, ".zip");
            fos = new FileOutputStream(zipFile);  
            zos = new ZipOutputStream(new BufferedOutputStream(fos));  
            byte[] bufs = new byte[1024*10];  
            for(File f : fileList){  
                //创建ZIP实体，并添加进压缩包  
                ZipEntry zipEntry = new ZipEntry(f.getName());  
                zos.putNextEntry(zipEntry);  
                //读取待压缩的文件并写进压缩包里  
                fis = f.getInputStream(); 
                bis = new BufferedInputStream(fis, 1024*10);  
                int read = 0;  
                while((read=bis.read(bufs, 0, 1024*10)) != -1){  
                    zos.write(bufs,0,read);  
                }  
            } 
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
            throw new RuntimeException(e);  
        } catch (IOException e) {  
            e.printStackTrace();  
            throw new RuntimeException(e);  
        } finally{  
            //关闭流  
            try {  
                if(null != bis) bis.close();  
                if(null != zos) zos.close();  
                
                if(zipFile != null){
                	is_zip = new FileInputStream(zipFile);
                	Sys.saveFile(zipFilePath + "/" + fileName + ".zip", is_zip);
                	
                	returnMap.put("zip_inputStream", is_zip);
                	returnMap.put("zip_file", zipFile);
                }
                
            } catch (IOException e) {
                e.printStackTrace();  
                throw new RuntimeException(e);  
            }  
        }
        return returnMap;
    }  
}
