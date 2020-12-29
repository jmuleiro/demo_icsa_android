package com.example.demoicsa.approval;

import android.content.Context;

import com.example.demoicsa.R;

import org.jetbrains.annotations.NotNull;

public class Approval {
    /*
     *   Functional properties
     */
    public int layoutId;
    private int approvalType;
    public boolean selected = false;

    public String emplid;
    public String reqOprid;
    public String addressType;
    public String actionDate;
    public String effSeq;
    public String stepInstance;
    private String fullName;
    private String dateCreated;
    private String addressOld;
    private String addressNew;


    public void setFullName(@NotNull String firstName, @NotNull String middleName, @NotNull String lastName, @NotNull String secondLastName) {
        if (!firstName.equals("") && !firstName.equals(" ")){
            this.fullName = firstName;
            if (!middleName.equals("") && !middleName.equals(" "))
                this.fullName += " " + middleName;
            if (!lastName.equals("") && !lastName.equals(" "))
                this.fullName += " " + lastName;
            else {
                this.fullName = "Nulo";
                return;
            }
            if(!secondLastName.equals("") && !secondLastName.equals(" "))
                this.fullName += " " + secondLastName;
        }else
            this.fullName = "Nulo";
    }

    public String getFullName() {
        return fullName;
    }

    public void setDateCreated(@NotNull String dateCreated) {
        if (!dateCreated.equals("") && !dateCreated.equals(" ")){
            String dateValues[];
            dateValues = dateCreated.split("-");
            this.dateCreated = dateValues[2] + "/" + dateValues[1] + "/" + dateValues[0];
        }
        else
            this.dateCreated = "Nulo";
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setAddressOld(String addr1, String addr2, String addr3, String addr4){
        if (!addr1.equals(null) && !addr2.equals(null) && !addr3.equals(null) && !addr4.equals(null)){
            if (!addr1.equals("") && !addr1.equals(" ")){
                this.addressOld = addr1;
                if (!addr2.equals("") && !addr2.equals(" ")){
                    this.addressOld += " " + addr2;
                    if (!addr3.equals("") && !addr3.equals(" ")){
                        this.addressOld += ", " + addr3;
                        if (!addr4.equals("") && !addr4.equals(" "))
                            this.addressOld += ", " + addr4;
                    }
                }
            }
            else
                this.addressOld = "Nulo";
        }
        else
            this.addressOld = "Nulo";

    }

    public String getAddressOld() {
        return addressOld;
    }

    public void setAddressNew(@NotNull String addr1, @NotNull String addr2, @NotNull String addr3, @NotNull String addr4){
        if (!addr1.equals("") && !addr1.equals(" ")){
            this.addressNew = addr1;
            if (!addr2.equals("") && !addr2.equals(" ")){
                this.addressNew += " " + addr2;
                if (!addr3.equals("") && !addr3.equals(" ")){
                    this.addressNew += ", " + addr3;
                    if (!addr4.equals("") && !addr4.equals(" "))
                        this.addressNew += ", " + addr4;
                }
            }
        }
        else
            this.addressNew = "Nulo";
    }

    public String getAddressNew() {
        return addressNew;
    }

    public void setApprovalType(int approvalType) {
        this.approvalType = approvalType;
    }

    public String getApprovalType(Context context){
        switch (approvalType){
            case 1:
                return context.getString(R.string.approval_type_address_change);
            default:
                return "Nulo";
        }
    }
}
