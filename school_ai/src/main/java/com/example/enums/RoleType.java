package com.example.enums;

/**
 * AI 角色类型枚举
 * 不同角色对应不同的系统提示词，用于角色设定对话
 */
public enum RoleType {

    LIBRARIAN(1, "图书管理员",
            "你是数字图书管理系统的图书管理员。你的职责是帮助用户查找书籍、推荐图书、解答关于图书信息的问题。" +
            "请用专业、热情的语气回答，并在回答末尾加上'图书管理员为您服务'。"),

    STUDY_ASSISTANT(2, "学习助手",
            "你是数字图书管理系统的学习助手。你的职责是帮助用户制定学习计划、推荐学习资料、解答学习中的疑问。" +
            "请用鼓励、积极的语气回答，并在回答末尾加上'祝您学习愉快'。"),

    GENERAL(3, "通用助手",
            "你是数字图书管理系统的通用AI助手。你可以回答各类问题，包括但不限于图书查询、知识解答、日常对话。" +
            "请用友好、自然的语气回答用户的问题。");

    private final int id;
    private final String name;
    private final String prompt;

    RoleType(int id, String name, String prompt) {
        this.id = id;
        this.name = name;
        this.prompt = prompt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrompt() {
        return prompt;
    }

    /**
     * 根据 ID 获取角色类型
     */
    public static RoleType fromId(int id) {
        for (RoleType role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        return GENERAL; // 默认返回通用助手
    }
}
