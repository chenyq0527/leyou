package com.leyou.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> specGroups = specificationService.querySpecsByCid(cid);
        if(CollectionUtils.isEmpty(specGroups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specGroups);
    }

    @PostMapping("group")
    public ResponseEntity<Void> saveSpecGroup(SpecGroup specGroup){
        int rows = specificationService.saveSpecGroup(specGroup);
        if(rows < 1){
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(HttpStatus.CREATED);

    }
    @PutMapping("group")
    public ResponseEntity<Void> updateSpecGroup(SpecGroup specGroup){
        int rows = specificationService.updateSpecGroup(specGroup);
        if(rows <= 0){
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);

    }
    @DeleteMapping("group/{id}")
    public ResponseEntity<Void> deleteSpecGroupById(@PathVariable("id")Long id){
        specificationService.deleteSpecGroupById(id);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParamById(@RequestParam(value = "gid",required = false) Long gid,
                                                              @RequestParam(value = "cid",required = false)Long cid,
                                                              @RequestParam(value = "searching",required = false) Boolean searching){
        List<SpecParam> specParams = specificationService.querySpecParamById(gid,cid,searching);
        if(CollectionUtils.isEmpty(specParams)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specParams);

    }
    @PutMapping("param")
    public ResponseEntity<Void> updateSpecParam(SpecParam specParam){
        specificationService.updateSpecParam(specParam);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }



}
