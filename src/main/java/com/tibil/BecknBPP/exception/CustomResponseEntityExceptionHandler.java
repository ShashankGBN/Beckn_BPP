package com.tibil.BecknBPP.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tibil.BecknBPP.dto.Ack;
import com.tibil.BecknBPP.dto.Ack.StatusEnum;
import com.tibil.BecknBPP.dto.Error;
import com.tibil.BecknBPP.dto.Error.TypeEnum;
import com.tibil.BecknBPP.dto.InlineResponse200;
import com.tibil.BecknBPP.dto.InlineResponse200Message;

@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		InlineResponse200 responseObj = new InlineResponse200();
		responseObj.setMessage(new InlineResponse200Message().ack(new Ack().status(StatusEnum.NACK)));
		responseObj.setError(new Error().type(TypeEnum.JSON_SCHEMA_ERROR).message(ex.getFieldError().getDefaultMessage()));
				
		return new ResponseEntity<Object>(responseObj, HttpStatus.BAD_REQUEST);
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		InlineResponse200 responseObj = new InlineResponse200();
		responseObj.setMessage(new InlineResponse200Message().ack(new Ack().status(StatusEnum.NACK)));
		responseObj.setError(new Error().type(TypeEnum.JSON_SCHEMA_ERROR).message(ex.getMessage()));
				
		return new ResponseEntity<Object>(responseObj, HttpStatus.BAD_REQUEST);
	}
	

}
