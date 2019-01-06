package telran.ashkelon2018.student.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class StudentUnauthorised extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
