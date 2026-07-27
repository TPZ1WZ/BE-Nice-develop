# 🤖 Nike Store RAG Chatbot

## Tổng quan

Chatbot AI thông minh sử dụng công nghệ RAG (Retrieval-Augmented Generation) để tư vấn khách hàng về sản phẩm Nike.

## 🌟 Tính năng

### ✅ Đã hoàn thành:
- ✨ **Vector Database**: pgvector trên PostgreSQL để lưu embeddings
- 🧠 **Embedding Model**: all-MiniLM-L6-v2 (384 dimensions, chạy local)
- 🤖 **LLM**: Google Gemini 1.5 Flash (miễn phí 1500 requests/ngày)
- 📄 **PDF Processing**: Đọc và xử lý file PDF, tự động chunking
- 💾 **Cache**: Redis cache cho câu hỏi thường gặp
- 🔍 **Vector Search**: Tìm kiếm similarity với HNSW index
- 💬 **Chat History**: Lưu lịch sử hội thoại
- 🎯 **Context-Aware**: Trả lời dựa trên context từ database

## 🏗️ Kiến trúc

```
┌─────────────┐
│   User      │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│   ChatController (REST API)     │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│      RAGService                 │
│  ┌──────────────────────────┐  │
│  │ 1. Embed Query           │  │
│  │ 2. Vector Search         │  │
│  │ 3. Build Context         │  │
│  │ 4. Generate Response     │  │
│  └──────────────────────────┘  │
└────┬──────────────┬─────────────┘
     │              │
     ▼              ▼
┌──────────┐   ┌──────────┐
│ Embedding│   │   LLM    │
│  Service │   │ Service  │
└────┬─────┘   └────┬─────┘
     │              │
     ▼              ▼
┌──────────────────────────────┐
│   pgvector (PostgreSQL)      │
│   + HNSW Index               │
└──────────────────────────────┘
```

## 🚀 Cài đặt

### 1. Start Docker Services

```bash
cd BE-Nice-develop
docker-compose up -d
```

Dịch vụ sẽ chạy:
- PostgreSQL with pgvector: `localhost:5432`
- Redis: `localhost:6379`
- MailHog: `localhost:8025`

### 2. Cấu hình Google Gemini API (Miễn phí)

1. Truy cập: https://makersuite.google.com/app/apikey
2. Đăng nhập Google
3. Tạo API Key (FREE - không cần thẻ tín dụng)
4. Copy API key và thêm vào `application.properties`:

```properties
gemini.api.key=YOUR_API_KEY_HERE
```

### 3. Build và Run

```bash
./mvnw.cmd spring-boot:run
```

Backend sẽ chạy ở `http://localhost:8080`

### 4. Tự động Seed Data

Khi khởi động, hệ thống tự động:
- ✅ Embed tất cả sản phẩm vào vector DB
- ✅ Thêm FAQs
- ✅ Thêm chính sách

## 📡 API Endpoints

### Chat với Bot

```bash
POST /api/v1/chat
Content-Type: application/json

{
  "message": "Có giày chạy bộ nào dưới 2 triệu không?",
  "sessionId": "optional-session-id",
  "userId": 123,  // optional
  "topK": 5       // optional, số lượng documents retrieve
}
```

**Response:**
```json
{
  "message": "Dạ có ạ! Shop đang có Nike Revolution 7 giá 1,850,000đ...",
  "sessionId": "abc-123",
  "sources": [
    {
      "id": 1,
      "content": "Sản phẩm: Nike Revolution 7...",
      "sourceType": "product",
      "similarity": 0.87
    }
  ]
}
```

### Upload PDF (Admin)

```bash
POST /api/v1/chat/admin/upload-pdf
Content-Type: multipart/form-data

file: [PDF file]
sourceType: "manual"  // optional
category: "guide"     // optional
```

### Add Text (Admin)

```bash
POST /api/v1/chat/admin/add-text
Content-Type: application/json

{
  "text": "Hướng dẫn chọn size giày...",
  "source": "size-guide",
  "sourceType": "guide",
  "metadata": {
    "author": "Nike Team"
  }
}
```

### Get Statistics (Admin)

```bash
GET /api/v1/chat/admin/stats
```

**Response:**
```json
{
  "total_documents": 150,
  "pdf_documents": 5,
  "product_documents": 120,
  "faq_documents": 25
}
```

### Get Conversation History

```bash
GET /api/v1/chat/conversations/{sessionId}
```

