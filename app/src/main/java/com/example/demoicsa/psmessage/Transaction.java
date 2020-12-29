package com.example.demoicsa.psmessage;

//@Root(name="Transaction", strict = false)
//public class Transaction{
//    // signonCount
//    public int rCount;
//}

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root(name="Transaction", strict = false)
public class Transaction{
    @Path(value = "./AND_RESP_RST")
    @Element(name = "DESCR_X")
    public String signonCountStr;

    @Path(value = "./AND_RESP_RST")
    @Element(name = "PT_SIGNON_TYPE")
    public String signonType;

    public int rCount = 0;

    void setrCount(Object r){
        if (r instanceof Integer)
            this.rCount = ((Integer) r).intValue();
        else
            throw new ClassCastException();
    }

    public int getrCount(){
        return this.rCount;
    }

}