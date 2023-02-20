package com.github.yuanrw.im.transfer.config;

/**
 * TransferConfig
 */
public class TransferConfig {

    private Integer port;

    private String redisHost;

    private Integer redisPort;

    private String redisPassword;

    private String rabbitmqHost;

    private Integer rabbitmqPort;

    private String rabbitmqUsername;

    private String rabbitmqPassword;

    private Long kafkaBuffer;
    private Integer kafkaLinger;
    private Integer kafkaBatch;
    private Integer kafkaRetries;

    public Integer getKafkaRetries() {
        return kafkaRetries;
    }

    public void setKafkaRetries(Integer kafkaRetries) {
        this.kafkaRetries = kafkaRetries;
    }

    public Long getKafkaBuffer() {
        return kafkaBuffer;
    }

    public void setKafkaBuffer(Long kafkaBuffer) {
        this.kafkaBuffer = kafkaBuffer;
    }

    public Integer getKafkaLinger() {
        return kafkaLinger;
    }

    public void setKafkaLinger(Integer kafkaLinger) {
        this.kafkaLinger = kafkaLinger;
    }

    private String kafkaAcks;
    private String kafkaBootstrap;

    public String getKafkaAcks() {
        return kafkaAcks;
    }

    public void setKafkaAcks(String kafkaAcks) {
        this.kafkaAcks = kafkaAcks;
    }

    public String getKafkaBootstrap() {
        return kafkaBootstrap;
    }

    public void setKafkaBootstrap(String kafkaBootstrap) {
        this.kafkaBootstrap = kafkaBootstrap;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public Integer getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(Integer redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getRabbitmqHost() {
        return rabbitmqHost;
    }

    public void setRabbitmqHost(String rabbitmqHost) {
        this.rabbitmqHost = rabbitmqHost;
    }

    public Integer getRabbitmqPort() {
        return rabbitmqPort;
    }

    public void setRabbitmqPort(Integer rabbitmqPort) {
        this.rabbitmqPort = rabbitmqPort;
    }

    public String getRabbitmqUsername() {
        return rabbitmqUsername;
    }

    public void setRabbitmqUsername(String rabbitmqUsername) {
        this.rabbitmqUsername = rabbitmqUsername;
    }

    public String getRabbitmqPassword() {
        return rabbitmqPassword;
    }

    public void setRabbitmqPassword(String rabbitmqPassword) {
        this.rabbitmqPassword = rabbitmqPassword;
    }

    public Integer getKafkaBatch() {
        return kafkaBatch;
    }

    public void setKafkaBatch(Integer kafkaBatch) {
        this.kafkaBatch = kafkaBatch;
    }
}
