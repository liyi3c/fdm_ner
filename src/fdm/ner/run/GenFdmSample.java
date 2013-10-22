package fdm.ner.run;
import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import fdm.ner.train.TrainConvertor;

@Component
public class GenFdmSample {
	private TrainConvertor convertor;

	public TrainConvertor getConvertor() {
		return convertor;
	}

	@Resource
	public void setConvertor(TrainConvertor convertor) {
		this.convertor = convertor;
	}
	
	public static void main(String[] args) throws IOException {
		long startTime=System.currentTimeMillis();   //获取开始时间  

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");		
		GenFdmSample u = (GenFdmSample)ctx.getBean("genFdmSample");
		u.getConvertor().convert();
				
		long endTime=System.currentTimeMillis(); //获取结束时间  
		System.out.println("程序运行时间： "+(endTime-startTime)+"ms"); 
	}
}
