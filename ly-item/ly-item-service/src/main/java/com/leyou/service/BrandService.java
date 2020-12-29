package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.mapper.BrandMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    //分页查询品牌
    public PageResult<Brand> queryBrandByPage(String key,Integer page,Integer rows,String sortBy,Boolean desc){
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        //判断是否为空，如果不为空的话就根据名称模糊查询或者根据首写字母查询
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);
        }
        //判断是否进行排序
        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+" "+(desc ? "desc" : "asc"));
        }
        //添加分页条件
        PageHelper.startPage(page,rows);
        //查询得到结果集
        List<Brand> brands = brandMapper.selectByExample(example);
        //将结果集包装成PageInfo对象
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //打包成PageResult并返回
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());


    }
    @Transactional
    public void saveBrand(Brand brand,List<Long> cids){
        this.brandMapper.insertSelective(brand);
        cids.forEach(cid ->{
            int row =  brandMapper.insertBrandAndCategory(cid,brand.getId());
        });

    }

    @Transactional
    public void updateBrand(Brand brand,List<Long> cids){
        //删除原数据
        this.brandMapper.deleteCategoryAndBrandByBid(brand.getId());
        this.brandMapper.updateByPrimaryKeySelective(brand);
        for (Long cid : cids) {
            this.brandMapper.insertBrandAndCategory(cid,brand.getId());
        }

    }
    public List<Brand> queryBrandByCid(Long cid){
        return brandMapper.queryBrandByCid(cid);
    }
    public Brand queryBrandById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        return brand;

    }


}
