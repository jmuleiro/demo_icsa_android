package com.example.demoicsa.psmessage;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="PSMessage", strict = false)
public class PSMessage<T>{
    @Element(name="MsgData")
    public MsgData<T> msgData;
}
