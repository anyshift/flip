package com.flip.utils.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;

import java.io.IOException;

public class ElasticUtils {
    public static boolean isIndexExist(ElasticsearchClient elasticsearchClient, String index) throws IOException {
        BooleanResponse response = elasticsearchClient.indices().exists(er -> er.index(index));
        return response.value();
    }

    public static boolean isDocumentExistInIndex(ElasticsearchClient elasticsearchClient, String documentId, String index) throws IOException {
        boolean indexExist = isIndexExist(elasticsearchClient, index);
        if (!indexExist) {
            return false;
        }
        GetResponse<Void> getResponse = elasticsearchClient.get(gr -> gr.index(index).id(documentId), Void.class);
        return getResponse.found();
    }

    public static boolean deleteIndexInEs(ElasticsearchClient elasticsearchClient, String index) throws IOException {
        boolean indexExist = isIndexExist(elasticsearchClient, index);
        if (!indexExist) {
            return true;
        }

        DeleteIndexResponse deleteIndexResponse = elasticsearchClient.indices().delete(dir -> dir.index(index));
        return deleteIndexResponse.acknowledged();
    }
}
