package fdm.ner.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;


import exp.check.io.IoExpHandler;
import fdm.ner.domain.Intro_learn;

enum Type 
{
	POS(999), COM(888), DPT(777),
	COMPRI(8881), AWDCOM(889), AWDPRI(8891),
	//TYPE150(150), /*,*/TYPE151(151) /*、*/, TYPE152(152), /*。*/
	TYPE132(132), /*日期*/ 
	//TYPE170(170) /*任*/, 
	//TYPE95(95), /*名词*/
	//TYPE108(108), /*介词*/ 
	//TYPE20(20), /*区分词*/ 
	//TYPE(40)/*副词*/
	;
	
	private int type;
	
	private Type(int type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return String.valueOf(type);
	}
	
	public static boolean contains(String type) {
		for (Type t : Type.values()) {
			if (t.toString().equals(type))
				return true;
		}
		
		return false;
	}
	
}

@Component
public class TrainConvertor {
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Resource
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void convert() {
		File samples = new File("sampleFile.list");
		File trainData = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		FileWriter ffw = null;
		BufferedWriter fbw = null;
		
		try {			
			ffw = new FileWriter(samples);
			fbw = new BufferedWriter(ffw);
			String sIntro;
			Session session = sessionFactory.openSession();
			session.beginTransaction();
			Query q = session.createQuery("from Intro_learn where mod(pid,32) = 2");
			@SuppressWarnings("unchecked")
			List<Intro_learn> sIntroList = q.list();
			
			Pattern pattern = Pattern.compile("(?<tok>[^\\s]+)[\t ]+(?<type>\\d+)[\t ]+(?<index>\\d+)[\t ]+");
			Matcher matcher;
			for(int i = 0; i  < sIntroList.size(); i++) {
				if (i%10 == 0) {
					trainData = new File("fdmSample" + String.valueOf(i) + ".ner");
					fbw.write("fdmSample" + String.valueOf(i) + ".ner");
					fbw.write(",");
					fw = new FileWriter(trainData);
					bw = new BufferedWriter(fw);
				}
				sIntro = sIntroList.get(i).getIntroduction();
				matcher = pattern.matcher(sIntro);
				while(matcher.find()) {
					bw.write(matcher.group("tok"));
					bw.write("\t");
					if (Type.contains(matcher.group("type")))
						bw.write(matcher.group("type"));
					else
						bw.write("000");
					bw.write("\n");
				}
				if (i%10 == 9) {
					IoExpHandler.closeWriter(bw);
					IoExpHandler.closeWriter(fw);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IoExpHandler.closeWriter(fbw);
			IoExpHandler.closeWriter(ffw);
		}
	}
	
	public void reportTagFreq() {
		HashMap<String,Integer> tagCntMap = new HashMap<String,Integer>();
		MapValueComparator<String, Integer> mvc =  new MapValueComparator<String, Integer>(tagCntMap);
		TreeMap<String, Integer> tagCntTreeMap = new TreeMap<String, Integer>(mvc);
		
		String sIntro;
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Query q = session.createQuery("from Intro_learn");
		@SuppressWarnings("unchecked")
		List<Intro_learn> sIntroList = q.list();
		
		Pattern pattern = Pattern.compile("(?<tok>[^\\s]+)[\t ]+(?<type>\\d+)[\t ]+(?<index>\\d+)[\t ]+");
		Matcher matcher;
		for(int i = 0; i  < sIntroList.size(); i++) {
			sIntro = sIntroList.get(i).getIntroduction();
			matcher = pattern.matcher(sIntro);
			while(matcher.find()) {
				if(tagCntMap.containsKey(matcher.group("type")))
				tagCntMap.put(matcher.group("type"), tagCntMap.get(matcher.group("type"))+1);
			else
				tagCntMap.put(matcher.group("type"), 1);
			}
		}
		tagCntTreeMap.putAll(tagCntMap);
		for (String type : tagCntTreeMap.keySet())
			System.out.println(type + ": " + tagCntMap.get(type));
	}
	
	class MapValueComparator<K, V extends Comparable<V>> implements Comparator<K> {

	    Map<K, V> base;
	    public MapValueComparator(Map<K, V> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(K a, K b) {
	        if (base.get(a).compareTo(base.get(b)) > 0) {
	        	// DESC
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
}
