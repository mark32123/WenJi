-- 为 ai_chat_session 表添加索引
CREATE INDEX idx_ai_chat_session_user_id ON ai_chat_session(user_id);
CREATE INDEX idx_ai_chat_session_status ON ai_chat_session(status);
CREATE INDEX idx_ai_chat_session_last_active_time ON ai_chat_session(last_active_time);
CREATE INDEX idx_ai_chat_session_user_status_time ON ai_chat_session(user_id, status, last_active_time);

-- 为 ai_chat_message 表添加索引
CREATE INDEX idx_ai_chat_message_chat_id ON ai_chat_message(chat_id);
CREATE INDEX idx_ai_chat_message_create_time ON ai_chat_message(create_time);
CREATE INDEX idx_ai_chat_message_chat_time ON ai_chat_message(chat_id, create_time);
