package com.leyou.es.entity;

/**
 * @Author: Leslie Arnold
 * @Date: 2020/12/15 9:08
 */
public class ElasticConstants {
    public static final String SOURCE_TEMPLATE = "{\n" +
            "  \"settings\": {\n" +
            "    \"analysis\": {\n" +
            "      \"analyzer\": {\n" +
            "        \"my_pinyin\": {\n" +
            "          \"tokenizer\": \"ik_max_word\",\n" +
            "          \"filter\": [\n" +
            "            \"py\"\n" +
            "          ]\n" +
            "        }\n" +
            "      },\n" +
            "      \"filter\": {\n" +
            "        \"py\": {\n" +
            "          \"type\": \"pinyin\",\n" +
            "          \"keep_full_pinyin\": false,\n" +
            "          \"keep_joined_full_pinyin\": true,\n" +
            "          \"keep_original\": true,\n" +
            "          \"limit_first_letter_length\": 16,\n" +
            "          \"remove_duplicated_term\": true,\n" +
            "          \"none_chinese_pinyin_tokenize\": false\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\": {\n" +
            "        \"type\": \"completion\",\n" +
            "        \"analyzer\": \"my_pinyin\",\n" +
            "        \"search_analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"title\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"my_pinyin\",\n" +
            "        \"search_analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"price\":{\n" +
            "        \"type\": \"long\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
