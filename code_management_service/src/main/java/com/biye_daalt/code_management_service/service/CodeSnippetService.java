package com.biye_daalt.code_management_service.service;

import com.biye_daalt.code_management_service.model.CodeSnippet;
import com.biye_daalt.code_management_service.repository.CodeSnippetRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CodeSnippetService {
    private final CodeSnippetRepository codeSnippetRepository;

    @Autowired
    public CodeSnippetService(CodeSnippetRepository repository) {
        this.codeSnippetRepository = repository;
    }

    public List<CodeSnippet> getAllCodeSnippets() {
        return codeSnippetRepository.findAll();
    }

    public Optional<CodeSnippet> getCodeSnippetById(Long id){
        return codeSnippetRepository.findById(id);
    }

    @Transactional
    public CodeSnippet createCodeSnippet(CodeSnippet snippet) {
        if (snippet.getComments()==null) {
            snippet.setComments(new ArrayList<>());
        }
        return codeSnippetRepository.save(snippet);
    }

    @Transactional
    public Optional<CodeSnippet> updateCodeSnippet(Long id, CodeSnippet details) {
        return codeSnippetRepository.findById(id)
                .map(existingSnippet -> {
                    existingSnippet.setName(details.getName());
                    existingSnippet.setProblemMarkdown(details.getProblemMarkdown());
                    existingSnippet.setLanguage(details.getSolution());
                    existingSnippet.setLanguage(details.getLanguage());

                    return codeSnippetRepository.save(existingSnippet);
                });
    }

    @Transactional
    public boolean deleteCodeSnippet(Long id) {
        if (codeSnippetRepository.existsById(id)) {
            codeSnippetRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<CodeSnippet> addCommentToSnippet(Long snippetId, String comment){
        return codeSnippetRepository.findById(snippetId)
                .map(snippet -> {
                    snippet.getComments().add(comment);
                    return codeSnippetRepository.save(snippet);
                });
    }

    @Transactional
    public Optional<CodeSnippet> clearCommentsFromSnippet(Long snippetId) {
        return codeSnippetRepository.findById(snippetId)
                .map(snippet -> {
                    snippet.getComments().clear();
                    return codeSnippetRepository.save(snippet);
                });
    }
}
