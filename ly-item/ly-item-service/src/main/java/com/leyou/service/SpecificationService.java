package com.leyou.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.mapper.SpecGroupMapper;
import com.leyou.mapper.SpecParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> querySpecGroupByCid(Long cid){
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> specGroups = specGroupMapper.select(specGroup);
        return  specGroups;

    }
    @Transactional
    public int updateSpecGroup(SpecGroup specGroup){
        int rows = specGroupMapper.updateByPrimaryKey(specGroup);
        return rows;
    }
    @Transactional
    public void deleteSpecGroupById(Long id){
        int rows = specGroupMapper.deleteByPrimaryKey(id);
    }
    @Transactional
    public int saveSpecGroup(SpecGroup specGroup){
        int rows = specGroupMapper.insert(specGroup);
        return rows;
    }
    public List<SpecParam> querySpecParamById(Long gid,Long cid,Boolean searching){
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> specParams = specParamMapper.select(specParam);
        return specParams;
    }
    @Transactional
    public void updateSpecParam(SpecParam specParam){
        specParamMapper.updateByPrimaryKeySelective(specParam);
    }

    public List<SpecGroup> querySpecsByCid(Long cid){
        List<SpecGroup> specGroups = this.querySpecGroupByCid(cid);
        List<SpecParam> specParams = this.querySpecParamById(null, cid, null);
        Map<Long,List<SpecParam>> map = new HashMap<>();
        for(SpecParam specParam : specParams){
            if(!map.containsKey(specParam.getGroupId())){
                map.put(specParam.getGroupId(),new ArrayList<>());
            }
            map.get(specParam.getGroupId()).add(specParam);
        }
        for(SpecGroup specGroup : specGroups){
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;

    }
}
