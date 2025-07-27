package com.example.kyarte.service;

import com.example.kyarte.entity.Folder;
import com.example.kyarte.entity.Employee;
import com.example.kyarte.repository.FolderRepository;
import com.example.kyarte.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FolderService {
    
    @Autowired
    private FolderRepository folderRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    // 全フォルダを取得
    public List<Folder> getAllFolders() {
        return folderRepository.findAllByOrderByNameAsc();
    }
    
    // フォルダをIDで取得
    public Optional<Folder> getFolderById(Long id) {
        return folderRepository.findById(id);
    }
    
    // フォルダを保存
    public Folder saveFolder(Folder folder) {
        return folderRepository.save(folder);
    }
    
    // フォルダを削除
    public void deleteFolder(Long id) {
        // フォルダ内の従業員をフォルダなしに設定
        List<Employee> employees = employeeRepository.findByFolderId(id);
        for (Employee employee : employees) {
            employee.setFolder(null);
            employeeRepository.save(employee);
        }
        
        folderRepository.deleteById(id);
    }
    
    // フォルダ名で検索
    public List<Folder> searchFoldersByName(String name) {
        return folderRepository.findByNameContainingIgnoreCase(name);
    }
    
    // フォルダ説明で検索
    public List<Folder> searchFoldersByDescription(String description) {
        return folderRepository.findByDescriptionContainingIgnoreCase(description);
    }
    
    // 従業員をフォルダに移動
    public void moveEmployeeToFolder(Long employeeId, Long folderId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        Optional<Folder> folderOpt = folderRepository.findById(folderId);
        
        if (employeeOpt.isPresent() && folderOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            Folder folder = folderOpt.get();
            employee.setFolder(folder);
            employeeRepository.save(employee);
        }
    }
    
    // 従業員をフォルダから削除
    public void removeEmployeeFromFolder(Long employeeId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setFolder(null);
            employeeRepository.save(employee);
        }
    }
    
    // フォルダ内の従業員数を取得
    public int getEmployeeCountInFolder(Long folderId) {
        return employeeRepository.countByFolderId(folderId);
    }
    
    // 空のフォルダを取得
    public List<Folder> getEmptyFolders() {
        return folderRepository.findEmptyFolders();
    }
    
    // フォルダの統計情報を取得
    public FolderStatistics getFolderStatistics() {
        List<Folder> allFolders = getAllFolders();
        List<Folder> emptyFolders = getEmptyFolders();
        
        return new FolderStatistics(
            allFolders.size(),
            emptyFolders.size(),
            allFolders.size() - emptyFolders.size()
        );
    }
    
    // フォルダ統計情報クラス
    public static class FolderStatistics {
        private final int totalFolders;
        private final int emptyFolders;
        private final int nonEmptyFolders;
        
        public FolderStatistics(int totalFolders, int emptyFolders, int nonEmptyFolders) {
            this.totalFolders = totalFolders;
            this.emptyFolders = emptyFolders;
            this.nonEmptyFolders = nonEmptyFolders;
        }
        
        public int getTotalFolders() { return totalFolders; }
        public int getEmptyFolders() { return emptyFolders; }
        public int getNonEmptyFolders() { return nonEmptyFolders; }
    }
} 