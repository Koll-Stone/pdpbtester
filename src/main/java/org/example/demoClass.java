package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class demoClass {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void outLog() {
        logger.info("this is info");
        logger.debug("this is debug");
    }
}
