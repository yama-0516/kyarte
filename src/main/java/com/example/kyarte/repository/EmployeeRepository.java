package com.example.kyarte.repository;

import com.example.kyarte.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // 部署で検索
    List<Employee> findByDepartment(String department);
    
    // 氏名で検索（部分一致）
    List<Employee> findByLastNameContainingOrFirstNameContaining(String lastName, String firstName);

    // 入力文字列に従業員の姓/名が含まれている場合にマッチ（例: 入力が「佐藤太郎」でも命中）
    @Query("SELECT e FROM Employee e WHERE :name LIKE CONCAT('%', e.lastName, '%') OR :name LIKE CONCAT('%', e.firstName, '%')")
    List<Employee> findByNameContainedIn(@Param("name") String name);
    
    // 入社年で検索
    @Query("SELECT e FROM Employee e WHERE YEAR(e.hireDate) = :year")
    List<Employee> findByHireYear(@Param("year") int year);
    
    // 部署と役職で検索
    List<Employee> findByDepartmentAndPosition(String department, String position);
    
    // フォルダIDで検索
    List<Employee> findByFolderId(Long folderId);
    
    // フォルダなしの従業員を検索
    List<Employee> findByFolderIsNull();
    
    // フォルダIDで従業員数をカウント
    int countByFolderId(Long folderId);
    
    // フォルダ別に従業員を取得
    @Query("SELECT e FROM Employee e WHERE e.folder.id = :folderId ORDER BY e.lastName, e.firstName")
    List<Employee> findByFolderIdOrderByName(@Param("folderId") Long folderId);
} 