### Health Check

```bash
GET /api/v1/chat/health
```

## 🧪 Test với Postman/cURL

### Test Chat

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Giày Jordan 1 giá bao nhiêu?",
    "sessionId": "test-123"
  }'
```

### Upload PDF

```bash
curl -X POST http://localhost:8080/api/v1/chat/admin/upload-pdf \
  -F "file=@guide.pdf" \
  -F "sourceType=manual" \
  -F "category=product-guide"
```

## 📊 Database Schema

### vector_documents
- `id`: Primary key
- `content`: Nội dung text
- `embedding`: Vector (384 dimensions)
- `metadata`: JSON metadata
- `source`: Nguồn document
- `source_type`: Loại document (product, faq, pdf, policy)

### chat_conversations
- `id`: Primary key
- `user_id`: Foreign key to users
- `session_id`: Unique session identifier
- `created_at`, `updated_at`: Timestamps

### chat_messages
- `id`: Primary key
- `conversation_id`: Foreign key
- `role`: user, assistant, system
- `content`: Nội dung message
- `metadata`: JSON metadata

## 🎯 Use Cases

### 1. Tư vấn sản phẩm
**User:** "Tôi muốn mua giày chạy bộ"  
**Bot:** "Dạ shop có các dòng giày chạy bộ Nike như Revolution, Pegasus, Air Zoom... Bạn muốn giá bao nhiêu ạ?"

### 2. Hỏi giá và tồn kho
**User:** "Air Max 270 còn size 42 không?"  
**Bot:** "Dạ có ạ! Nike Air Max 270 Black giá 3,200,000đ, hiện còn 8 đôi size 42..."

### 3. Chính sách
**User:** "Đổi trả trong bao lâu?"  
**Bot:** "Nike Store hỗ trợ đổi trả trong 30 ngày kể từ ngày mua..."

## ⚙️ Configuration

### application.properties

```properties
# Redis Cache
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis

# Gemini API
gemini.api.key=YOUR_API_KEY

# Chatbot Settings
chatbot.chunk.size=500
chatbot.chunk.overlap=100
chatbot.retrieval.top-k=5
chatbot.retrieval.similarity-threshold=0.5
```

## 🔧 Troubleshooting

### 1. Không có API key Gemini
- Bot vẫn hoạt động với fallback responses
- Log sẽ báo: `⚠️ Gemini API key not configured`

### 2. PostgreSQL connection failed
```bash
docker-compose restart postgres
```

### 3. Redis connection failed
```bash
docker-compose restart redis
```

### 4. Vector search không trả về kết quả
- Check embedding model đã load: xem log `✅ Embedding Model initialized`
- Check data đã seed: `GET /api/v1/chat/admin/stats`

## 📈 Performance

- **Embedding**: ~50ms/query (local model)
- **Vector Search**: ~10ms (HNSW index)
- **LLM Response**: ~1-3s (Google Gemini)
- **Total**: ~1.5-3.5s per request

## 🔐 Security Notes

⚠️ **Production Recommendations:**
1. Add authentication cho admin endpoints
2. Rate limiting cho chat endpoint
3. Input validation và sanitization
4. HTTPS only
5. Protect Gemini API key trong environment variables

## 📝 TODO / Future Enhancements

- [ ] WebSocket cho real-time chat
- [ ] Sentiment analysis
- [ ] Multi-language support
- [ ] Voice input/output
- [ ] Product recommendation engine
- [ ] A/B testing for responses
- [ ] Analytics dashboard

## 💡 Tips

### Tối ưu hóa Response Quality:
1. Thêm nhiều FAQs vào vector DB
2. Tăng `topK` để retrieve nhiều context hơn
3. Fine-tune similarity threshold
4. Thêm metadata phong phú cho products

### Tiết kiệm Gemini API quota:
1. Sử dụng Redis cache cho câu hỏi phổ biến
2. Implement rate limiting
3. Fallback sang câu trả lời template

## 🤝 Contributing

Để thêm documents mới vào knowledge base:

```java
documentIngestionService.ingestText(
    text,           // Nội dung
    source,         // Tên nguồn
    sourceType,     // Loại: product, faq, policy, pdf
    metadata        // Metadata bổ sung
);
```

## 📞 Support

- Email: support@nikestore.com
- Hotline: 1900-xxxx
- Documentation: https://docs.nikestore.com

---

**Made with ❤️ by Nike Store Team**
