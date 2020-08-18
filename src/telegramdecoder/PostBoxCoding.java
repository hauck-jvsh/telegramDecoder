/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegramdecoder;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 *
 * @author ADMHauck
 */
public class PostBoxCoding {
    public static int int32 = 0;
    public static int Int64 = 1;
    public static int Bool = 2;
    public static int Double = 3;
    public static int STRING = 4;
    public static int OBJECT = 5;
    public static int Int32Array = 6;
    public static int Int64Array = 7;
    public static int ObjectArray = 8;
    public static int ObjectDictionary = 9;
    public static int Bytes = 10;
    public static int Nil = 11;
    public static int StringArray = 12;
    public static int BytesArray = 13;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
    private byte[] data=null;
    
    private int offset=0;
    
    public int readInt32(int start) {
        try {
            int i = 0;
            for (int j = 0; j < 4; j++) {
                int a=data[start+j];
                a=a&0xFF;
                i |= (a << (j * 8));
            }
            return i;
        } catch (Exception e) {
            
        }
        
        return 0;
    }
    
    public long readInt64(int start) {
        try {
            long i = 0;
            for (int j = 0; j < 8; j++) {
                int a=data[start+j];
                a=a&0xFF;
                i |= (a << (j * 8));
            }
            return i;
        } catch (Exception e) {
            
        }
        
        return 0;
    }
    
    public String readString(int start, int tam){
        if(start+tam>=data.length)
            return null;
        return new String(Arrays.copyOfRange(data, start,start+tam ),StandardCharsets.UTF_8);
    }
    
    private boolean findOfset(String key,int type){
        int start=offset;
        while(offset<data.length){
            int offant=offset;
            int keylength = data[offset];
            keylength=keylength & 0xFF;
            String readk=readString(++offset, keylength);
            offset+=keylength;
            
            if(offset>=data.length){
                offset=offant+1;
                continue;
            }
            
            int readtype=data[offset];
            readtype=readtype & 0xFF;
            
            if(keylength==key.length() && key.equals(readk) && type==readtype){
                offset++;
                return true;
            }
            offset=offant+1;
            
        }
        if(start>0){
            offset=0;
            return findOfset(key, type);
        }
        return false;
    }
    
    public String decodeStringForKey(String key){
        if(findOfset(key, STRING)){
            int tam=readInt32(offset);
            offset+=4;
            String aux=readString(offset, tam);
            offset+=tam;
            return aux;
        }
        return null;
        
    }
    public genericObj readObj(){
        try{
        genericObj obj=new genericObj();
        obj.hash=readInt32(offset);
        offset+=4;
        int btam=readInt32(offset);
        offset+=4;
        obj.content=Arrays.copyOfRange(data, offset, offset+btam);
        offset+=btam;
        return obj;
        }catch(Exception e){
            return null;
        }
    }
    public genericObj decodeObjectForKey(String key){
        if(findOfset(key, OBJECT)){
            return readObj();
        }
        return null;
    }
    public int decodeInt32ForKey(String key){
        if(findOfset(key, int32)){
            int val=readInt32(offset);
            offset+=4;
            return val;
        }
        return 0;
    }
    public long decodeInt64ForKey(String key){
        if(findOfset(key,Int64)){
            long val=readInt64(offset);
            offset+=8;
            return val;
        }
        return 0;
    }
    
    public List<genericObj> decodeObjectArrayForKey(String key){
        ArrayList<genericObj> l=new ArrayList<>();
        if(findOfset(key, ObjectArray)){
            int tam=readInt32(offset);
            offset+=4;
            for(int i=0;i<tam;i++){
                genericObj o=readObj();
                if(o!=null){
                    l.add(o);
                }
            }
        }
        return l;
    }
    
    
    public static void main(String[] args){
       Connection conn=DecoderTelegram.createConnection("D:\\telegram_ios\\db_sqlite");
       
       for(int i=7;i<=7;i++){
           try{
               String sql="SELECT * from t"+i;//" where HEX(value) like '%546d6a20746f64%'";
               Statement stmt=conn.createStatement();
               ResultSet rs=stmt.executeQuery(sql);
               int k=0;
               while(rs.next()){
                    //System.out.println("t"+i);
                    byte[] dados=rs.getBytes("value");
                    PostBoxCoding p=new PostBoxCoding();
                    p.setData(dados);
                    long peerid=p.readInt32(0x1C-8);
                    int tam=p.readInt32(0x1C);
                    k++;
                    System.out.println("tam="+tam);
                    System.out.println("user id="+peerid);
                    if(tam>=dados.length){
                        System.out.println("k="+k);
                    }else{
                        
                        String msg;
                        if(tam>20)
                            msg=p.readString(0x20, 20);
                        else
                            msg=p.readString(0x20, tam);
                        System.out.println(msg);
                    }
               }
              
               
               /*
               
               genericObj user=p.decodeObjectForKey("_");
               p.setData(user.content);
               System.out.println(p.decodeStringForKey("fn"));
               System.out.println(p.decodeStringForKey("un"));
               System.out.println(p.decodeStringForKey("p"));
               List<genericObj> l= p.decodeObjectArrayForKey("ph");
               for(genericObj ph:l){
                   PostBoxCoding p2=new PostBoxCoding();
                   p2.setData(ph.content);
                   System.out.println("volume:"+p2.decodeInt64ForKey("v"));
                   System.out.println("local:"+p2.decodeInt32ForKey("l"));
               }
               */
           }catch(SQLException e){
               //e.printStackTrace();
           }
       }
    }
    
}
