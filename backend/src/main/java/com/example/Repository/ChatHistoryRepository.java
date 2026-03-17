package com.example.Repository;

import java.util.List;

public interface ChatHistoryRepository {
    /**
     * 保存会话记录
     * @param type 业务类型，如：chat、service、pdf
     * @param chatId 会话ID
     */
    void save(String type,String chatId,Long userId);

    /**
     * 获取会话ID列表
     * @param type 业务类型，如：chat、service、pdf
     * @return 会话ID列表
     */
    List<String> getChatIds(String type);

    /**
     * 删除会话
     * @param type 业务类型，如：chat、service、pdf
     * @param sessionId 会话ID
     * @param userId 用户ID
     *
     **/

    void delete(String type,String sessionId,Long userId);
}
