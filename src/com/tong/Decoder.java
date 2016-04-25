package com.tong;

import java.io.*;

/**
 * Created by Martin on 2016/4/25.
 */
public class Decoder {

    private static String MPath;
    private static String MPrefix = "";
    private static StringBuilder builder;
    private static String MName;

    /**
     * 解析aco文件步骤：
     * 1.先读取前2个word字段；分别是版本号和颜色数
     * 2.直接跳到颜色数*5个字段位置，判断是否还有字节；
     * 3.如果有则直接从后面的字段开始读取；否则从开头读起；
     * 4.后面的字段读取分别是（版本号2，颜色数，【0字段，4个基本颜色值，0字段，名字长度length，length个字段】）
     * 5.开头字段读取分别是（版本号1，颜色数，【0字段，4个基本颜色值】）
     */

    public static void readFile(String path,String name){
        MPath = path;
        MName = name;
    }

    public static void decode(String prefix){
        if(prefix!=null&&prefix.length()!=0){
            MPrefix = prefix;
        }
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(MPath+File.separator+MName))){
            ReadAcoFile raf = new ReadAcoFile(bis);
            int version = raf.readWord();
            //System.out.println("版本号:"+version);

            int count = raf.readWord();
            //System.out.println("颜色数:"+count);

            bis.skip(count*5*2);

            builder = new StringBuilder();

            version =  raf.readWord();
            builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
            builder.append("<!-- ");
            builder.append("版本号:"+version+" ");

            count =  raf.readWord();
            builder.append("颜色数:"+count+" ");
            builder.append("-->\n");

            int i = 0;
            builder.append("<resources>\n");
            while(i < count){
                builder.append("<color ");
                while(raf.readWord()!=0);
                String color = new String();
                int[] colors = new int[3];
                for(int j = 0;j < 4;j++){
                    if(j==3){
                        raf.readWord();
                        break;
                    }
                    colors[j] = raf.readWord();
                    color+=Integer.toHexString(colors[j]/256);
                    if(colors[j]==0){
                        color+="0";
                    }
                }
                while(raf.readWord()!=0);
                int length = raf.readWord();
                builder.append("name=\""+MPrefix);
                char[] names = new char[length];
                for (int j = 0; j < length; j++) {
                    if(j==length-1){
                        raf.readWord();
                        break;
                    }
                    names[j] = (char)raf.readWord();
                }
                String name = String.valueOf(names).toLowerCase().replaceAll("-"," ").replaceAll("\\s+","_").trim();
                builder.append(name+"\">#");
                builder.append(color+"</color>\n");
                i++;
            }
            builder.append("</resources>\n");
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void write(String name){
        if(name!=null&&name.length()!=0){
            MName = name+".xml";
        }else{
            MName = MName.substring(0,MName.lastIndexOf("."))+".xml";
        }
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(MPath+File.separator+MName))){
            writer.write(builder.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class ReadAcoFile{

        long len = 0;
        InputStream is;

        public ReadAcoFile(InputStream is){
            this.is = is;
        }

        public byte[] read()  throws IOException{
            byte[] bytes = new byte[2];
            if(is.read(bytes)!=-1){
                len+=2;
            }else{
                throw new IllegalStateException();
            }
            return bytes;
        }

        public int readWord() throws IOException{
            byte[] bytes = read();
            return (bytes[0] << 8)&0xff00 | bytes[1]&0x00ff;
        }
    }

}
