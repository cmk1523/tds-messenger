package com.techdevsolutions.messenger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.techdevsolutions.common.dao.DaoCrudInterface;
import com.techdevsolutions.common.service.core.AESEncryptionService;
import com.techdevsolutions.common.service.core.Timer;
import com.techdevsolutions.messenger.beans.auditable.Message;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MessageEncryptedServiceImpl extends MessageServiceImpl {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected String SYSTEM_KEY = "8bUCjrgig7RyR08h6HAf";
    protected AESEncryptionService encryptionService = new AESEncryptionService();
    protected ObjectMapper objectMapper = new ObjectMapper();

    public MessageEncryptedServiceImpl(Environment environment, DaoCrudInterface<Message> dao) {
        super(dao);
        objectMapper.registerModule(new Jdk8Module());
        this.SYSTEM_KEY = environment != null ? environment.getProperty("message.service.key") : this.SYSTEM_KEY;
    }

    @Override
    public Message get(final String id) throws Exception {
        Timer timer = new Timer().start();

        if (StringUtils.isEmpty(id)) {
            throw new Exception("id is null or empty");
        }

        Message item = this.dao.get(id);
        Message decrypted = this.decryptMessage(item, item.getKey().isPresent() ? item.getKey().get() : this.SYSTEM_KEY);
        this.logger.info("Got item by ID: " + id + " in " + timer.stopAndGetDiff() + " ms");
        return decrypted;
    }

    @Override
    public Message create(final Message item) throws Exception {
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

        Message encrypted = this.encryptMessage(item, item.getKey().isPresent() ? item.getKey().get() : this.SYSTEM_KEY);
        Message created = this.dao.create(encrypted);
        // This guarantees item is created
        // Message created = this.get(item.getId());
        this.logger.info("Created item by ID: " + encrypted.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return created;
    }

    @Override
    public Message update(final Message item) throws Exception {
        Timer timer = new Timer().start();

        Set<ConstraintViolation<Message>> violations = this.validator.validate(item);

        if (violations.size() > 0) {
            throw new Exception("Invalid item: " + violations.toString());
        }

        Message encrypted = this.encryptMessage(item, item.getKey().isPresent() ? item.getKey().get() : this.SYSTEM_KEY);
        Message updated = this.dao.update(encrypted);
        // This guarantees item is created
        // Message updated = this.get(item.getId());
        this.logger.info("Updated item by ID: " + updated.getId() + " in " + timer.stopAndGetDiff() + " ms");
        return updated;
    }

    public Message encryptMessage(final Message message, final String key) throws Exception {
        byte[] iv = this.encryptionService.generateRandomBytes();
        byte[] salt = this.encryptionService.generateRandomBytes();
        return this.encryptMessage(message, key, iv, salt);
    }

    public Message encryptMessage(final Message message, final String key, final byte[] iv, final byte[] salt) throws Exception {
        String str = this.objectMapper.writeValueAsString(message);
        Message copy = this.objectMapper.readValue(str, Message.class);
        String newText = this.encryptionService.encrypt(copy.getMessage(), key, iv, salt);
        copy.setMessage(newText);
        return copy;
    }

    public Message decryptMessage(final Message message, final String key) throws Exception {
        String str = this.objectMapper.writeValueAsString(message);
        Message copy = this.objectMapper.readValue(str, Message.class);
        String newText = this.encryptionService.decrypt(copy.getMessage(), key);
        copy.setMessage(newText);
        return copy;
    }
}
