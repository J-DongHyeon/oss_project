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

public class oss_project implements MqttCallback{ // implement callback 추가 & 필요한 메소드 정의
	static MqttClient mqtt_client;// Mqtt Client 객체 선언
	
    public static void main(String[] args) {
    	oss_project obj = new oss_project();
    	obj.run();
    	
    }
    public void run() {    	
    	connectBroker(); // 브로커 서버에 접속
    	try { 
    		mqtt_client.subscribe("region"); // region 리소스 구독    		
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    public void connectBroker() {
        String broker = "tcp://127.0.0.1:1883"; // 브로커 서버의 주소 
        String clientId = "weather publisher"; // 클라이언트의 ID
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqtt_client = new MqttClient(broker, clientId, persistence);// Mqtt Client 객체 초기화
            MqttConnectOptions connOpts = new MqttConnectOptions(); // 접속시 접속의 옵션을 정의하는 객체 생성
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            mqtt_client.connect(connOpts); // 브로커서버에 접속
            mqtt_client.setCallback(this);// Call back option 추가
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
        String topic = topic_input; // 토픽
        int qos = 1; // QoS level
        try {
        	//String sub_data = "{\"test\":10}";
            System.out.println("Publishing message: "+ data);
            
            mqtt_client.publish(topic, data.getBytes(), qos, false); // topic, 데이터를 byte로 변환하여 전송, qos level, retain bit
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
    	
    	// 현재 시간 확인해서 날짜, 시간 저장
    	Date current = new Date(System.currentTimeMillis());
    	SimpleDateFormat d_format = new SimpleDateFormat("yyyyMMddHHmmss"); 
    	String date = d_format.format(current).substring(0,8); // 현재 날짜
    	int i_date = Integer.parseInt(date);
    	String time = d_format.format(current).substring(8, 12); // 현재 시간
    	int i_time_h = Integer.parseInt(time.substring(0, 2));
    	
    	/* 
    	 * 공공데이터 요청 시간을 (현재시간 - 1) 시간으로 한다.
    	 * 너무 최근 시간의 공공 데이터 요청은 오류가 날 수도 있다.
    	*/ 
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
    	

    	String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst" // https가 아닌 http 프로토콜을 통해 접근해야 함.
    			+ "?serviceKey=Lcm5d0LdcOf0ikmOlbmHQkrgM%2Fe%2Bl8laBOhOhXB4n9q8cOvOFyhnFvwKclSTvc%2BK3rll9BgV0dfGF9mdgnJGQA%3D%3D"
    			+ "&pageNo=1&numOfRows=1000"
    			+ "&dataType=XML"
    			+ "&base_date="+date
    			+ "&base_time="+time
    			+ "&nx=" + String.valueOf(i_nx_ny[0])
    			+ "&ny=" + String.valueOf(i_nx_ny[1]);
				
    	Document doc = null;
    	String [] weather = new String[4];

		// Jsoup으로 API 데이터 가져오기
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 받아온 xml API 중 PTY (강수 형태), REH (습도), T1H (기온), WSD (풍속) 데이터 추출
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
    	System.out.println("PTY(강수 형태) : " + weather[0] + ", REH(습도) : " + weather[1] + 
    						", T1H(기온) : " + weather[2] + ", WSD(풍속) : " + weather[3]); 
    	System.out.println("Web Crawling Ok");
    	
    	String s_weather = String.join(" ", weather);
    	
    	return s_weather;
    }
    
    //위도, 경도 정보를 받아 격자 x, y 좌표로 변환 (오픈소스 이용)
    //공공 api를 요청할 때 격자 x, y 좌표로 요쳥해야 한다. 
    public int[] get_xy(double[] lat_lng) {
    	double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기준점 Y좌표(GRID)
        
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

	//MQTT client가 구독하고 있는 토픽을 받았을 때 실행되는 함수
	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception {
		// TODO Auto-generated method stub
		if (topic.equals("region")){
			System.out.println("--------------------Received Message---------------------");
			System.out.println("Region : " + msg.toString());
			System.out.println("---------------------------------------------------------");
			String[] lat_lng = msg.toString().split(" ");
			
			String weather = get_weather_data(lat_lng);
		
			// 이 mqtt client는 '강수 형태, 습도, 기온, 풍속' 데이터 토픽을 publish 한다.  			
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