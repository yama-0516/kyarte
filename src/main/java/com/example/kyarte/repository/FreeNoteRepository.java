package com.example.kyarte.repository;

import com.example.kyarte.entity.FreeNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FreeNoteRepository extends JpaRepository<FreeNote, Long> {
    
    // 未処理のノートを取得（AI解析前）
    List<FreeNote> findByProcessedOrderByCreatedAtDesc(Boolean processed);
    
    // 最新のノートを取得
    List<FreeNote> findTop10ByOrderByCreatedAtDesc();
} 