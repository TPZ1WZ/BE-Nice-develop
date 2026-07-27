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

-- Create chat_conversations table
CREATE TABLE IF NOT EXISTS chat_conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
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
