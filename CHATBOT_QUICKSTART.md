# 🚀 QUICK START - RAG Chatbot

## ⚡ Setup trong 5 phút

### Bước 1: Start Docker Services
```bash
cd BE-Nice-develop
docker-compose up -d
```

✅ Check services:
```bash
docker ps
# Should see: postgres (pgvector), redis, mailhog
```

### Bước 2: Get FREE Gemini API Key

1. Mở: https://makersuite.google.com/app/apikey
2. Đăng nhập Google
3. Click "Create API Key"
4. Copy key

### Bước 3: Configure

Mở `src/main/resources/application.properties`:

```properties
gemini.api.key=YOUR_API_KEY_HERE
```

### Bước 4: Run Backend

```bash
.\mvnw.cmd spring-boot:run
```

Đợi cho đến khi thấy:
```
✅ Embedding Model initialized successfully
✅ Google Gemini Chat Model initialized successfully  
🌱 Starting data seeding for chatbot...
✅ Indexed 120 products
✅ Indexed 6 FAQs
✅ Indexed 2 policies
Started WebprojrctApplication in X seconds
```

### Bước 5: Test

#### Test 1: Health Check
```bash
curl http://localhost:8080/api/v1/chat/health
```

Expected:
```json
{
  "status": "UP",
  "message": "Chatbot service is running"
}
```

#### Test 2: Chat
```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Có giày chạy bộ nào không?",
    "sessionId": "test-001"
  }'
```

Expected: Bot trả lời về các sản phẩm giày chạy bộ có trong database

#### Test 3: Get Stats
```bash
curl http://localhost:8080/api/v1/chat/admin/stats
```

Expected:
```json
{
  "total_documents": 128,
  "pdf_documents": 0,
  "product_documents": 120,
  "faq_documents": 6
}
```

## ✅ Success!

Chatbot đã sẵn sàng! 

### Next Steps:

1. **Frontend Integration**: Xem [CHATBOT_README.md](CHATBOT_README.md) section "API Endpoints"
2. **Add PDF Documents**: Upload PDF qua endpoint `/api/v1/chat/admin/upload-pdf`
3. **Customize Prompts**: Edit `RAGService.java` method `buildPrompt()`

## 🐛 Troubleshooting

### Error: "Connection refused to localhost:5432"
```bash
docker-compose restart postgres
# Wait 10 seconds
.\mvnw.cmd spring-boot:run
```

### Error: "Connection refused to localhost:6379"  
```bash
docker-compose restart redis
```

### Warning: "Gemini API key not configured"
- Bot vẫn hoạt động với fallback responses
- Để enable AI: thêm Gemini API key vào application.properties

### No products in vector store
```bash
# Check database
docker exec -it cps_postgres psql -U cps_user -d cps_db -c "SELECT COUNT(*) FROM vector_documents;"
```

## 📱 Test trên Android

Gọi API từ Android app:

```kotlin
val request = ChatRequest(
    message = "Có giày Jordan 1 không?",
    sessionId = UUID.randomUUID().toString()
)

// POST http://10.0.2.2:8080/api/v1/chat (trong emulator)
// POST http://YOUR_IP:8080/api/v1/chat (trên device thật)
```

## 🎯 What's Next?

- [ ] Add more products to database
- [ ] Upload product manuals as PDFs
- [ ] Customize chatbot personality
- [ ] Add WebSocket for real-time chat
- [ ] Deploy to production

---

**Questions?** Check [CHATBOT_README.md](CHATBOT_README.md) for full documentation.
