package oss_project_2022;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class oss_project implements MqttCallback{ // implement callback �߰� & �ʿ��� �޼ҵ� ����
	static MqttClient mqtt_client;// Mqtt Client ��ü ����
	
    public static void main(String[] args) {
    	oss_project obj = new oss_project();
    	obj.run();
    	
    }
    public void run() {    	
    	connectBroker(); // ���Ŀ ������ ����
    	try { 
    		mqtt_client.subscribe("region"); // region ���ҽ� ����    		
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    public void connectBroker() {
        String broker = "tcp://127.0.0.1:1883"; // ���Ŀ ������ �ּ� 
        String clientId = "weather publisher"; // Ŭ���̾�Ʈ�� ID
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqtt_client = new MqttClient(broker, clientId, persistence);// Mqtt Client ��ü �ʱ�ȭ
            MqttConnectOptions connOpts = new MqttConnectOptions(); // ���ӽ� ������ �ɼ��� �����ϴ� ��ü ����
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            mqtt_client.connect(connOpts); // ���Ŀ������ ����
            mqtt_client.setCallback(this);// Call back option �߰�
            System.out.println("Connected");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
    
    public void publish_data(String topic_input, String data) { 
        String topic = topic_input; // ����
        int qos = 1; // QoS level
        try {
        	//String sub_data = "{\"test\":10}";
            System.out.println("Publishing message: "+ data);
            
            mqtt_client.publish(topic, data.getBytes(), qos, false); // topic, �����͸� byte�� ��ȯ�Ͽ� ����, qos level, retain bit
            System.out.println("Message published");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
    
    public String get_weather_data(String[] lat_lng) {
    	
    	// ���� �ð� Ȯ���ؼ� ��¥, �ð� ����
    	Date current = new Date(System.currentTimeMillis());
    	SimpleDateFormat d_format = new SimpleDateFormat("yyyyMMddHHmmss"); 
    	String date = d_format.format(current).substring(0,8); // ���� ��¥
    	int i_date = Integer.parseInt(date);
    	String time = d_format.format(current).substring(8, 12); // ���� �ð�
    	int i_time_h = Integer.parseInt(time.substring(0, 2));
    	
    	if (time.substring(0, 2).equals("00")) {
    		i_date--;
    		date = String.valueOf(i_date);
    		time = "2300";
    	} else {
    		i_time_h--;
    		time = String.valueOf(i_time_h) + "00";
    	}
    	
    	if (time.length() == 3) time = "0" + time;    	
    	    	
    	
    	double[] d_lat_lng = {Double.parseDouble(lat_lng[0]), Double.parseDouble(lat_lng[1])};
    	int i_nx_ny[] = get_xy(d_lat_lng);
    	
    	System.out.println("base_date : " + date + ", base_time : " + time);	
    	System.out.println("nx : " + i_nx_ny[0] + ", ny : " + i_nx_ny[1]);
    	

        	String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst" // https�� �ƴ� http ���������� ���� �����ؾ� ��.
        			+ "?serviceKey=Lcm5d0LdcOf0ikmOlbmHQkrgM%2Fe%2Bl8laBOhOhXB4n9q8cOvOFyhnFvwKclSTvc%2BK3rll9BgV0dfGF9mdgnJGQA%3D%3D"
        			+ "&pageNo=1&numOfRows=1000"
        			+ "&dataType=XML"
        			+ "&base_date="+date
        			+ "&base_time="+time
        			+ "&nx=" + String.valueOf(i_nx_ny[0])
        			+ "&ny=" + String.valueOf(i_nx_ny[1]);
    				
        	Document doc = null;
        	String [] weather = new String[4];

    		// Jsoup���� API ������ ��������
    		try {
    			doc = Jsoup.connect(url).get();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}

    		//�޾ƿ� xml API �� ���ϴ� ������ ����
    		Elements elements = doc.select("item");    		
    		for (Element e : elements) {
    		
    			if (e.select("category").text().equals("PTY")) {
    				weather[0] = e.select("obsrValue").text();
    			} else if (e.select("category").text().equals("REH")) {
    				weather[1] = e.select("obsrValue").text();
    			} else if (e.select("category").text().equals("T1H")) {
    				weather[2] = e.select("obsrValue").text();
    			} else if (e.select("category").text().equals("WSD")) {
    				weather[3] = e.select("obsrValue").text();
    			}    			
    		}    		
    	System.out.println("PTY(���� ����) : " + weather[0] + ", REH(����) : " + weather[1] + 
    						", T1H(���) : " + weather[2] + ", WSD(ǳ��) : " + weather[3]); 
    	System.out.println("Web Crawling Ok");
    	
    	String s_weather = String.join(" ", weather);
    	
    	return s_weather;
    }
    
    //����, �浵 ������ �޾� ���� x, y ��ǥ�� ��ȯ (���¼ҽ� �̿�)
    //���� api�� ��û�� �� ���� x, y ��ǥ�� �䫊�ؾ� �Ѵ�. 
    public int[] get_xy(double[] lat_lng) {
    	double RE = 6371.00877; // ���� �ݰ�(km)
        double GRID = 5.0; // ���� ����(km)
        double SLAT1 = 30.0; // ���� ����1(degree)
        double SLAT2 = 60.0; // ���� ����2(degree)
        double OLON = 126.0; // ������ �浵(degree)
        double OLAT = 38.0; // ������ ����(degree)
        double XO = 43; // ������ X��ǥ(GRID)
        double YO = 136; // ������ Y��ǥ(GRID)
        
        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;
        int rs[] = {0, 0};
       
        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        
       
        double ra = Math.tan(Math.PI * 0.25 + (lat_lng[0]) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lat_lng[1] * DEGRAD - olon;
        
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;
        
        rs[0] = (int) (Math.floor(ra * Math.sin(theta) + XO + 0.5));
        rs[1] = (int) (Math.floor(ro - ra * Math.cos(theta) + YO + 0.5));
        
        return rs;
    }
    
    
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		System.out.println("Connection lost");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
	}

	//MQTT client�� �����ϰ� �ִ� ������ �޾��� �� ����Ǵ� �Լ�
	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		// TODO Auto-generated method stub
		if (topic.equals("region")){
			System.out.println("--------------------Received Message---------------------");
			System.out.println("Region : " + msg.toString());
			System.out.println("---------------------------------------------------------");
			String[] lat_lng = msg.toString().split(" ");
			
			String weather = get_weather_data(lat_lng);
		
			// web server�� Mqtt client �� '����, �浵, �µ�, ǳ��, ǳ��, ���� Ȯ��' ������ publish
			try {
    			publish_data("weather", weather);
    		}catch (Exception e) {
				// TODO: handle exception
    			try {
    				mqtt_client.disconnect();
				} catch (MqttException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			e.printStackTrace();
    	        System.out.println("Disconnected");
    	        System.exit(0);
			}
		}		
	}
}