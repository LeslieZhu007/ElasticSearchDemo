package com.leyou.es.test;

import com.alibaba.fastjson.JSON;
import com.leyou.es.entity.Goods;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
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
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
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
import sun.net.dns.ResolverConfiguration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//静态导入，利用import static导入任意类的任意成员，导入的任意成员就作为当前类的成员
import static com.leyou.es.entity.ElasticConstants.SOURCE_TEMPLATE;
//import static java.lang.Math.abs;  abs就可以直接使用

//静态导入，利用import static导入任意类的任意成员，导入的任意成员就作为当前类的成员

/**
 * @author 虎哥
 */
public class ElasticDemo {

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
    public void testCreateIndex() throws IOException {
        //1.准备Request对象
        CreateIndexRequest request = new CreateIndexRequest("goods");

        //2.给Request对象准备请求参数
        request.source(SOURCE_TEMPLATE, XContentType.JSON);

        //3.发送request请求，得到response
        client.indices().create(request, RequestOptions.DEFAULT);

        //4.解析response
    }

    @Test
    public void testDeleteIndex() throws IOException {
        //1.准备Request对象
        DeleteIndexRequest request = new DeleteIndexRequest("goods");


        //3.发送request请求，得到response
        client.indices().delete(request, RequestOptions.DEFAULT);

    }


    @Test
    public void testAddDocument() throws IOException {
        //一个文档实体
        Goods goods = new Goods(1L, Arrays.asList("红米9", "手机"), "红米9手机 数码", 1499L);


        //1.准备Request对象
        IndexRequest request = new IndexRequest("goods");

        //2.给Request对象准备请求对象
        request.id(goods.getId().toString());
        //把goods对象转成json
        request.source(JSON.toJSONString(goods), XContentType.JSON);

        //3.发送request请求，得到response
        client.index(request, RequestOptions.DEFAULT);
        //4.解析response
    }

    @Test
    public void testGetDocument() throws IOException {

        //1.准备Request对象
        GetRequest request = new GetRequest("goods", "1"); //es中id永远是字符串,而且是keyword


        //3.发送request请求，得到response
        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        //4.解析response
        String source = response.getSourceAsString();
        Goods goods = JSON.parseObject(source, Goods.class);
        System.out.println("goods = " + goods);

    }


