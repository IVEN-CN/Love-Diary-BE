# Android 集成文档 - 系统消息序列化

## 1. API 接口说明

### 1.1 获取统一系统消息列表

**接口地址：** `GET /users/lover/messages/unified`

**返回JSON格式：**
```json
{
  "success": true,
  "details": [
    {
      "messageType": "INVITE",
      "fromUserId": 1,
      "fromUserName": "张三",
      "link": "abc123",
      "time": "2025-12-19T10:00:00",
      "hasResponse": true,
      "accepted": false,
      "responseUserId": null,
      "responseUserName": null
    },
    {
      "messageType": "RESPONSE",
      "responseUserId": 2,
      "responseUserName": "李四",
      "link": "xyz789",
      "time": "2025-12-19T10:30:00",
      "accepted": false,
      "fromUserId": null,
      "fromUserName": null,
      "hasResponse": null
    }
  ]
}
```

### 1.2 消息类型说明

#### INVITE（邀请消息）
- **可见对象：** 被邀请人（收到邀请的用户）
- **有效字段：**
  - `messageType`: "INVITE"
  - `fromUserId`: 发起邀请的用户ID
  - `fromUserName`: 发起邀请的用户名
  - `link`: 邀请短链
  - `time`: 邀请创建时间
  - `hasResponse`: 是否已响应（true/false）
  - `accepted`: 接受状态（true=接受，false=拒绝，null=未响应）

#### RESPONSE（响应消息）
- **可见对象：** 邀请发起人
- **有效字段：**
  - `messageType`: "RESPONSE"
  - `responseUserId`: 响应用户ID
  - `responseUserName`: 响应用户名
  - `link`: 邀请短链
  - `time`: 响应时间
  - `accepted`: 响应状态（true=接受，false=拒绝）

---

## 2. Android Kotlin 数据类定义

### 2.1 使用 Gson 序列化

```kotlin
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

// API响应包装类
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("details")
    val details: T?,
    
    @SerializedName("message")
    val message: String?
)

// 统一系统消息
data class UnifiedSystemMessage(
    @SerializedName("messageType")
    val messageType: String, // "INVITE" 或 "RESPONSE"
    
    // 邀请消息字段
    @SerializedName("fromUserId")
    val fromUserId: Long?,
    
    @SerializedName("fromUserName")
    val fromUserName: String?,
    
    // 响应消息字段
    @SerializedName("responseUserId")
    val responseUserId: Long?,
    
    @SerializedName("responseUserName")
    val responseUserName: String?,
    
    // 公共字段
    @SerializedName("link")
    val link: String,
    
    @SerializedName("time")
    val time: String, // ISO 8601 格式: "2025-12-19T10:00:00"
    
    // 状态字段
    @SerializedName("hasResponse")
    val hasResponse: Boolean?,
    
    @SerializedName("accepted")
    val accepted: Boolean?
) {
    // 辅助方法：判断是否为邀请消息
    fun isInviteMessage(): Boolean = messageType == "INVITE"
    
    // 辅助方法：判断是否为响应消息
    fun isResponseMessage(): Boolean = messageType == "RESPONSE"
    
    // 辅助方法：获取显示名称
    fun getDisplayName(): String {
        return when (messageType) {
            "INVITE" -> fromUserName ?: "未知用户"
            "RESPONSE" -> responseUserName ?: "未知用户"
            else -> "未知"
        }
    }
    
    // 辅助方法：获取状态描述
    fun getStatusText(): String {
        return when (messageType) {
            "INVITE" -> when {
                hasResponse == true && accepted == true -> "已接受"
                hasResponse == true && accepted == false -> "已拒绝"
                else -> "待响应"
            }
            "RESPONSE" -> if (accepted == true) "接受了邀请" else "拒绝了邀请"
            else -> "未知状态"
        }
    }
}
```

### 2.2 使用 kotlinx.serialization

```kotlin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("details")
    val details: T? = null,
    
    @SerialName("message")
    val message: String? = null
)

@Serializable
data class UnifiedSystemMessage(
    @SerialName("messageType")
    val messageType: String,
    
    @SerialName("fromUserId")
    val fromUserId: Long? = null,
    
    @SerialName("fromUserName")
    val fromUserName: String? = null,
    
    @SerialName("responseUserId")
    val responseUserId: Long? = null,
    
    @SerialName("responseUserName")
    val responseUserName: String? = null,
    
    @SerialName("link")
    val link: String,
    
    @SerialName("time")
    val time: String,
    
    @SerialName("hasResponse")
    val hasResponse: Boolean? = null,
    
    @SerialName("accepted")
    val accepted: Boolean? = null
)
```

---

## 3. Retrofit API 接口定义

```kotlin
import retrofit2.http.GET
import retrofit2.http.Header

interface LoverApiService {
    
    @GET("users/lover/messages/unified")
    suspend fun getUnifiedSystemMessages(
        @Header("Authorization") token: String
    ): ApiResponse<List<UnifiedSystemMessage>>
}
```

---

## 4. 使用示例

### 4.1 获取系统消息列表

