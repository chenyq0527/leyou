package com.leyou.service;

import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Spu;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;
import com.leyou.pojo.SearchResult;
import com.leyou.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private IndexService indexService;
    
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public SearchResult queryGoodsByPage(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        if(StringUtils.isBlank(key)){
            return  null;
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //提取出查询字段
        //MatchQueryBuilder baseQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
        QueryBuilder boolQueryBuilder = buildBasicQueryWithFilter(searchRequest);

        queryBuilder.withQuery(boolQueryBuilder);
        //过滤结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));
        //开始分页
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();
        queryBuilder.withPageable(PageRequest.of(page-1,size));

        String categoryAggName = "categories";
        String brandAggName = "brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        AggregatedPage<Goods>  goodsPage = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());
        //商品分析
        List<Map<String,Object>> categories = getCategoriesAgg(goodsPage.getAggregation(categoryAggName));
        //品牌分析
        List<Brand> brandList = getBrandsAgg(goodsPage.getAggregation(brandAggName));
        List<Map<String,Object>> specs = null;
        if(!CollectionUtils.isEmpty(categories) && categories.size() == 1){
            specs = getParamAggResult( (Long)categories.get(0).get("id"),boolQueryBuilder);
        }
            //分装
        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());
        long totalElements = pageInfo.getTotalElements();
        long totalPages = pageInfo.getTotalPages();
        return new SearchResult(totalElements,totalPages,pageInfo.getContent(),categories,brandList,specs);

    }

    //这个方法用来构建查询条件以及过滤条件
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest searchRequest) {
        //构造布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        queryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()));

        //给这个查询加过滤
        // 过滤条件构建器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        //取出map中的实体
        for (Map.Entry<String, String> entry : searchRequest.getFilter().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 商品分类和品牌不用前后加修饰
            if (key != "cid3" && key != "brandId") {
                key = "specs." + key + ".keyword";
            }
            // 字符串类型，进行term查询
            filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
        }

        return queryBuilder.filter(filterQueryBuilder);
    }

    private List<Map<String, Object>> getParamAggResult(Long id, QueryBuilder boolQueryBuilder) {
        List<Map<String,Object>> specs = new ArrayList<>();
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(boolQueryBuilder);
        //通过商品id查询规格参数
        List<SpecParam> specParams = specificationClient.querySpecParamById(null, id, true);
        specParams.forEach(specParam -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs."+specParam.getName()+".keyword"));
        });
        //只要聚合结果集，不要查询结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        //解析聚合查询的结果集
        AggregatedPage<Goods> pageAgg = (AggregatedPage<Goods>)elasticsearchTemplate.queryForPage(queryBuilder.build(),Goods.class);
        Aggregations aggregations = pageAgg.getAggregations();

        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            Terms terms = aggregations.get(name);
            List<Object> options = terms.getBuckets().stream().map(bucket -> bucket.getKey()).collect(Collectors.toList());
            Map<String,Object> map = new HashMap<>();
            map.put("k",name);
            map.put("options",options);
            specs.add(map);
        }

        return specs;
    }

    private List<Map<String,Object>> getCategoriesAgg(Aggregation aggregation) {
        LongTerms terms = (LongTerms) aggregation;
        return terms.getBuckets().stream().map(bucket -> {
            Map<String,Object> map = new HashMap<>();
            Long id = bucket.getKeyAsNumber().longValue();
            List<String> names = categoryClient.queryNamesByIds(Arrays.asList(id));
            map.put("id",id);
            map.put("name",names.get(0));
            return map;
        }).collect(Collectors.toList());
    }

    private List<Brand> getBrandsAgg(Aggregation aggregation) {
        LongTerms terms = (LongTerms) aggregation;
        return terms.getBuckets().stream().map(bucket -> {
            return brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
        }).collect(Collectors.toList());
    }

    public void createIndex(Long id) {
        SpuBo spu =  this.goodsClient.querySpuById(id);
        Goods goods = this.indexService.buildGoods(spu);
        goodsRepository.save(goods);


    }
    public void deleteIndex(Long id){
        goodsRepository.deleteById(id);
    }
}