    @Test
    public void testDeletetDocument() throws IOException {

        //1.准备Request对象
        DeleteRequest request = new DeleteRequest("goods", "1"); //es中id永远是字符串,而且是keyword


        //3.发送request请求，得到response
        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);


    }

    //修改和增加的原理是一样的


    @Test
    public void testBulk() throws IOException {

        //0.准备文档数据
        List<Goods> list = new ArrayList<>();
        list.add(new Goods(1L, Arrays.asList("红米9", "手机"), "红米9手机 数码", 1499L));
        list.add(new Goods(2L, Arrays.asList("Galaxy", "手机"), "三星 Galaxy A90 手机 数码 疾速5G 骁龙855", 3099L));
        list.add(new Goods(3L, Arrays.asList("Sony", "WH-1000XM3", "数码"), "Sony WH-1000XM3 降噪耳机 数码", 2299L));
        list.add(new Goods(4L, Arrays.asList("松下", "剃须刀"), "松下电动剃须刀高转速磁悬浮马达", 599L));

        //1.准备request对象
        BulkRequest request = new BulkRequest();

        for (Goods goods : list) {
            //1.准备Request对象
            //IndexRequest request2 = new IndexRequest("goods");

            //2.给Request对象准备请求对象
            //request2.id(goods.getId().toString());
            //把goods对象转成json
            //request2.source(JSON.toJSONString(goods), XContentType.JSON);

            //此处add添加IndexRequest对象
            //request.add(request2);

            //链式编程替代上述代码
            request.add(
                    new IndexRequest("goods")
                            .id(goods.getId().toString())
                            .source(JSON.toJSONString(goods), XContentType.JSON));
        }


        //bulk不处理业务逻辑
        client.bulk(request, RequestOptions.DEFAULT);

    }


    /*
     * SearchSourceBuilder-->用来构建搜索所需要的一切条件，包括：
     *   query,sort,from,size,highlight,aggs,sugguest
     *   SearchSourceBuilder 相当于_search后面整个json数组中的对象
     *
     *   QueryBuilders --> query
     *   HighlightBuilder-->highlight
     *   AggregationBuilders-->aggs
     *   SuggestionBuilder-->suggest
     *   */

    @Test
    public void testSearch() throws IOException {

        //对应search后的json数组
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //query查询
        sourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("title", "手机"))
                .must(QueryBuilders.rangeQuery("price").gte(0).lte(20000)));
        //sort
        sourceBuilder.sort("price", SortOrder.ASC);
        //分页
        sourceBuilder.from(0).size(5);
        //高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title"));
        //1.准备request
        SearchRequest searchRequest = new SearchRequest("goods");

        //2.准备请求参数
        searchRequest.source(sourceBuilder);


        //3.发送请求
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

        //4.解析结果
        //对应的结果
        /*{
  "took" : 318,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 2,
      "relation" : "eq"
    },
    "max_score" : null,
    "hits" : [
      {
        "_index" : "goods",
        "_type" : "_doc",
        "_id" : "2",
        "_score" : null,
        "_source" : { },
        "highlight" : {
          "title" : [
            "三星 Galaxy A90 <em>手机</em> 数码 疾速5G 骁龙855"
          ]
        },
        "sort" : [
          3099
        ]
      },
      {
        "_index" : "goods",
        "_type" : "_doc",
        "_id" : "1",
        "_score" : null,
        "_source" : { },
        "highlight" : {
          "title" : [
            "红米9<em>手机</em> 数码"
          ]
        },
        "sort" : [
          1499
        ]
      }
    ]
  },
  "suggest" : {
    "name_suggestion" : [
      {
        "text" : "x",
        "offset" : 0,
        "length" : 1,
        "options" : [ ]
      }
    ]
  }
}*/
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


        //聚合查询
        @Test
        public void testAggs () throws IOException {
        //代码整体右移 tab   , 整体左移shift+tab
                /*#对my_index进行聚合查询
                #对my_index进行聚合查询
                GET /my_index/_search
                {
                  "aggs": {
                    "groupAgg": {
                      "terms": {
                        "field": "group",
                        "size": 10
                      }
                    },
                    "groupAgg01": {
                      "terms": {
                        "field": "group",
                        "size": 10
                      }
                    }
                  }
                }*/

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            //aggregation 相当于 ：  "aggs":
            // AggregationBuilders 构建  AggregationBuilder
            //三要素：聚合名称，聚合类型，聚合字段
            sourceBuilder.aggregation(AggregationBuilders.terms("groupAgg").field("group"));

            //1.准备request
            SearchRequest searchRequest = new SearchRequest("my_index");

            //2.准备请求参数
            searchRequest.source(sourceBuilder);


            //3.发送请求
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            //kibana中的结果
             /*
                                 * {
                      "took" : 9,
                      "timed_out" : false,
                      "_shards" : {
                        "total" : 1,
                        "successful" : 1,
                        "skipped" : 0,
                        "failed" : 0
                      },
                      "hits" : {
                        "total" : {
                          "value" : 4,
                          "relation" : "eq"
                        },
                        "max_score" : 1.0,
                        "hits" : [
                          {
                            "_index" : "my_index",
                            "_type" : "_doc",
                            "_id" : "1",
                            "_score" : 1.0,
                            "_source" : {
                              "group" : "heima115",
                              "user" : [
                                {
                                  "first" : "John",
                                  "last" : "Smith"
                                },
                                {
                                  "first" : "Alice",
                                  "last" : "White"
                                }
                              ]
                            }
                          },
                          {
                            "_index" : "my_index",
                            "_type" : "_doc",
                            "_id" : "3",
                            "_score" : 1.0,
                            "_source" : {
                              "group" : "heima117",
                              "user" : [
                                {
                                  "first" : "Rose",
                                  "last" : "Lee"
                                },
                                {
                                  "first" : "Jack",
                                  "last" : "Chen"
                                }
                              ]
                            }
                          },
                          {
                            "_index" : "my_index",
                            "_type" : "_doc",
                            "_id" : "4",
                            "_score" : 1.0,
                            "_source" : {
                              "group" : "heima117",
                              "user" : [
                                {
                                  "first" : "Bruce",
                                  "last" : "Lee"
                                },
                                {
                                  "first" : "Jack",
                                  "last" : "Ma"
                                }
                              ]
                            }
                          },
                          {
                            "_index" : "my_index",
                            "_type" : "_doc",
                            "_id" : "2",
                            "_score" : 1.0,
                            "_source" : {
                              "group" : "heima116",
                              "user" : [
                                {
                                  "first" : "John",
                                  "last" : "White"
                                },
                                {
                                  "first" : "Alice",
                                  "last" : "Smith"
                                }
                              ]
                            }
                          }
                        ]
                      },
                      "aggregations" : {
                        "groupAgg01" : {
                          "doc_count_error_upper_bound" : 0,
                          "sum_other_doc_count" : 0,
                          "buckets" : [
                            {
                              "key" : "heima117",
                              "doc_count" : 2
                            },
                            {
                              "key" : "heima115",
                              "doc_count" : 1
                            },
                            {
                              "key" : "heima116",
                              "doc_count" : 1
                            }
                          ]
                        },
                        "groupAgg" : {
                          "doc_count_error_upper_bound" : 0,
                          "sum_other_doc_count" : 0,
                          "buckets" : [
                            {
                              "key" : "heima117",
                              "doc_count" : 2
                            },
                            {
                              "key" : "heima115",
                              "doc_count" : 1
                            },
                            {
                              "key" : "heima116",
                              "doc_count" : 1
                            }
                          ]
                        }
                      }
                    }*/

            //4. 聚合结果
            Aggregations aggregations = response.getAggregations();


            // 4.1 根据名称获取聚合结果
            // 向下转型  Aggregation groupAgg = aggregations.get("groupAgg");

            Terms groupAgg = aggregations.get("groupAgg");


            List<? extends Terms.Bucket> list = groupAgg.getBuckets();

            for (Terms.Bucket bucket : list) {
                String key = bucket.getKeyAsString();
                System.out.println("key = " + key);
                long docCount = bucket.getDocCount();
                System.out.println("docCount = " + docCount);
            }


        }

        //nested 类型聚合
            /*
            * GET /my_index/_search
        {
          "size": 0,
          "_source": "{}",
          "aggs": {
            "nAgg": {
              "nested": {
                "path": "user"
              },
              "aggs": {
                "lastAgg":{
                  "terms": {
                    "field": "user.last",
                    "size": 10
                  },
                  "aggs": {
                    "firstAgg": {
                      "terms": {
                        "field": "user.first",
                        "size": 10
                      }
                    }
                  }
                }
              }
            }
          }
        }*/

            //结果
            /*
            * {
          "took" : 13,
          "timed_out" : false,
          "_shards" : {
            "total" : 1,
            "successful" : 1,
            "skipped" : 0,
            "failed" : 0
          },
          "hits" : {
            "total" : {
              "value" : 4,
              "relation" : "eq"
            },
            "max_score" : null,
            "hits" : [ ]
          },
          "aggregations" : {
            "nAgg" : {
              "doc_count" : 8,
              "lastAgg" : {
                "doc_count_error_upper_bound" : 0,
                "sum_other_doc_count" : 0,
                "buckets" : [
                  {
                    "key" : "Lee",
                    "doc_count" : 2,
                    "firstAgg" : {
                      "doc_count_error_upper_bound" : 0,
                      "sum_other_doc_count" : 0,
                      "buckets" : [
                        {
                          "key" : "Bruce",
                          "doc_count" : 1
                        },
                        {
                          "key" : "Rose",
                          "doc_count" : 1
                        }
                      ]
                    }
                  },
                  {
                    "key" : "Smith",
                    "doc_count" : 2,
                    "firstAgg" : {
                      "doc_count_error_upper_bound" : 0,
                      "sum_other_doc_count" : 0,
                      "buckets" : [
                        {
                          "key" : "Alice",
                          "doc_count" : 1
                        },
                        {
                          "key" : "John",
                          "doc_count" : 1
                        }
                      ]
                    }
                  },
                  {
                    "key" : "White",
                    "doc_count" : 2,
                    "firstAgg" : {
                      "doc_count_error_upper_bound" : 0,
                      "sum_other_doc_count" : 0,
                      "buckets" : [
                        {
                          "key" : "Alice",
                          "doc_count" : 1
                        },
                        {
                          "key" : "John",
                          "doc_count" : 1
                        }
                      ]
                    }
                  },
                  {
                    "key" : "Chen",
                    "doc_count" : 1,
                    "firstAgg" : {
                      "doc_count_error_upper_bound" : 0,
                      "sum_other_doc_count" : 0,
                      "buckets" : [
                        {
                          "key" : "Jack",
                          "doc_count" : 1
                        }
                      ]
                    }
                  },
                  {
                    "key" : "Ma",
                    "doc_count" : 1,
                    "firstAgg" : {
                      "doc_count_error_upper_bound" : 0,
                      "sum_other_doc_count" : 0,
                      "buckets" : [
                        {
                          "key" : "Jack",
                          "doc_count" : 1
                        }
                      ]
                    }
                  }
                ]
              }
            }
          }
        }*/


        @Test
        public void testNestedAggs () throws IOException {

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            //三层嵌套
            sourceBuilder.aggregation(AggregationBuilders.nested("nAgg", "user")
            .subAggregation(AggregationBuilders.terms("lastAgg").field("user.last")
            .subAggregation(AggregationBuilders.terms("firstAgg").field("user.first"))));

            //1.准备request
            SearchRequest searchRequest = new SearchRequest("my_index");

            //2.准备请求参数
            searchRequest.source(sourceBuilder);


            //3.发送请求
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);


            //接续结果
            Aggregations aggregations = response.getAggregations();


            // 向下转型  Aggregation groupAgg = aggregations.get("groupAgg");

            Nested nAgg = aggregations.get("nAgg");

            //获取lastName的集合
            Terms lastAgg = nAgg.getAggregations().get("lastAgg");
            List<? extends Terms.Bucket> buckets = lastAgg.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                String keyAsString = bucket.getKeyAsString();
                System.out.println("firstName = " + keyAsString);
                long docCount = bucket.getDocCount();
                System.out.println("docCount = " + docCount);

                //获取lastName的集合
                Terms firstAgg = bucket.getAggregations().get("firstAgg");
                List<? extends Terms.Bucket> firstBuckets = firstAgg.getBuckets();
                for (Terms.Bucket firstBucket : firstBuckets) {
                    String keyAsString1 = firstBucket.getKeyAsString();
                    System.out.println("\t lastName = " + keyAsString1);
                    long docCount1 = firstBucket.getDocCount();
                    System.out.println("\t docCount1 = " + docCount1);
                }
            }


        }


        //suggest查询  属于search的一部分

            /*GET /goods/_search
        {
          "suggest": {
            "name_suggestion": {
              "prefix": "s",
              "completion": {
                "field": "name"
              }
            }
          }
        }*/
        @Test
        public void testSuggest () throws IOException {

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            //除了 new SuggestBuilder() 也可也用suggestBuilders
            //SuggestBuilders.completionSuggestion()
            //new SuggestBuilder().addSuggestion(, )
            ///suggest表示自动补全
            sourceBuilder.suggest(new SuggestBuilder().addSuggestion("name_suggestion", SuggestBuilders.completionSuggestion("name").prefix("s")));

            //1.准备request
            SearchRequest searchRequest = new SearchRequest("goods");

            //2.准备请求参数
            searchRequest.source(sourceBuilder);


            //3.发送请求
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

                            /*
                            * {
                  "took" : 5,
                  "timed_out" : false,
                  "_shards" : {
                    "total" : 1,
                    "successful" : 1,
                    "skipped" : 0,
                    "failed" : 0
                  },
                  "hits" : {
                    "total" : {
                      "value" : 0,
                      "relation" : "eq"
                    },
                    "max_score" : null,
                    "hits" : [ ]
                  },
                  "suggest" : {
                    "name_suggestion" : [
                      {
                        "text" : "s",
                        "offset" : 0,
                        "length" : 1,
                        "options" : [
                          {
                            "text" : "手机",
                            "_index" : "goods",
                            "_type" : "_doc",
                            "_id" : "1",
                            "_score" : 1.0,
                            "_source" : {
                              "id" : 1,
                              "name" : [
                                "红米9",
                                "手机"
                              ],
                              "price" : 1499,
                              "title" : "红米9手机 数码"
                            }
                          },
                          {
                            "text" : "手机",
                            "_index" : "goods",
                            "_type" : "_doc",
                            "_id" : "2",
                            "_score" : 1.0,
                            "_source" : {
                              "id" : 2,
                              "name" : [
                                "Galaxy",
                                "手机"
                              ],
                              "price" : 3099,
                              "title" : "三星 Galaxy A90 手机 数码 疾速5G 骁龙855"
                            }
                          },
                          {
                            "text" : "数码",
                            "_index" : "goods",
                            "_type" : "_doc",
                            "_id" : "3",
                            "_score" : 1.0,
                            "_source" : {
                              "id" : 3,
                              "name" : [
                                "Sony",
                                "WH-1000XM3",
                                "数码"
                              ],
                              "price" : 2299,
                              "title" : "Sony WH-1000XM3 降噪耳机 数码"
                            }
                          }
                        ]
                      }
                    ],
                    "name_suggestion1" : [
                      {
                        "text" : "s",
                        "offset" : 0,
                        "length" : 1,
                        "options" : [
                          {
                            "text" : "手机",
                            "_index" : "goods",
                            "_type" : "_doc",
                            "_id" : "1",
                            "_score" : 1.0,
                            "_source" : {
                              "id" : 1,
                              "name" : [
                                "红米9",
                                "手机"
                              ],
                              "price" : 1499,
                              "title" : "红米9手机 数码"
                            }
                          },
                          {
                            "text" : "手机",
                            "_index" : "goods",
                            "_type" : "_doc",
                            "_id" : "2",
                            "_score" : 1.0,
                            "_source" : {
                              "id" : 2,
                              "name" : [
                                "Galaxy",
                                "手机"
                              ],
                              "price" : 3099,
                              "title" : "三星 Galaxy A90 手机 数码 疾速5G 骁龙855"
                            }
                          },
                          {
                            "text" : "数码",
                            "_index" : "goods",
                            "_type" : "_doc",
                            "_id" : "3",
                            "_score" : 1.0,
                            "_source" : {
                              "id" : 3,
                              "name" : [
                                "Sony",
                                "WH-1000XM3",
                                "数码"
                              ],
                              "price" : 2299,
                              "title" : "Sony WH-1000XM3 降噪耳机 数码"
                            }
                          }
                        ]
                      }
                    ]
                  }
                }*/


            //解析结果
            //结果在suggest字段下，所以用getSuggest()
            Suggest suggest = response.getSuggest();

            //根据名称获取补全结果
             CompletionSuggestion nameSuggestion = suggest.getSuggestion("name_suggestion");
            List<CompletionSuggestion.Entry.Option> options = nameSuggestion.getOptions();

            List<String> optionList = options.stream()
                    //获取option中的text
                    .map(Suggest.Suggestion.Entry.Option::getText).map(Text::toString)
                    //把text变为String
                    .collect(Collectors.toList());

            optionList.forEach(System.out::println);


        }


    }


