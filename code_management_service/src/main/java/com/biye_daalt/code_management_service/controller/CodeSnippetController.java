package com.biye_daalt.code_management_service.controller;

import com.biye_daalt.code_management_service.model.CodeSnippet;
import com.biye_daalt.code_management_service.service.CodeSnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/code-snippets")
public class CodeSnippetController {
    private final CodeSnippetService codeSnippetService;

    @Autowired
    public CodeSnippetController(CodeSnippetService service){
        this.codeSnippetService = service;
    }

    @GetMapping
    public ResponseEntity<List<CodeSnippet>> getAllCodeSnippets() {
        return ResponseEntity.ok(codeSnippetService.getAllCodeSnippets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeSnippet> getCodeSnippetById(@PathVariable Long id){
        return codeSnippetService.getCodeSnippetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CodeSnippet> createCodeSnippet(@RequestBody CodeSnippet codeSnippet) {
        if (codeSnippet.getName() == null || codeSnippet.getName().isBlank() ||
            codeSnippet.getProblemMarkdown() == null || codeSnippet.getProblemMarkdown().isBlank() ||
            codeSnippet.getLanguage() == null || codeSnippet.getLanguage().isBlank()
        ) {
            return ResponseEntity.badRequest().build();
        }
        CodeSnippet createdSnippet = codeSnippetService.createCodeSnippet(codeSnippet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSnippet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CodeSnippet> updateCodeSnippet(@PathVariable Long id, @RequestBody CodeSnippet codeSnippet) {
        return codeSnippetService.updateCodeSnippet(id, codeSnippet)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CodeSnippet> deleteCodeSnippet(@PathVariable Long id) {
        if (codeSnippetService.deleteCodeSnippet(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CodeSnippet> addComment(@PathVariable Long id, @RequestBody Map<String,String> payload) {
        String commentText = payload.get("comment");
        if (commentText == null || commentText.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return codeSnippetService.addCommentToSnippet(id, commentText)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/comments")
    public ResponseEntity<CodeSnippet> clearComments(@PathVariable Long id) {
        return codeSnippetService.clearCommentsFromSnippet(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
