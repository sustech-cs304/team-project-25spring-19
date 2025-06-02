package com.example.cs304project.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

public class GlobalExceptionHandler {

    //资源不存在的异常：课程课件在数据库中找不到等等
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handResourceNotFoundException(ResourceNotFoundException ex, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
    //处理无效请求的异常：格式错误，必填字段缺失等等
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorDetails> handInvalidRequestException(InvalidRequestException ex, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    //处理代码执行异常：代码编译错误运行超时等等
    @ExceptionHandler(CodeExecutionException.class)
    public ResponseEntity<ErrorDetails> handCodeExecutionException(CodeExecutionException ex, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    //处理AI异常：调用Ai的异常
    @ExceptionHandler(AIProcessingException.class)
    public ResponseEntity<ErrorDetails> handAIProcessingException(AIProcessingException ex, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    //处理文件异常：PPT、PDF文件异常
    @ExceptionHandler(FileProcessException.class)
    public ResponseEntity<ErrorDetails> handFileProcessException(FileProcessException ex, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    //其他异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handGlobalException(Exception ex, WebRequest request){
        ErrorDetails errorDetails = new ErrorDetails(new Date(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
