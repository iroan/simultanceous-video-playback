package com.wangkaixuan.svpserver;

class HttpException extends Exception{
    private Status status;
    public HttpException(){}
    public HttpException(String errs){
        super(errs);
        this.status = new Status();
    }

    public HttpException(int code , String errs){
        this.status = new Status();
        this.status.httpStatusCode = code;
        this.status.success = false;
        this.status.errMsg = errs;
    }

    public HttpException(Status status){
        this.status =status;
    }

    public Status getStatus() {
        return status;
    }
}
