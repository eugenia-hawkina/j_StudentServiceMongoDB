package telran.ashkelon2018.student.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import telran.ashkelon2018.student.dao.StudentRepository;
import telran.ashkelon2018.student.domain.Student;
import telran.ashkelon2018.student.dto.ScoreDto;
import telran.ashkelon2018.student.dto.StudentDto;
import telran.ashkelon2018.student.dto.StudentEditDto;
import telran.ashkelon2018.student.dto.StudentForbiddenException;
import telran.ashkelon2018.student.dto.StudentNotFoundException;
import telran.ashkelon2018.student.dto.StudentResponseDto;
import telran.ashkelon2018.student.dto.StudentUnauthorised;

// @Component
// спринг при запуске создает объект и помещает его в мапу
// ключ - имя класса (объект рефлексии), значение - объект
// автоматически связать объекты нашего созданного класса с аннотацией component
// (должен быть конструктор по умолчанию)
// и autowired над полем типа интерфейса 

// component ставим над классом, autowired над полем типа интерфейс
// связываем между собой классы

 @Service
 @ManagedResource
public class StudentServiceImpl implements StudentService {
	 
	 private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);
	 boolean logEnable= false;
	 
	 @Value("${time.title}")
	 String title;
	 
	@ManagedAttribute
	public boolean isLogEnable() {
		return logEnable;
	}

	@ManagedAttribute
	public void setLogEnable(boolean logEnable) {
		this.logEnable = logEnable;
	}

	@Autowired
	StudentRepository studentRepository;
	

	@Override
	public boolean addStudent(StudentDto studentDto) {
		if(studentRepository.existsById(studentDto.getId())) {
			// если студент есть, то не делаем ничего
			return false;
		}
		Student student = new Student(studentDto.getId(), studentDto.getName(), 
				studentDto.getPassword());
		studentRepository.save(student);
		// в монго save всегда перезаписывает
		return true;
		
	}

	@Override
	public StudentResponseDto deleteStudent(int id, String token) {
		Credentials credentials = decodeToken(token);		
		if(credentials.id != id) {
			throw new StudentForbiddenException();
		}
		Student student = studentRepository.findById(id).get();
		studentRepository.deleteById(id);
		// OR: studentRepository.delete(student);		
		return convertToStudentResponceDto(student);
	}

	private Credentials decodeToken(String token) {
		try {
			int index = token.indexOf(" ");
			token = token.substring(index + 1);
			byte[] base64DecodeBytes = Base64.getDecoder().decode(token);
			token = new String(base64DecodeBytes);
			String[] auth = token.split(":");
			Credentials credentials = new Credentials(Integer.parseInt(auth[0]), auth[1]);
			Student student = studentRepository.findById(credentials.id).get();
			if(!credentials.password.equals(student.getPassword())) {
				throw new StudentUnauthorised();
			}
			return credentials;
		} catch (Exception e) {
			throw new StudentUnauthorised();
		}
	}

	private StudentResponseDto convertToStudentResponceDto(Student student) {		
		return StudentResponseDto.builder()
				.id(student.getId())
				.name(student.getName())
				.score(student.getScores())
				.build();
	}

	@Override
	public StudentDto editStudent(int id, StudentEditDto studentEditDto, String token) {
		Credentials credentials = decodeToken(token);
		if(credentials.id != id) {
			throw new StudentForbiddenException();
		}
		Student student = studentRepository.findById(id).get();
		if(studentEditDto.getName() != null) {
			student.setName(studentEditDto.getName());
		}
		if(studentEditDto.getPassword() != null) {
			student.setPassword(studentEditDto.getPassword());
		}
		studentRepository.save(student);
		return convertToStudentDto(student);
	}

	private StudentDto convertToStudentDto(Student student) {
		return StudentDto.builder()
				.id(student.getId())
				.name(student.getName())
				.password(student.getPassword())
				.build();
	}

	@Override
	public StudentResponseDto getStudent(int id) {
		return convertToStudentResponceDto(studentRepository.findById(id)
				.orElseThrow(StudentNotFoundException::new));
		// OR: (() -> new StudentNotFoundException())
	}

	@Override
	public boolean addScore(int id, ScoreDto scoreDto) {
		LocalDateTime timeStamp = LocalDateTime.now();
		long t1 = System.currentTimeMillis();
		Student student = studentRepository.findById(id)
				.orElseThrow(StudentNotFoundException::new);
		boolean res = student.addScore(scoreDto.getExamName(), scoreDto.getScore());
		studentRepository.save(student);
		long t2 = System.currentTimeMillis();
		if(logEnable) {
			logger.info(title + "Time stamp: " + timeStamp + "\t Duration: " + (t2-t1));
		}
		return res;	
	}
	
	@AllArgsConstructor
	private class Credentials {
		int id;
		String password;		
	}

	@Override
	public List<StudentResponseDto> getStudentsByName(String name) {
//		return studentRepository.findPleaseBy()
//		// находит всех студентов в базе
//				.filter(s -> name.equals(s.getName()))
//				.map(this::convertToStudentResponceDto)
//				.collect(Collectors.toList());
		
		return studentRepository.findByNameRegex(name)
		// только отфильтрованных студентов дает
				.stream()
				.map(this::convertToStudentResponceDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<StudentResponseDto> getStudentsByExam(String exam, int minscore) {
		return studentRepository.findByExam("scores." + exam, minscore)
						.stream()
						.map(this::convertToStudentResponceDto)
						.collect(Collectors.toList());
	}
	
}
