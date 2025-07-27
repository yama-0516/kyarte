package com.example.kyarte.service;

import com.example.kyarte.entity.FreeNote;
import com.example.kyarte.repository.FreeNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class FreeNoteService {
    
    @Autowired
    private FreeNoteRepository freeNoteRepository;
    
    // フリーノートを保存
    public FreeNote saveFreeNote(FreeNote freeNote) {
        return freeNoteRepository.save(freeNote);
    }
    
    // 最新のノートを取得
    public List<FreeNote> getRecentNotes() {
        return freeNoteRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    // 未処理のノートを取得
    public List<FreeNote> getUnprocessedNotes() {
        return freeNoteRepository.findByProcessedOrderByCreatedAtDesc(false);
    }
    
    // 全ノートを取得
    public List<FreeNote> getAllFreeNotes() {
        return freeNoteRepository.findAll();
    }
    
    // ノートを処理済みにマーク
    public FreeNote markAsProcessed(Long id) {
        FreeNote note = freeNoteRepository.findById(id).orElse(null);
        if (note != null) {
            note.setProcessed(true);
            note.setProcessedAt(java.time.LocalDateTime.now());
            return freeNoteRepository.save(note);
        }
        return null;
    }
} 