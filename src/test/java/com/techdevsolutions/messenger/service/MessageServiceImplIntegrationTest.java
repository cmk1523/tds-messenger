package com.techdevsolutions.messenger.service;

import com.techdevsolutions.common.dao.elasticsearch.events.EventElasticsearchDAO;
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

public class MessageServiceImplIntegrationTest {
    private ElasticsearchMessageDao dao = new ElasticsearchMessageDao(null);
    private MessageServiceImpl messageService = new MessageServiceImpl(this.dao);
    private EventElasticsearchDAO eventElasticsearchDAO =
            new EventElasticsearchDAO("localhost", ElasticsearchMessageDao.INDEX_BASE_NAME);

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
                "            \"event.data.text.keyword\": {\n" +
                "              \"value\": \"test message\"\n" +
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

//    @Test
//    public void search() {
//    }
//
//    @Test
//    public void getAll() {
//    }

    @Test
    public void get() throws Exception {
        Message item = MessageTest.GenerateTestMessage();
        item.setCreated(null);
        Message created = this.messageService.create(item);
        this.ids.add(created.getId());
        Assert.assertTrue(StringUtils.isNotEmpty(created.getId()));
        Message verify = this.messageService.get(created.getId());
        Assert.assertTrue(created.equals(verify));
    }

//    @Test
//    public void create() throws Exception {
//        // same as get()
//    }

    @Test
    public void remove() throws Exception {
        Message message = MessageTest.GenerateTestMessage();
        Message created = this.messageService.create(message);
        this.ids.add(created.getId());
        this.messageService.remove(message.getId());
//        this.eventElasticsearchDAO.verifyRemoval(Message.getId(), MessageEvent.CATEGORY, MessageEvent.DATASET);
//
//        try {
//            this.messageService.get(created.getId());
//            Assert.assertTrue(false);
//        } catch (Exception e) {
//            Assert.assertTrue(e.getMessage().contains("Item has been removed"));
//        }
    }

//    @Test
//    public void delete() {
//        // same as remove()
//    }

    @Test
    public void update() throws Exception {
        Message Message = MessageTest.GenerateTestMessage();
        Message created = this.messageService.create(Message);
        this.ids.add(created.getId());
        Assert.assertTrue(Message.equals(created));

        created.setName("test new name");
        Message updated = this.messageService.update(created);
//        this.eventElasticsearchDAO.verifyUpdate(Message.getId(), MessageEvent.CATEGORY, MessageEvent.DATASET);
//
//        Message verify = this.messageService.get(created.getId());
//        Assert.assertTrue(created.equals(verify));
    }

//    @Test
//    public void install() {
//    }
}