package com.example.cs304project.controller;


import com.example.cs304project.dto.CodeResultDTO;
import com.example.cs304project.dto.CodeSnippetDTO;
import com.example.cs304project.entity.CodeSnippet;
import com.example.cs304project.service.CodeSnippetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/codes")
@CrossOrigin(origins = "http://localhost:5173")
public class CodeSnippetController {


    @Autowired
    private CodeSnippetService codeSnippetService;

    //post /api/codes/create 创建代码
    @PostMapping("/{slideId}/create")
    public ResponseEntity<CodeSnippetDTO> createCode(@PathVariable Long slideId,
                                                     @RequestBody CodeSnippetDTO codeDTO){
        CodeSnippet code = new CodeSnippet();
        code.setLanguage(codeDTO.getLanguage());
        code.setContext(codeDTO.getContext());
        code.setResult(codeSnippetService.runCode(codeDTO.getContext(),codeDTO.getLanguage()));

        CodeSnippet created = codeSnippetService.createCode(slideId,code);

        CodeSnippetDTO dto = new CodeSnippetDTO();
        dto.setCodeSnippetId(created.getCodeSnippetId());
        dto.setContext(created.getContext());
        dto.setLanguage(created.getLanguage());
        dto.setResult(created.getResult());


        return ResponseEntity.ok(dto);

    }

    //post /api/codes/run 实时运行代码，返回结果
    @PostMapping("/run")
    public ResponseEntity<String> runCode(@RequestBody CodeResultDTO resultDTO){
        String result = codeSnippetService.runCode(resultDTO.getCode(),resultDTO.getLanguage());
        return ResponseEntity.ok(result);
    }

    //get /api/codes/{codeId}/getCodeById 根据id获取代码内容
    @GetMapping("/{codeId}/getCodeById")
    public ResponseEntity<CodeSnippetDTO> getCodeById(@PathVariable Long codeId){
        CodeSnippet code = codeSnippetService.getCodeById(codeId);
        CodeSnippetDTO dto = new CodeSnippetDTO();
        dto.setCodeSnippetId(code.getCodeSnippetId());
        dto.setContext(code.getContext());
        dto.setLanguage(code.getLanguage());
        dto.setResult(code.getResult());

        return ResponseEntity.ok(dto);

    }

    //get /api/codes/{slideId}/getCodeBySlide 根据课件获取代码内容
    @GetMapping("/{slideId}/getCodeBySlide")
    public ResponseEntity<List<CodeSnippetDTO>> getCodesBySlide(@PathVariable Long slideId){
        List<CodeSnippet> codes = codeSnippetService.getCodesBySlide(slideId);
        List<CodeSnippetDTO> dtos = codes.stream().map(code -> {
            CodeSnippetDTO dto = new CodeSnippetDTO();
            dto.setCodeSnippetId(code.getCodeSnippetId());
            dto.setContext(code.getContext());
            dto.setLanguage(code.getLanguage());
            dto.setResult(code.getResult());

            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //delete /api/code/{codeId}/delete 删除代码存储
    @DeleteMapping("/{codeId}/delete")
    public ResponseEntity<String> deleteCode(@PathVariable Long codeId){
        codeSnippetService.deleteCode(codeId);
        return ResponseEntity.ok("已成功删除代码");
    }
}
