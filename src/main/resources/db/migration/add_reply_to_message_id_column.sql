-- Add reply_to_message_id column to user_staff_chat_messages table
-- This allows staff to reply to specific user messages

ALTER TABLE user_staff_chat_messages
ADD COLUMN reply_to_message_id VARCHAR(255) NULL;

-- Add index for better query performance when finding replies
CREATE INDEX idx_user_staff_chat_messages_reply_to_message_id
ON user_staff_chat_messages(reply_to_message_id);

-- Add comment for documentation
COMMENT ON COLUMN user_staff_chat_messages.reply_to_message_id IS 'ID của tin nhắn gốc mà tin nhắn này đang phản hồi (chỉ áp dụng cho STAFF_TO_USER)';
