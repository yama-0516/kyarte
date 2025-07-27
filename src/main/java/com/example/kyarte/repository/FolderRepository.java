package com.example.kyarte.repository;

import com.example.kyarte.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    
    // 名前で検索
    List<Folder> findByNameContainingIgnoreCase(String name);
    
    // 説明で検索
    List<Folder> findByDescriptionContainingIgnoreCase(String description);
    
    // 作成日時順でソート
    List<Folder> findAllByOrderByCreatedAtDesc();
    
    // 名前順でソート
    List<Folder> findAllByOrderByNameAsc();
    
    // 従業員数が多い順でソート
    @Query("SELECT f FROM Folder f LEFT JOIN f.employees e GROUP BY f ORDER BY COUNT(e) DESC")
    List<Folder> findAllOrderByEmployeeCountDesc();
    
    // 空のフォルダを取得
    @Query("SELECT f FROM Folder f WHERE f.employees IS EMPTY OR SIZE(f.employees) = 0")
    List<Folder> findEmptyFolders();
} 