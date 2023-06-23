package com.example.rocketmqandrabbitmq;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.javassist.bytecode.stackmap.BasicBlock.Catch;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import com.example.rocketmqandrabbitmq.Mapper.DeviceMapper;


@EnableRabbit
@SpringBootApplication
public class RocketmqandrabbitmqApplication {

    private static final String BROKER_URL = "tcp://";


    public static void main(String[] args) {
        String[] topics = {"Topic1", "Topic2", "Topic3", "Topic4", "Topic5","Hello"};

        for (String topic : topics) {
            try {
                MQTTSubscriber subscriber = new MQTTSubscriber(topic);
                subscriber.start();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

		SpringApplication.run(RocketmqandrabbitmqApplication.class, args);
	}

}

class MqttPublisher {

    private String brokerUrl="tcp://";
    private String clientId = "1";
    private IMqttClient mqttClient;

    public MqttPublisher(){
        
    }

    public MqttPublisher(String brokerUrl, String clientId) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
    }

    public void connect() throws MqttException {
        mqttClient = new MqttClient(brokerUrl, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        mqttClient.connect(options);
    }

    public void publish(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        mqttClient.publish(topic, message);
    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }
}

class MQTTSubscriber {
    private static final String BROKER_URL = "tcp://";
    private String topic;
    private DeviceMapper deviceMapper;

    public MQTTSubscriber(String topic) {
        this.topic = topic;
        
    }

    public void start() throws MqttException {
        MqttClient client = new MqttClient(BROKER_URL, MqttClient.generateClientId());
        System.out.println("MQTT Subscriber started for topic: " + topic);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        client.connect(options);

        //client.subscribe(topic);
		//client.subscribe(topic);
		client.subscribeWithResponse(topic,(IMqttMessageListener) new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Received message for topic " + topic + ": " + message.toString());
                // You can add your own logic here to process the received data
                if(topic.equals("Topic1")){
                    System.out.println("SQL Topic 1 will be runn");
                } 
                

                if(topic.equals("Hello")){
                    System.out.println("Hello");
                    try{
                            // Create the SqlSessionFactory (assuming mybatis-config.xml is in the classpath)
                            SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));

                            // Open a new SqlSession
                            SqlSession session = sessionFactory.openSession();
                                // Get an instance of DeviceMapper
                                DeviceMapper deviceMapper = session.getMapper(DeviceMapper.class);
                                
                                // Use the DeviceMapper to insert device data
                                deviceMapper.insertDeviceData(message.toString());
                                
                                // Commit the transaction (if needed)
                                session.commit();
                                MqttPublisher mqttPublisher = new MqttPublisher();
                                mqttPublisher.connect();
                                mqttPublisher.publish("GetHello", "Replies : " + message.toString());
                                mqttPublisher.disconnect();
                      
                    }catch(Exception e){
                        System.out.println(e);
                    }
                }
            }
        });
    }
}







