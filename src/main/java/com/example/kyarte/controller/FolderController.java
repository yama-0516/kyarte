package com.example.kyarte.controller;

import com.example.kyarte.entity.Folder;
import com.example.kyarte.entity.Employee;
import com.example.kyarte.service.FolderService;
import com.example.kyarte.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/folders")
public class FolderController {
    
    @Autowired
    private FolderService folderService;
    
    @Autowired
    private EmployeeService employeeService;
    
    // フォルダ一覧表示
    @GetMapping
    public String listFolders(Model model) {
        List<Folder> folders = folderService.getAllFolders();
        List<Employee> unassignedEmployees = employeeService.getEmployeesWithoutFolder();
        FolderService.FolderStatistics stats = folderService.getFolderStatistics();
        
        model.addAttribute("folders", folders);
        model.addAttribute("unassignedEmployees", unassignedEmployees);
        model.addAttribute("stats", stats);
        
        return "folders/list";
    }
    
    // フォルダ作成フォーム
    @GetMapping("/new")
    public String newFolderForm(Model model) {
        model.addAttribute("folder", new Folder());
        return "folders/form";
    }
    
    // フォルダ作成
    @PostMapping
    public String createFolder(@ModelAttribute Folder folder, RedirectAttributes redirectAttributes) {
        try {
            folderService.saveFolder(folder);
            redirectAttributes.addFlashAttribute("success", "フォルダを作成しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "フォルダの作成に失敗しました。");
        }
        return "redirect:/folders";
    }
    
    // フォルダ編集フォーム
    @GetMapping("/{id}/edit")
    public String editFolderForm(@PathVariable Long id, Model model) {
        Optional<Folder> folderOpt = folderService.getFolderById(id);
        if (folderOpt.isPresent()) {
            model.addAttribute("folder", folderOpt.get());
            return "folders/form";
        }
        return "redirect:/folders";
    }
    
    // フォルダ更新
    @PostMapping("/{id}")
    public String updateFolder(@PathVariable Long id, @ModelAttribute Folder folder, RedirectAttributes redirectAttributes) {
        try {
            Optional<Folder> existingFolderOpt = folderService.getFolderById(id);
            if (existingFolderOpt.isPresent()) {
                Folder existingFolder = existingFolderOpt.get();
                existingFolder.setName(folder.getName());
                existingFolder.setDescription(folder.getDescription());
                folderService.saveFolder(existingFolder);
                redirectAttributes.addFlashAttribute("success", "フォルダを更新しました。");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "フォルダの更新に失敗しました。");
        }
        return "redirect:/folders";
    }
    
    // フォルダ削除
    @PostMapping("/{id}/delete")
    public String deleteFolder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            folderService.deleteFolder(id);
            redirectAttributes.addFlashAttribute("success", "フォルダを削除しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "フォルダの削除に失敗しました。");
        }
        return "redirect:/folders";
    }
    
    // フォルダ詳細表示
    @GetMapping("/{id}")
    public String showFolder(@PathVariable Long id, Model model) {
        Optional<Folder> folderOpt = folderService.getFolderById(id);
        if (folderOpt.isPresent()) {
            Folder folder = folderOpt.get();
            List<Employee> employees = employeeService.getEmployeesByFolderId(id);
            
            model.addAttribute("folder", folder);
            model.addAttribute("employees", employees);
            return "folders/show";
        }
        return "redirect:/folders";
    }
    
    // 従業員をフォルダに移動
    @PostMapping("/{folderId}/employees/{employeeId}/move")
    @ResponseBody
    public String moveEmployeeToFolder(@PathVariable Long folderId, @PathVariable Long employeeId) {
        try {
            folderService.moveEmployeeToFolder(employeeId, folderId);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    // 従業員をフォルダから削除
    @PostMapping("/employees/{employeeId}/remove")
    @ResponseBody
    public String removeEmployeeFromFolder(@PathVariable Long employeeId) {
        try {
            folderService.removeEmployeeFromFolder(employeeId);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    // フォルダ検索
    @GetMapping("/search")
    public String searchFolders(@RequestParam String query, Model model) {
        List<Folder> folders = folderService.searchFoldersByName(query);
        List<Folder> descriptionResults = folderService.searchFoldersByDescription(query);
        
        // 重複を除去
        folders.addAll(descriptionResults);
        folders = folders.stream().distinct().toList();
        
        model.addAttribute("folders", folders);
        model.addAttribute("searchQuery", query);
        return "folders/search";
    }
} 