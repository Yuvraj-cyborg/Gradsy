package com.example.application.services;

import com.example.application.data.Note;
import com.example.application.data.User;
import com.example.application.repositories.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {
    
    private final NoteRepository noteRepository;
    
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }
    
    @Transactional
    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }
    
    @Transactional(readOnly = true)
    public Optional<Note> findById(Long id) {
        return noteRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Note> findNotesByUser(User user) {
        return noteRepository.findByUserOrderByUpdatedAtDesc(user);
    }
    
    @Transactional
    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean isNoteOwner(Note note, User user) {
        return note.getUser().equals(user);
    }
} 