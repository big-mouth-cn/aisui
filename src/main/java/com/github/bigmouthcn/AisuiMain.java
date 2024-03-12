package com.github.bigmouthcn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bigmouthcn.executor.*;
import com.github.bigmouthcn.service.MyFunctionExecutor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.ChatMessageAccumulator;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class AisuiMain {

    public static void main(String[] args) {
        OpenAiService service = createOpenAiService();

        List<ChatFunction> chatFunctions = createFunctions();

        MyFunctionExecutor functionExecutor = new MyFunctionExecutor(chatFunctions);

        List<ChatMessage> chatMessages = Lists.newArrayList(createSystemMessage());

        System.out.print("Enter your message: ");

        Scanner scanner = new Scanner(System.in);
        ChatMessage firstUserMessage = new ChatMessage(ChatMessageRole.USER.value(), scanner.nextLine());
        chatMessages.add(firstUserMessage);

        while (true) {
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo-16k-0613")
                    .messages(chatMessages)
                    .functions(functionExecutor.getFunctions())
                    .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
                    .n(1)
                    .maxTokens(4096)
                    .logitBias(Maps.newHashMap())
                    .build();

            Flowable<ChatCompletionChunk> flowable = service.streamChatCompletion(chatCompletionRequest);
            AtomicBoolean isFirstCompletion = new AtomicBoolean(true);
            ChatMessage completionChatMessage = service.mapStreamToAccumulator(flowable)
                    .doOnNext(new Consumer<ChatMessageAccumulator>() {
                        @Override
                        public void accept(ChatMessageAccumulator chatMessageAccumulator) throws Exception {
                            if (!chatMessageAccumulator.isFunctionCall()) {
                                if (isFirstCompletion.getAndSet(false)) {
                                    System.out.print("Response: ");
                                }
                                String token = chatMessageAccumulator.getMessageChunk().getContent();
                                if (null != token) {
                                    System.out.print(token);
                                }
                            } else {
                                if (isFirstCompletion.getAndSet(false)) {
                                    System.out.print("|- Please wait...");
                                }
                            }
                        }
                    })
                    .doOnComplete(System.out::println)
                    .doOnError(new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            System.err.println(throwable.getMessage());
                        }
                    })
                    .lastElement()
                    .blockingGet()
                    .getAccumulatedMessage();
            chatMessages.add(completionChatMessage);

            if (Objects.nonNull(completionChatMessage.getFunctionCall())) {
                // execute the function
                System.out.println("|- Executing: "+ completionChatMessage.getFunctionCall().getName());
                System.out.println("|- Arguments: "+ completionChatMessage.getFunctionCall().getArguments());
                ChatMessage functionResponse = functionExecutor.executeAndConvertToMessageHandlingExceptions(completionChatMessage.getFunctionCall());
                chatMessages.add(functionResponse);
                continue;
            }

            System.out.print("Enter your message: ");
            String nextLine = scanner.nextLine();
            if (nextLine.equalsIgnoreCase("exit")) {
                System.exit(0);
            }
            chatMessages.add(new ChatMessage(ChatMessageRole.USER.value(), nextLine));
        }
    }

    private static ChatMessage createSystemMessage() {
        return new ChatMessage(ChatMessageRole.SYSTEM.value(),
                "你是一个记账机器人，我会给你一些关于记账需求的指令，默认情况下请按照流程执行，不需要和我确认。" +
                        "" +
                        "# 记账流程" +
                        "- 首先，如果我的指令里包含了时间，你需要调用`query_current_time`来获取对应的时间；" +
                        "- 然后，你需要根据我的记账需求调用`build_accounting_transaction_object`生成交易对象。请一步一步生成，检查数据的准确性，是标准的JSON格式；" +
                        "- 最后将交易对象对应`bill`数据库表，生成可执行的SQL，调用`sql_executor`完成数据库的操作。" +
                        "" +
                        "## 非预期情况" +
                        "- 如果指令无法满足记账要求，请告诉我需要提供什么信息：比如缺少账户信息、分类、金额。" +
                        "" +
                        "# 查询流程" +
                        "- 请根据实际情况来获取时间后执行查询SQL即可，不需要生成交易对象；" +
                        "- 如果查询的结果有多条，请尽可能使用markdown格式展示出列表。" +
                        "" +
                        "```SQL 交易记录表" +
                        "CREATE TABLE `bill` (\n" +
                        "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
                        "  `type` smallint(2) DEFAULT '2' COMMENT '交易类型。1：收入，2：支出',\n" +
                        "  `classification` varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '分类',\n" +
                        "  `account` varchar(50) COLLATE utf8mb4_bin NOT NULL COMMENT '账户',\n" +
                        "  `amount` decimal(10,2) NOT NULL COMMENT '金额',\n" +
                        "  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间',\n" +
                        "  `description` varchar(256) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  KEY `idx_type` (`type`) USING BTREE,\n" +
                        "  KEY `idx_classification` (`classification`) USING BTREE,\n" +
                        "  KEY `idx_account` (`account`) USING BTREE\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;" +
                        "```");
    }

    private static List<ChatFunction> createFunctions() {
        ChatFunction queryCurrentTime = ChatFunction.builder()
                .name("query_current_time")
                .description("可以获取现实中的时间，格式是：yyyy-MM-dd HH:mm:ss")
                .executor(QueryCurrentTimeRequest.class, new QueryCurrentTimeFunction())
                .build();

        ChatFunction accountingTransactionFun = ChatFunction.builder()
                .name("build_accounting_transaction_object")
                .description("创建交易对象。")
                .executor(MultiAccountingTransactionRequest.class, new MultiAccountingTransactionFunction())
                .build();

        ChatFunction sqlExecutorFun = ChatFunction.builder()
                .name("sql_executor")
                .description("这个函数用来执行数据库操作。" +
                        "请务必只执行增删改查语句。" +
                        "目标数据库是MySQL。")
                .executor(SqlExecutorRequest.class, new SqlExecutorFunction())
                .build();

        return Lists.newArrayList(queryCurrentTime, accountingTransactionFun, sqlExecutorFun);
    }

    private static OpenAiService createOpenAiService() {
        String token = "your_aigateway_api_token";
        Duration timeout = Duration.ofSeconds(60);

        ObjectMapper mapper = OpenAiService.defaultObjectMapper();
        OkHttpClient client = OpenAiService.defaultClient(token, timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper);

        OpenAiApi api = retrofit.create(OpenAiApi.class);
        ExecutorService executorService = client.dispatcher().executorService();

        return new OpenAiService(api, executorService);
    }

    public static Retrofit defaultRetrofit(OkHttpClient client, ObjectMapper mapper) {
        return new Retrofit.Builder()
                .baseUrl("https://api.aigateway.work")
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}