package telran.ashkelon2018.student.dao;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import telran.ashkelon2018.student.domain.Student;

public interface StudentRepository extends MongoRepository<Student, Integer> {
	// класс, который имплементирует интерфейс, будет создан спрингом при запуске
	// мы этот класс не видим
	// спринг реализует все методы сам
	
	Stream<Student> findPleaseBy();
	// спринг сам создает методы, которые начинаются с файндБай
	// мы задаем ему параметр
	// все что между файнд и бай - не имеет значения
	
	List<Student> findByNameRegex(String regex);

	@Query("{'?0':{'$gt':?1}}")
	// нулевой аргумент: балл выше, чем первый передаваемый аргумент (нумерация с нуля!)
	List<Student> findByExam(String exam, int minscore);
	

}
