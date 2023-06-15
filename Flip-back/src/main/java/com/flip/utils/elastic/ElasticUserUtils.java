package com.flip.utils.elastic;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.flip.entity.Authority;
import com.flip.entity.Role;
import com.flip.entity.User;
import com.flip.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ElasticUserUtils {

    public static boolean createUserIndex(ElasticsearchClient elasticsearchClient) throws IOException {
        Map<String, Property> map = new HashMap<>();
        map.put("id", Property.of(p -> p.integer(inp -> inp.index(false))));
        map.put("uid", Property.of(p -> p.long_(lnp -> lnp.index(true))));
        map.put("avatar", Property.of(p -> p.keyword(kp -> kp.index(false))));
        map.put("username", Property.of(p -> p.text(tp -> tp.index(true).analyzer("ik_max_word"))));
        map.put("nickname", Property.of(p -> p.text(tp -> tp.index(true).analyzer("ik_max_word"))));
        map.put("email", Property.of(p -> p.keyword(kp -> kp.index(true))));
        map.put("password", Property.of(p -> p.keyword(kp -> kp.index(false))));
        map.put("emailVerified", Property.of(p -> p.boolean_(bp -> bp.index(true))));
        map.put("salt", Property.of(p -> p.keyword(kp -> kp.index(false))));
        map.put("registerIp", Property.of(p -> p.keyword(kp -> kp.index(true))));
        map.put("updateBy", Property.of(p -> p.keyword(kp -> kp.index(false))));
        map.put("createTime", Property.of(p -> p.date(dp -> dp.index(true).format("yyyy-MM-dd HH:mm:ss"))));
        map.put("updateTime", Property.of(p -> p.date(dp -> dp.index(false).format("yyyy-MM-dd HH:mm:ss"))));
        map.put("banned", Property.of(p -> p.boolean_(bp -> bp.index(true))));
        map.put("deleted", Property.of(p -> p.boolean_(bp -> bp.index(true))));
        map.put("authority", Property.of(p -> p.nested(np -> np.enabled(true))));
        map.put("role", Property.of(p -> p.nested(np -> np.enabled(true))));

        CreateIndexResponse indexResponse = elasticsearchClient.indices().create(CreateIndexRequest.of(cir -> cir
                .index("user")
                .mappings(TypeMapping.of(tm -> tm.properties(map)))
                .settings(is -> is.numberOfShards("1").numberOfReplicas("0"))));

        return indexResponse.acknowledged();
    }

    public static boolean insertAllUserByBulkOperation(ElasticsearchClient elasticsearchClient, UserService userService) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            boolean created = createUserIndex(elasticsearchClient);
            if (created) { log.info("已创建user索引到Elasticsearch"); }
        }

        List<User> users = userService.list();
        List<BulkOperation> bulkOperations = new ArrayList<>();

        users.forEach(user -> {
            Role role = userService.loadUserRoleByUid(String.valueOf(user.getUid()));
            user.setRole(role);

            List<String> authorities = userService.loadRoleAuthoritiesByRid(role.getRid());
            Authority authority = new Authority();
            authority.setAuthorities(authorities);
            user.setAuthority(authority);

            user.setPassword(null); //密码不用存储到ES

            bulkOperations.add(BulkOperation.of(bo -> bo.index(io -> io.id(String.valueOf(user.getUid())).document(user))));
        });

        BulkResponse bulkResponse = elasticsearchClient.bulk(br -> br.index("user").operations(bulkOperations));

        return !bulkResponse.errors(); //如果errors是false则代表执行过程没有错误产生，反之则有错误产生，取反后返回
    }

    public static void insertUserToEs(ElasticsearchClient elasticsearchClient, User user) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        elasticsearchClient.index(ir -> ir.index("user").id(String.valueOf(user.getUid())).document(user));
    }

    public static List<Hit<User>> searchUsersByKey(ElasticsearchClient elasticsearchClient, String key) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        SearchResponse<User> searchResponse = elasticsearchClient.search(sr -> sr
                        .index("user")
                        .query(q -> q
                                .bool(bq -> bq
                                        .should(obq -> obq
                                                .wildcard(wq -> wq
                                                        .field("username")
                                                        .value("*" + key + "*")))
                                        .should(obq -> obq
                                                .wildcard(wq -> wq
                                                        .field("nickname")
                                                        .value("*" + key + "*")))))
                        .highlight(hl -> hl
                                .requireFieldMatch(false)
                                .fields("username", hlf -> hlf
                                        .preTags("<span style='color: red;'>")
                                        .postTags("</span>"))
                                .fields("nickname", hlf -> hlf
                                        .preTags("<span style='color: red;'>")
                                        .postTags("</span>")))
                ,
                User.class
        );

        if (searchResponse.timedOut() || searchResponse.hits().total().value() == 0) {
            return null;
        }

        List<Hit<User>> userHits = searchResponse.hits().hits();
        userHits.forEach(userHit -> {
            User user = userHit.source();
            String username = "";
            String nickname = "";
            if (ObjectUtil.isNotNull(userHit.highlight())) {
                if (userHit.highlight().containsKey("username")) username = userHit.highlight().get("username").get(0);
                if (userHit.highlight().containsKey("nickname")) nickname = userHit.highlight().get("nickname").get(0);
            }

            if (ObjectUtil.isNotNull(user)) {
                if (ObjectUtil.isNotEmpty(username)) user.setUsername(username);
                if (ObjectUtil.isNotEmpty(nickname)) user.setNickname(nickname);
            }
        });

        return userHits;
    }

    public static void updateUsernameInEs(ElasticsearchClient elasticsearchClient, User user) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        Map<String, String> map = new HashMap<>();
        map.put("username", user.getUsername());

        elasticsearchClient.update(ur -> ur.index("user").id(String.valueOf(user.getUid())).doc(map), User.class);
    }

    public static void updateUserNicknameInEs(ElasticsearchClient elasticsearchClient, User user) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        Map<String, String> map = new HashMap<>();
        map.put("nickname", user.getNickname());

        elasticsearchClient.update(ur -> ur.index("user").id(String.valueOf(user.getUid())).doc(map), User.class);
    }

    public static void updateUserAvatarInEs(ElasticsearchClient elasticsearchClient, User user) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        Map<String, String> map = new HashMap<>();
        map.put("avatar", user.getAvatar());

        elasticsearchClient.update(ur -> ur.index("user").id(String.valueOf(user.getUid())).doc(map), User.class);
    }

    public static void updateUserEmailInEs(ElasticsearchClient elasticsearchClient, User user) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        Map<String, String> map = new HashMap<>();
        map.put("email", user.getEmail());

        elasticsearchClient.update(ur -> ur.index("user").id(String.valueOf(user.getUid())).doc(map), User.class);
    }

    public static void updateUserEmailVerifiedInEs(ElasticsearchClient elasticsearchClient, User user) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        Map<String, Boolean> map = new HashMap<>();
        map.put("emailVerified", user.getEmailVerified());

        elasticsearchClient.update(ur -> ur.index("user").id(String.valueOf(user.getUid())).doc(map), User.class);
    }

    public static void updateUserRoleInEs(ElasticsearchClient elasticsearchClient, User user) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        Map<String, Role> map = new HashMap<>();
        map.put("role", user.getRole());

        elasticsearchClient.update(ur -> ur.index("user").id(String.valueOf(user.getUid())).doc(map), User.class);
    }

    public static void updateUserAuthorityInEs(ElasticsearchClient elasticsearchClient, User user) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        Map<String, Authority> map = new HashMap<>();
        map.put("authority", user.getAuthority());

        elasticsearchClient.update(ur -> ur.index("user").id(String.valueOf(user.getUid())).doc(map), User.class);
    }

    public static void updateUserBannedStatusInEs(ElasticsearchClient elasticsearchClient, User user) throws IOException {
        boolean indexExist = ElasticUtils.isIndexExist(elasticsearchClient, "user");
        if (!indexExist) {
            createUserIndex(elasticsearchClient);
        }

        Map<String, Boolean> map = new HashMap<>();
        map.put("banned", user.getBanned());

        elasticsearchClient.update(ur -> ur.index("user").id(String.valueOf(user.getUid())).doc(map), User.class);
    }
}
