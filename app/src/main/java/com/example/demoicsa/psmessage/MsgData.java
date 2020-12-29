package com.example.demoicsa.psmessage;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name="MsgData", strict = false)
public class MsgData<T>{
    @ElementList(name = "Transaction", inline = true)
    public List<T> transactions;
}

