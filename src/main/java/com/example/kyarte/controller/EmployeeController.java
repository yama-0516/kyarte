package com.example.kyarte.controller;

import com.example.kyarte.entity.Employee;
import com.example.kyarte.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    // 従業員一覧表示
    @GetMapping
    public String listEmployees(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        return "employees/list";
    }
    
    // 従業員詳細表示
    @GetMapping("/{id}")
    public String showEmployee(@PathVariable Long id, Model model) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if (employee.isPresent()) {
            model.addAttribute("employee", employee.get());
            return "employees/show";
        } else {
            return "redirect:/employees";
        }
    }
    
    // 従業員登録フォーム表示
    @GetMapping("/new")
    public String newEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employees/form";
    }
    
    // 従業員登録処理
    @PostMapping
    public String createEmployee(@ModelAttribute Employee employee) {
        employeeService.saveEmployee(employee);
        return "redirect:/employees";
    }
    
    // 従業員編集フォーム表示
    @GetMapping("/{id}/edit")
    public String editEmployeeForm(@PathVariable Long id, Model model) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if (employee.isPresent()) {
            model.addAttribute("employee", employee.get());
            return "employees/form";
        } else {
            return "redirect:/employees";
        }
    }
    
    // 従業員更新処理
    @PostMapping("/{id}")
    public String updateEmployee(@PathVariable Long id, @ModelAttribute Employee employee) {
        employee.setId(id);
        employeeService.saveEmployee(employee);
        return "redirect:/employees";
    }
    
    // 従業員削除処理
    @PostMapping("/{id}/delete")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/employees";
    }
    
    // 検索機能
    @GetMapping("/search")
    public String searchEmployees(@RequestParam String name, Model model) {
        List<Employee> employees = employeeService.searchEmployeesByName(name);
        model.addAttribute("employees", employees);
        model.addAttribute("searchName", name);
        return "employees/list";
    }
} 