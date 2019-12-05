package com.techdevsolutions.messenger.service;

import com.techdevsolutions.common.dao.DaoCrudInterface;
import com.techdevsolutions.common.service.core.Timer;
import com.techdevsolutions.messenger.beans.auditable.Message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MessageServiceImpl implements MessageService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private DaoCrudInterface<Message> dao;
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public MessageServiceImpl(DaoCrudInterface<Message> dao) {
        this.dao = dao;
    }

    @Override
    public List<Message> search() throws Exception {
        throw new Exception("Method not implemented");
    }

    @Override
    public List<Message> getAll() throws Exception {
        throw new Exception("Method not implemented");
    }

    @Override
    public Message get(String id) throws Exception {
        Timer timer = new Timer().start();

        if (StringUtils.isEmpty(id)) {
            throw new Exception("id is null or empty");
        }

        Message item = this.dao.get(id);
        this.logger.info("Got item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
        return item;
    }

    @Override
    public Message create(Message item) throws Exception {
        Timer timer = new Timer().start();

        if (item != null) {
            item.setId(UUID.randomUUID().toString());

            if (item.getCreated() == null) {
                item.setCreated(new Date().getTime());
            }
        }

        Set<ConstraintViolation<Message>> violations = this.validator.validate(item);

        if (violations.size() > 0) {
            throw new Exception("Invalid item: " + violations.toString());
        }

        Message created = this.dao.create(item);
        // This guarantees item is created
        // Message created = this.get(item.getId());
        this.logger.info("Created item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return created;
    }

    @Override
    public void remove(String id) throws Exception {
        Timer timer = new Timer().start();

        if (StringUtils.isEmpty(id)) {
            throw new Exception("id is null or empty");
        }

        this.dao.remove(id);
        // This guarantees item is removed
//        this.dao.verifyRemoval(id);
        this.logger.info("Removed item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
    }

    @Override
    public void delete(String id) throws Exception {
        this.remove(id);
    }

    @Override
    public Message update(Message item) throws Exception {
        Timer timer = new Timer().start();

        Set<ConstraintViolation<Message>> violations = this.validator.validate(item);

        if (violations.size() > 0) {
            throw new Exception("Invalid item: " + violations.toString());
        }

        Message updated = this.dao.update(item);
        // This guarantees item is created
        // Message updated = this.get(item.getId());
        this.logger.info("Updated item by ID: " + item.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return updated;
    }

    @Override
    public void install() throws Exception {
        throw new Exception("Method not implemented");
    }
}
