package com.marcosedo.yambu.app;

/**
 * Created by Marcos on 29/05/15.
 */
public class Currency {
    private String code;
    private String symbol;

    public Currency(String code,String symbol){
        this.code = code;
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSymbol(String symbol){
        this.symbol = symbol;
    }

    public String getSymbol(){
        return symbol;
    }
}
