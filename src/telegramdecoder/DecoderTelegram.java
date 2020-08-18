/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegramdecoder;


import dpf.ap.gpinf.interfacetelegram.ContactInterface;
import dpf.ap.gpinf.interfacetelegram.DecoderTelegramInterface;
import dpf.ap.gpinf.interfacetelegram.MessageInterface;
import dpf.ap.gpinf.interfacetelegram.PhotoData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.TLRPC.DocumentAttribute; 

/**
 *
 * @author ADMHauck
 */
public class DecoderTelegram implements DecoderTelegramInterface{
    
    TLRPC.Message m=null;
    TLRPC.User u=null;
    TLRPC.Chat c=null;
    @Override
    public void setDecoderData(byte[] data,int TYPE) {
        SerializedData s=new SerializedData(data);
        int aux=s.readInt32(false);
        m=null;
        u=null;
        c=null;
        if(TYPE==MESSAGE){
            m=TLRPC.Message.TLdeserialize(s,aux, false);
        }
        if(TYPE==USER){
            u=TLRPC.User.TLdeserialize(s,aux, false);
        }
        if(TYPE==CHAT){
            c=TLRPC.Chat.TLdeserialize(s, aux, false);
        }
        
    }

    @Override
    public void getUserData(ContactInterface user)  {
        if(user!=null && u!=null){
            user.setUsername(u.username);
            user.setId(u.id);
            user.setName(u.first_name);
            user.setLastName(u.last_name);
            user.setPhone(u.phone);
        }else{
            System.err.println("Erro ao carregar usuario");
        }
    }
    
    public int getRemetenteId(){
        if(m!=null){
            return m.from_id;
        }
        return 0;
    }

    @Override
    public void getMessageData(MessageInterface message) {
       
        if (m!=null && message!=null ) {
            message.setFromMe(m.out);
            if(m.action!=null) {
                    if(m.action.call!=null) {
                            message.setType("call duration:"+m.action.duration);
                    }
                    if(m.action instanceof TLRPC.TL_messageActionChatJoinedByLink) {
                            message.setType("User Join chat by link");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionChatAddUser) {
                            message.setType("Chat Add User");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionUserJoined) {
                            message.setType("User Join");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionHistoryClear) {
                            message.setType("History Clear");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionChatDeleteUser) {
                            message.setType("User deleted");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionChannelCreate) {
                            message.setType("Channel created");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionUserUpdatedPhoto) {
                            message.setType("User update photo");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionChatEditPhoto) {
                            message.setType("Chat update photo");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionChatDeletePhoto) {
                            message.setType("Chat delete photo");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionChatEditTitle) {   	                        		
                            message.setType("Change title to "+m.action.title);
                    }
                    if(m.action instanceof TLRPC.TL_messageActionContactSignUp) {
                            message.setType("Contact sign up");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionChatMigrateTo) {
                            message.setType("Chat migrate");
                    }
                    if(m.action instanceof TLRPC.TL_messageActionPinMessage) {
                            message.setType("Message pinned");
                    }

                    if(message.getType()==null) {
                            message.setType(m.action.getClass().getSimpleName());
                            System.out.println("tipo desconhecido");
                    }

            }


            




            message.setData(m.message);


            message.setTimeStamp(Date.from(Instant.ofEpochSecond(m.date)));
            //message.timeStamp=LocalDateTime.ofInstant(Instant.ofEpochSecond(), ZoneId.systemDefault())
            if(m.media!=null) {
                if(m.media.document!=null) {
                    message.setMediaMime(m.media.document.mime_type);
                }

                if(m.media.photo!=null){
                    message.setMediaMime("image/jpeg");

                }
                if(m.media.webpage!=null) {
                    message.setLink(true);
                    message.setMediaMime("link");

                }


            }

        }
        //System.out.println(m.message);

    	                
    }

    @Override
    public void getChatData(ContactInterface chat) {
        if(chat!=null && c!=null){
            chat.setUsername(c.username);
            chat.setId(c.id);
            chat.setName(c.title);
            chat.setLastName(null);
            chat.setPhone(null);
        }
        
    }

    

    @Override
    public List<String> getDocumentNames() {
        ArrayList<String> list=new ArrayList<>();
        if(m!=null && m.media!=null && m.media.document!=null){
             list.add(m.media.document.id+"");
             for( DocumentAttribute at :m.media.document.attributes){
                //tentar achar pelo nome do arquivo original
                if(at.file_name!=null){
                	list.add(at.file_name);
                }
            }
         }
        return list;
    }

    @Override
    public List<PhotoData> getPhotoData() {
        ArrayList<PhotoData> list=new ArrayList<>();
        if(u!=null && u.photo!=null){
            if(u.photo.photo_big!=null){
                Photo p=new Photo();
                p.setName(""+ u.photo.photo_big.volume_id + "_" + u.photo.photo_big.local_id);
                list.add(p);
            }
            if(u.photo.photo_small!=null){
                Photo p=new Photo();
                p.setName(""+ u.photo.photo_small.volume_id + "_" + u.photo.photo_small.local_id);
                list.add(p);
            }
            
        }
        if(m!=null && m.media!=null){
            if(m.media.photo!=null){
                if(m.media.photo.sizes!=null &&m.media.photo.sizes.size()>0){
                    list.addAll(getPhotosFromSize(m.media.photo.sizes));
                }
            }
            if(m.media.webpage!=null){
                if(m.media.webpage.photo!=null && m.media.webpage.photo.sizes!=null){
                    list.addAll(getPhotosFromSize(m.media.webpage.photo.sizes));
                }
            }
            if(m.media.document!=null){
                if(m.media.document.thumbs!=null){
                    list.addAll(getPhotosFromSize(m.media.document.thumbs));
                }
            }
        }
        if(c!=null && c.photo!=null){
            if(c.photo.photo_big!=null){
                Photo p=new Photo();
                p.setName(""+ c.photo.photo_big.volume_id + "_" + c.photo.photo_big.local_id);
                list.add(p);
            }
            if(c.photo.photo_small!=null){
                Photo p=new Photo();
                p.setName(""+ c.photo.photo_small.volume_id + "_" + c.photo.photo_small.local_id);
                list.add(p);
            }
        }
        return list;
    }
    
    private List<PhotoData> getPhotosFromSize(ArrayList<TLRPC.PhotoSize> photos){
        ArrayList<PhotoData> l=new ArrayList<PhotoData>();
        if(photos==null){
            return l;
        }
        for(TLRPC.PhotoSize photo:photos){
            if(photo.location!=null){
                Photo p=new Photo();

                p.setName(photo.location.volume_id+"_"+photo.location.local_id);
                p.setSize(photo.size);
                l.add(p);
            }
        }
        return l;
    }

    @Override
    public int getDocumentSize() {
        if(m!=null && m.media!=null && m.media.document!=null){
            return m.media.document.size;
        }
        return -1;
    }
     public static Connection createConnection(String database){
         try {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection("jdbc:sqlite:"+database);
            } catch (Exception ex ) {
                System.err.println(ex.toString());
            }

        return null;
    }
    public static Object load(byte[] data){
        SerializedData s=new SerializedData(data);
        int aux=s.readInt32(false);
        return TLRPC.User.TLdeserialize(s,aux, false);
        
    }
    
    
    
   
    
}
