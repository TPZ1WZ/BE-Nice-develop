-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create vector_documents table for storing embeddings
CREATE TABLE IF NOT EXISTS vector_documents (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    metadata JSONB,
    embedding vector(384), -- all-MiniLM-L6-v2 produces 384-dimensional vectors
    source VARCHAR(255),
    source_type VARCHAR(50), -- 'pdf', 'product', 'faq', 'policy'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for vector similarity search (HNSW is faster than IVFFlat for most cases)
CREATE INDEX IF NOT EXISTS vector_documents_embedding_idx 
ON vector_documents USING hnsw (embedding vector_cosine_ops);

-- Create index for metadata queries
CREATE INDEX IF NOT EXISTS vector_documents_metadata_idx 
ON vector_documents USING GIN (metadata);

-- Create index for source_type filtering
CREATE INDEX IF NOT EXISTS vector_documents_source_type_idx 
ON vector_documents (source_type);

-- Create chat_conversations table
CREATE TABLE IF NOT EXISTS chat_conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create chat_messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT REFERENCES chat_conversations(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL, -- 'user', 'assistant', 'system'
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for conversation queries
CREATE INDEX IF NOT EXISTS chat_messages_conversation_idx 
ON chat_messages (conversation_id, created_at DESC);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for vector_documents
CREATE TRIGGER update_vector_documents_updated_at
    BEFORE UPDATE ON vector_documents
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for chat_conversations
CREATE TRIGGER update_chat_conversations_updated_at
    BEFORE UPDATE ON chat_conversations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create function for vector similarity search
CREATE OR REPLACE FUNCTION search_similar_documents(
    query_embedding vector(384),
    match_threshold FLOAT DEFAULT 0.5,
    match_count INT DEFAULT 5,
    filter_source_type VARCHAR DEFAULT NULL
)
RETURNS TABLE (
    id BIGINT,
    content TEXT,
    metadata JSONB,
    source VARCHAR,
    source_type VARCHAR,
    similarity FLOAT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        vd.id,
        vd.content,
        vd.metadata,
        vd.source,
        vd.source_type,
        1 - (vd.embedding <=> query_embedding) AS similarity
    FROM vector_documents vd
    WHERE (filter_source_type IS NULL OR vd.source_type = filter_source_type)
        AND 1 - (vd.embedding <=> query_embedding) > match_threshold
    ORDER BY vd.embedding <=> query_embedding
    LIMIT match_count;
END;
$$ LANGUAGE plpgsql;
