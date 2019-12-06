package com.techdevsolutions.messenger.service;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
import com.techdevsolutions.common.service.core.AESEncryptionService;
import com.techdevsolutions.messenger.beans.auditable.Message;
import com.techdevsolutions.messenger.beans.auditable.MessageTest;
import com.techdevsolutions.messenger.dao.elasticsearch.ElasticsearchMessageDao;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageEncryptedServiceImplTest {
    private ElasticsearchMessageDao dao = new ElasticsearchMessageDao(null);
    private MessageEncryptedServiceImpl messageService = new MessageEncryptedServiceImpl(null, this.dao);
    private EventElasticsearchDAO eventElasticsearchDAO =
            new EventElasticsearchDAO("localhost", ElasticsearchMessageDao.INDEX_BASE_NAME);
    protected AESEncryptionService encryptionService = new AESEncryptionService();
    protected String ENCRYPTION_KEY = "8bUCjrgig7RyR08h6HAf";

    private List<String> ids = new ArrayList<>();

    @After
    public void after() throws InterruptedException, IOException {
        this.cleanup();
    }

    public void cleanup() throws InterruptedException, IOException {
        Thread.sleep(3000L);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());

        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"event.data.tags.keyword\": {\n" +
                "              \"value\": \"test\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";

        try {
            NamedXContentRegistry namedXContentRegistry = new NamedXContentRegistry(searchModule.getNamedXContents());
            XContent xContent = XContentFactory.xContent(XContentType.JSON);
            XContentParser parser = xContent.createParser(namedXContentRegistry,
                    DeprecationHandler.THROW_UNSUPPORTED_OPERATION, query);
            searchSourceBuilder.parseXContent(parser);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DeleteByQueryRequest request = new DeleteByQueryRequest(ElasticsearchMessageDao.INDEX_BASE_NAME);
        request.setQuery(searchSourceBuilder.query());
        request.setMaxDocs(10000);
        request.setBatchSize(1000);
        request.setScroll(TimeValue.timeValueMinutes(1));
        request.setRefresh(true);

        try {
            this.eventElasticsearchDAO.deleteByQuery(request);
        } catch (Exception ignored) {

        }
    }

    @Test
    public void get() throws Exception {
        Message item = MessageTest.GenerateTestMessage();
        item.setCreated(null);
        Message created = this.messageService.create(item);
        String decrypted = this.encryptionService.decrypt(created.getMessage(), ENCRYPTION_KEY);
        created.setMessage(decrypted);
        this.ids.add(created.getId());
        Assert.assertTrue(StringUtils.isNotEmpty(created.getId()));
        Message verify = this.messageService.get(created.getId());
        Assert.assertTrue(created.equals(verify));
    }

//    @Test
//    public void create() {
//    }

    @Test
    public void update() throws Exception {
        Message item = MessageTest.GenerateTestMessage();
        Message created = this.messageService.create(item);
        String decrypted = this.encryptionService.decrypt(created.getMessage(), ENCRYPTION_KEY);
        created.setMessage(decrypted);
        this.ids.add(created.getId());
        Assert.assertTrue(item.equals(created));

        created.setName("test new name");
        Message updated = this.messageService.update(created);
    }

    @Test
    public void encryptMessage() {
    }

    @Test
    public void encryptMessage1() {
    }

    @Test
    public void decryptMessage() {
    }
}