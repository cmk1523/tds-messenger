package com.techdevsolutions.messenger.beans.auditable;

import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.UUID;

public class MessageTest {
    public static Message GenerateTestMessage() {
        Message item = new Message();
        item.setId("test-" + UUID.randomUUID().toString());
        item.setCreated(123L);
        item.setFrom("123");
        item.setTo("456");
        item.setText("test message");
        return item;
    }

    @Test
    public void test() {
        Message item = MessageTest.GenerateTestMessage();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Message>> violations = validator.validate(item);
        Assert.assertTrue(violations.size() == 0);
    }
}
