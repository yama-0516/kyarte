package com.example.kyarte.service;

import com.example.kyarte.entity.Employee;
import com.example.kyarte.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    // 全従業員を取得
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    // IDで従業員を取得
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
    
    // 従業員を保存
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    
    // 従業員を削除
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
    
    // 部署で検索
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }
    
    // 氏名で検索
    public List<Employee> searchEmployeesByName(String name) {
        // まず部分一致検索
        List<Employee> results = employeeRepository.findByLastNameContainingOrFirstNameContaining(name, name);
        if (results != null && !results.isEmpty()) {
            return results;
        }
        // 入力に姓/名が含まれるケース（例: 佐藤太郎, 佐藤さん など）
        String normalized = normalizeNameForSearch(name);
        List<Employee> contained = employeeRepository.findByNameContainedIn(normalized);
        return contained;
    }

    private String normalizeNameForSearch(String name) {
        if (name == null) {
            return "";
        }
        String trimmed = name.trim();
        // 敬称の除去
        trimmed = trimmed.replaceAll("(さん|氏|様)$", "");
        // 全角スペース・半角スペース除去
        trimmed = trimmed.replaceAll("[\\s　]+", "");
        return trimmed;
    }
    
    // 入社年で検索
    public List<Employee> getEmployeesByHireYear(int year) {
        return employeeRepository.findByHireYear(year);
    }
    
    // 部署と役職で検索
    public List<Employee> getEmployeesByDepartmentAndPosition(String department, String position) {
        return employeeRepository.findByDepartmentAndPosition(department, position);
    }
    
    // フォルダIDで従業員を取得
    public List<Employee> getEmployeesByFolderId(Long folderId) {
        return employeeRepository.findByFolderIdOrderByName(folderId);
    }
    
    // フォルダなしの従業員を取得
    public List<Employee> getEmployeesWithoutFolder() {
        return employeeRepository.findByFolderIsNull();
    }
    
    // フォルダ内の従業員数を取得
    public int getEmployeeCountInFolder(Long folderId) {
        return employeeRepository.countByFolderId(folderId);
    }
} 