```kotlin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MessageRepository {
    
    private val api: LoverApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://your-api-domain.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoverApiService::class.java)
    }
    
    suspend fun getSystemMessages(token: String): Result<List<UnifiedSystemMessage>> {
        return try {
            val response = api.getUnifiedSystemMessages("Bearer $token")
            if (response.success && response.details != null) {
                Result.success(response.details)
            } else {
                Result.failure(Exception(response.message ?: "获取消息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 4.2 在 ViewModel 中使用

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessageViewModel(
    private val repository: MessageRepository
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<UnifiedSystemMessage>>(emptyList())
    val messages: StateFlow<List<UnifiedSystemMessage>> = _messages
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    fun loadMessages(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getSystemMessages(token)
                .onSuccess { messages ->
                    _messages.value = messages
                }
                .onFailure { error ->
                    // 处理错误
                    println("加载消息失败: ${error.message}")
                }
            _isLoading.value = false
        }
    }
}
```

### 4.3 在 Compose 中显示消息列表

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SystemMessagesScreen(
    viewModel: MessageViewModel
) {
    val messages = viewModel.messages.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {
                items(messages.value) { message ->
                    MessageItem(message = message)
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: UnifiedSystemMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message.getDisplayName(),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = message.getStatusText(),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = message.time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

## 5. WebSocket 消息序列化

### 5.1 WebSocket 消息数据类

```kotlin
@Serializable
data class WebSocketMessage(
    @SerialName("messageType")
    val messageType: String, // "BIND_ACCEPT" 或 "BIND_REJECT"
    
    @SerialName("message")
    val message: LoverBindMessage
)

@Serializable
data class LoverBindMessage(
    @SerialName("fromUserId")
    val fromUserId: Long,
    
    @SerialName("fromUserName")
    val fromUserName: String,
    
    @SerialName("link")
    val link: String?
)
```

### 5.2 WebSocket 消息示例

**接受邀请通知：**
```json
{
  "messageType": "BIND_ACCEPT",
  "message": {
    "fromUserId": 2,
    "fromUserName": "李四",
    "link": null
  }
}
```

**拒绝邀请通知：**
```json
{
  "messageType": "BIND_REJECT",
  "message": {
    "fromUserId": 2,
    "fromUserName": "李四",
    "link": null
  }
}
```

### 5.3 WebSocket 客户端示例（使用 OkHttp）

```kotlin
import okhttp3.*
import kotlinx.serialization.json.Json

class WebSocketManager(
    private val token: String,
    private val onMessageReceived: (WebSocketMessage) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = json.decodeFromString<WebSocketMessage>(text)
                    onMessageReceived(message)
                } catch (e: Exception) {
                    println("解析WebSocket消息失败: ${e.message}")
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket连接失败: ${t.message}")
            }
        })
    }
    
    fun disconnect() {
        webSocket?.close(1000, "正常关闭")
    }
}
```

---

## 6. 时间格式处理

后端返回的时间格式为 ISO 8601：`"2025-12-19T10:00:00"`

### 6.1 解析时间

```kotlin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun parseTime(timeString: String): LocalDateTime {
    return LocalDateTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}
```

### 6.2 格式化显示

```kotlin
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDisplayTime(timeString: String): String {
    val dateTime = LocalDateTime.parse(timeString)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
    return dateTime.format(formatter)
}

// 使用示例
val displayTime = formatDisplayTime("2025-12-19T10:00:00")
// 输出: "2025-12-19 10:00"
```

---

## 7. 完整示例项目结构

```
app/
├── data/
│   ├── api/
│   │   └── LoverApiService.kt
│   ├── model/
│   │   ├── ApiResponse.kt
│   │   ├── UnifiedSystemMessage.kt
│   │   └── WebSocketMessage.kt
│   └── repository/
│       └── MessageRepository.kt
├── ui/
│   ├── viewmodel/
│   │   └── MessageViewModel.kt
│   └── screen/
│       └── SystemMessagesScreen.kt
└── websocket/
    └── WebSocketManager.kt
```

---

## 8. Gradle 依赖配置

```gradle
dependencies {
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // Gson (如果使用 Gson)
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Kotlinx Serialization (如果使用)
    implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0'
    
    // OkHttp WebSocket
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    
    // Compose (可选)
    implementation 'androidx.compose.material3:material3:1.1.2'
}
```

---

## 9. 注意事项

1. **时间处理：** 后端返回的时间是服务器时区，建议转换为本地时区显示
2. **空值处理：** 根据 `messageType` 判断哪些字段有效，避免使用 null 字段
3. **Token 管理：** 确保在请求头中正确携带 `Authorization: Bearer {token}`
4. **WebSocket 重连：** 实现 WebSocket 断线重连机制
5. **消息去重：** 如果同时使用轮询和 WebSocket，注意处理消息去重
6. **缓存策略：** 建议使用 Room 或其他本地存储缓存消息列表

---

## 10. 测试建议

### 10.1 单元测试

```kotlin
import org.junit.Test
import org.junit.Assert.*

class UnifiedSystemMessageTest {
    
    @Test
    fun testIsInviteMessage() {
        val message = UnifiedSystemMessage(
            messageType = "INVITE",
            fromUserId = 1,
            fromUserName = "张三",
            link = "abc123",
            time = "2025-12-19T10:00:00",
            hasResponse = false,
            accepted = null,
            responseUserId = null,
            responseUserName = null
        )
        
        assertTrue(message.isInviteMessage())
        assertFalse(message.isResponseMessage())
    }
    
    @Test
    fun testGetStatusText() {
        val message = UnifiedSystemMessage(
            messageType = "INVITE",
            fromUserId = 1,
            fromUserName = "张三",
            link = "abc123",
            time = "2025-12-19T10:00:00",
            hasResponse = true,
            accepted = false,
            responseUserId = null,
            responseUserName = null
        )
        
        assertEquals("已拒绝", message.getStatusText())
    }
}
```
