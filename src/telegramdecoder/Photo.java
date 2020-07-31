/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegramdecoder;

import dpf.ap.gpinf.interfacetelegram.PhotoData;

/**
 *
 * @author ADMHauck
 */
public class Photo implements PhotoData{
    private String name=null;
    private int size=0;
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSize() {
        return size;
    }
    
    public void setName(String name){
        this.name=name;
    }
    public void setSize(int size){
        this.size=size;
    }
    
}
