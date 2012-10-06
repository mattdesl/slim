package slim.test.bare;

import java.util.Random;

public class Temp {
	
	public static void main(String[] args)
		Random r = new Random();
		
		
		final int[] array = new int[(int)Math.pow(2, 28)];
		for (int i=0; i<array.length; i++)
			array[i] = r.nextInt();
		
		System.out.println(array.length);
	}
}
