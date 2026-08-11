package com.example.service;

import java.util.Map;

/**
 * 邮件服务接口，定义了所有邮件发送方法。
 */
public interface EmailService {

    /**
     * 发送纯文本邮件
     * @param to      收件人地址
     * @param subject 主题
     * @param content 邮件正文（纯文本）
     */
    void sendSimpleMail(String to, String subject, String content);

    /**
     * 发送 HTML 格式邮件
     * @param to      收件人地址
     * @param subject 主题
     * @param content 邮件正文（HTML 内容）
     */
    void sendHtmlMail(String to, String subject, String content);

    /**
     * 发送带附件的邮件（正文支持 HTML）
     * @param to       收件人地址
     * @param subject  主题
     * @param content  邮件正文（HTML 或纯文本）
     * @param filePath 本地附件文件绝对路径
     */
    void sendAttachmentMail(String to, String subject, String content, String filePath);

    /**
     * 发送模板邮件（使用 Thymeleaf 渲染）
     * @param to           收件人地址
     * @param subject      主题
     * @param templateName 模板名称（不含后缀，如 "welcome"）
     * @param variables    模板变量
     */
    void sendTemplateMail(String to, String subject, String templateName, Map<String, Object> variables);
}