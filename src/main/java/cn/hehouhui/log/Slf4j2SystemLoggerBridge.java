/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package cn.hehouhui.log;

import org.slf4j.Logger;

/**
 * slf4j到系统日志的桥接
 */
public class Slf4j2SystemLoggerBridge implements LoggerAdaptor {

    private final Logger logger;

    public Slf4j2SystemLoggerBridge(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(LogLevel level, Throwable e, String msg, Object... args) {
        if (!isEnabled(level)) {
            return;
        }

        switch (level) {
            case TRACE:
                logger.trace(msg, args, e);
                break;
            case DEBUG:
                logger.debug(msg, args, e);
                break;
            case INFO:
                logger.info(msg, args, e);
                break;
            case WARN:
                logger.warn(msg, args, e);
                break;
            case ERROR:
                logger.error(msg, args, e);
                break;
            default:
                throw new RuntimeException(String.format("未知的日志级别：%s", level.name()));
        }
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return switch (level) {
            case TRACE -> logger.isTraceEnabled();
            case DEBUG -> logger.isDebugEnabled();
            case INFO -> logger.isInfoEnabled();
            case WARN -> logger.isWarnEnabled();
            case ERROR -> logger.isErrorEnabled();
            default -> throw new RuntimeException(String.format("未知的日志级别：%s", level.name()));
        };
    }
}
