package com.leyou.es.test;

import com.alibaba.fastjson.JSON;
import com.leyou.es.entity.Goods;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.leyou.es.entity.ElasticConstants.SOURCE_TEMPLATE;

//静态导入，利用import static导入任意类的任意成员，导入的任意成员就作为当前类的成员
//import static java.lang.Math.abs;  abs就可以直接使用

//静态导入，利用import static导入任意类的任意成员，导入的任意成员就作为当前类的成员

/**
 * @author Leslie Arnold
 * BIO: blocking IO
 * NIO: non-blocking IO
 */
public class ElasticAsyncDemo {

    private RestHighLevelClient client;


    //建立连接

    @Before
    public void init() throws IOException {
        client = new RestHighLevelClient(
                RestClient.builder(
                        HttpHost.create("http://ly-es:9200")
//        new HttpHost("localhost", 9200, "http"),
//                new HttpHost("localhost", 9201, "http"),
//                new HttpHost("localhost", 9202, "http")
                )
        );
    }

    //关闭客户端连接

    @After
    public void close() throws IOException {
        client.close();
    }


    @Test
    public void testGetDocument() throws IOException, InterruptedException {

        //1.准备Request对象
        GetRequest request = new GetRequest("goods", "1"); //es中id永远是字符串,而且是keyword

        System.out.println("============准备发出请求==================");

        //2.发送request请求，得到response
        //actionlistener是一个回调函数
        //类似于ajax,  axios.get().then().catch()
        client.getAsync(request, RequestOptions.DEFAULT, new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse response) {
                System.out.println("=========获取响应============");
                //4.解析response
                String source = response.getSourceAsString();
                Goods goods = JSON.parseObject(source, Goods.class);
                System.out.println("goods = " + goods);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });

        //3.请求已经发出，结果还未出现
        System.out.println("==========请求已经发出，等待结果===============");


        Thread.sleep(2000);






    }



    /**
     * es的操作都分为4部
     * 1.准备Request对象（不同业务有不同的请求类型）
     * 2.给Request对象准备请求参数（准备kibana中编写的请求的JSON参数）
     * 3.发送request请求，得到response（client.xx(),xx对应request类型）
     * 4.解析Response
     * 索引库相关操作
     * 所有索引库对应的操作都封装在client的indicies中
     */


    @Test
    public void testSearch() throws IOException, InterruptedException {

        //对应search后的json数组
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //query查询
        sourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("title", "手机")));
        //sort
        sourceBuilder.sort("price", SortOrder.ASC);
        //分页
        sourceBuilder.from(0).size(5);
        //高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title"));
        System.out.println("============准备发出请求==================");
        //1.准备request
        SearchRequest searchRequest = new SearchRequest("goods");

        //2.准备请求参数
        searchRequest.source(sourceBuilder);


        //3.发送请求
        client.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse response) {
                System.out.println("=========获取响应============");
                //4.解析结果
                parseResponse(response);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });

        //3.请求已经发出，结果还未出现
        System.out.println("==========请求已经发出，等待结果===============");
        Thread.sleep(2000);




    }

    private void parseResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        //4.1 获取总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("共搜索到" + total + "条数据");
        //4.2 数据
        SearchHit[] hits = searchHits.getHits();
        List<Goods> goodsList = new ArrayList<>();
        //4.3 遍历解析数据
        for (SearchHit hit : hits) {
            //获取source
            String json = hit.getSourceAsString();
            //反序列化
            Goods goods = JSON.parseObject(json, Goods.class);

            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();

            for (HighlightField highlightField : highlightFields.values()) {
                //需要高亮的字段的名称
                String fieldName = highlightField.getName();
                //获取高亮结果，是高亮字段的值
                //Text[] fragments = highlightField.getFragments();
                //用StringUtils进行拼接
                String fieldValue = StringUtils.join(highlightField.getFragments());
                //反射获取对象字节码及与fieldName名称一致的成员变量
                try {
                    Field field = goods.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(goods,fieldValue );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }




            //普通写法，前提必须知道高亮字段 title
            /*HighlightField field = highlightFields.get("title");
            Text[] fragments = field.getFragments();
            String hTitle = StringUtils.join(fragments);


                goods.setTitle(hTitle);*/


            goodsList.add(goods);


        }
        //打印
        goodsList.forEach(System.out::println);
    }


}


